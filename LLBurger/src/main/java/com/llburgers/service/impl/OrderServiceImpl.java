package com.llburgers.service.impl;

import com.llburgers.domain.*;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.OrderStatus;
import com.llburgers.event.OrderCancelledEvent;
import com.llburgers.event.OrderPlacedEvent;
import com.llburgers.event.OrderStatusChangedEvent;
import com.llburgers.repository.*;
import com.llburgers.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Full order processing workflow as per documentation:
 * <ol>
 *     <li>Validate business is open</li>
 *     <li>Validate product / extra / side availability and stock</li>
 *     <li>Validate minimum order threshold</li>
 *     <li>Validate delivery block and room number are provided</li>
 *     <li>Reduce stock quantities atomically</li>
 *     <li>Save order with special instructions and delivery info</li>
 *     <li>Create notification record</li>
 *     <li>Send order confirmation email (Mailjet)</li>
 * </ol>
 */
@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final BigDecimal MINIMUM_ORDER_AMOUNT = new BigDecimal("20.00");

    private final OrderRepository repository;
    private final BusinessStatusRepository businessStatusRepository;
    private final ProductRepository productRepository;
    private final SideRepository sideRepository;
    private final ExtraRepository extraRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceImpl(OrderRepository repository,
                            BusinessStatusRepository businessStatusRepository,
                            ProductRepository productRepository,
                            SideRepository sideRepository,
                            ExtraRepository extraRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.businessStatusRepository = businessStatusRepository;
        this.productRepository = productRepository;
        this.sideRepository = sideRepository;
        this.extraRepository = extraRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Full order creation workflow:
     * validates business state → availability → stock → minimum amount →
     * delivery info → reduces stock → saves → notifies → emails.
     */
    @Transactional
    @Override
    public Order create(Order order) {
        // 1. Business must be open
        BusinessStatus status = businessStatusRepository.findById(1L).orElse(null);
        if (status == null || !status.isOpen()) {
            throw new IllegalStateException("Cannot place orders — business is currently closed");
        }

        // 2. Delivery info required
        if (order.getDeliveryBlock() == null) {
            throw new IllegalArgumentException("Delivery block is required");
        }
        if (order.getDeliveryRoomNumber() == null || order.getDeliveryRoomNumber().isBlank()) {
            throw new IllegalArgumentException("Delivery room number is required");
        }

        // 3. Validate and reduce stock for every order item
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        List<UUID> affectedProductIds = new ArrayList<>();
        List<UUID> affectedExtraIds = new ArrayList<>();
        List<UUID> affectedSideIds = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found: " + item.getProduct().getId()));

            if (!product.isAvailability()) {
                throw new IllegalStateException("Product is unavailable: " + product.getName());
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for " + product.getName()
                        + " (requested: " + item.getQuantity()
                        + ", available: " + product.getStockQuantity() + ")");
            }

            // Reduce stock atomically
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
            affectedProductIds.add(product.getId());

            // Validate extras
            for (OrderItemExtra oie : item.getExtras()) {
                Extra extra = extraRepository.findById(oie.getExtra().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Extra not found: " + oie.getExtra().getId()));
                if (!extra.isAvailability()) {
                    throw new IllegalStateException("Extra is unavailable: " + extra.getName());
                }
                if (extra.getStockQuantity() < oie.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for extra: " + extra.getName());
                }
                extra.setStockQuantity(extra.getStockQuantity() - oie.getQuantity());
                extraRepository.save(extra);
                affectedExtraIds.add(extra.getId());
            }

            // Validate sides
            for (OrderItemSide ois : item.getSides()) {
                Side side = sideRepository.findById(ois.getSide().getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Side not found: " + ois.getSide().getId()));
                if (!side.isAvailability()) {
                    throw new IllegalStateException("Side is unavailable: " + side.getName());
                }
                if (side.getStockQuantity() < ois.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for side: " + side.getName());
                }
                side.setStockQuantity(side.getStockQuantity() - ois.getQuantity());
                sideRepository.save(side);
                affectedSideIds.add(side.getId());
            }

            calculatedTotal = calculatedTotal.add(item.getTotalPrice());
        }

        // 4. Minimum order threshold
        if (calculatedTotal.compareTo(MINIMUM_ORDER_AMOUNT) < 0) {
            throw new IllegalArgumentException(
                    "Order total (R" + calculatedTotal
                    + ") is below the minimum order amount of R" + MINIMUM_ORDER_AMOUNT);
        }

        // 5. Save the order
        order.setTotalPrice(calculatedTotal);
        order.setStatus(OrderStatus.PROCESSING);
        Order saved = repository.save(order);

        // 6. Publish OrderPlacedEvent — listeners handle notifications, emails, and stock events
        eventPublisher.publishEvent(new OrderPlacedEvent(
                saved, affectedProductIds, affectedExtraIds, affectedSideIds));

        log.info("[ORDER-CREATED] id={}, customer={}, total={}, block={}, room={}",
                saved.getId(), saved.getCustomer().getId(),
                saved.getTotalPrice(), saved.getDeliveryBlock(), saved.getDeliveryRoomNumber());

        return saved;
    }

    @Override
    public Order read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
    }

    @Override
    public Order update(Order order) {
        if (order.getId() == null || !repository.existsById(order.getId())) {
            throw new IllegalArgumentException("Order not found with id: " + order.getId());
        }
        return repository.save(order);
    }

    @Override
    public List<Order> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Order not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── By Customer ─────────────────────────────────────────────────────────

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status) {
        return repository.findByCustomerIdAndStatus(customerId, status);
    }

    @Override
    public List<Order> findOrderHistory(UUID customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    // ─── By Status ────────────────────────────────────────────────────────────

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return repository.countByStatus(status);
    }

    // ─── By Delivery Location ─────────────────────────────────────────────────

    @Override
    public List<Order> findByDeliveryBlock(Block deliveryBlock) {
        return repository.findByDeliveryBlock(deliveryBlock);
    }

    @Override
    public List<Order> findByDeliveryBlockAndStatus(Block deliveryBlock, OrderStatus status) {
        return repository.findByDeliveryBlockAndStatus(deliveryBlock, status);
    }

    // ─── By Date ──────────────────────────────────────────────────────────────

    @Override
    public List<Order> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return repository.findByCreatedAtBetween(from, to);
    }

    @Override
    public List<Order> findByStatusAndDateRange(OrderStatus status, LocalDateTime from, LocalDateTime to) {
        return repository.findByStatusAndCreatedAtBetween(status, from, to);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    /**
     * Structured order status workflow: Processing → On The Way → Delivered.
     * Sends a status-update email and creates a notification for every transition.
     */
    @Transactional
    @Override
    public Order updateStatus(UUID id, OrderStatus newStatus) {
        Order order = read(id);
        OrderStatus current = order.getStatus();

        // Enforce structured workflow
        if (current == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot update status — order is already delivered");
        }
        if (current == OrderStatus.PROCESSING && newStatus != OrderStatus.ON_THE_WAY) {
            throw new IllegalStateException("Order must move from PROCESSING → ON_THE_WAY");
        }
        if (current == OrderStatus.ON_THE_WAY && newStatus != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order must move from ON_THE_WAY → DELIVERED");
        }

        order.setStatus(newStatus);
        Order saved = repository.save(order);

        // Publish OrderStatusChangedEvent — listeners handle notification + email
        eventPublisher.publishEvent(new OrderStatusChangedEvent(saved, current, newStatus));

        log.info("[ORDER-STATUS] id={}, {} → {}", saved.getId(), current, newStatus);
        return saved;
    }

    @Override
    public Order cancel(UUID id) {
        Order order = read(id);
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }
        if (order.getStatus() == OrderStatus.ON_THE_WAY) {
            throw new IllegalStateException("Cannot cancel an order that is already on the way");
        }

        // Restore stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);

            for (OrderItemExtra oie : item.getExtras()) {
                Extra extra = oie.getExtra();
                extra.setStockQuantity(extra.getStockQuantity() + oie.getQuantity());
                extraRepository.save(extra);
            }

            for (OrderItemSide ois : item.getSides()) {
                Side side = ois.getSide();
                side.setStockQuantity(side.getStockQuantity() + ois.getQuantity());
                sideRepository.save(side);
            }
        }

        repository.delete(order);

        // Publish OrderCancelledEvent — listeners fire stock-updated events
        eventPublisher.publishEvent(new OrderCancelledEvent(order));

        log.info("[ORDER-CANCELLED] id={}", id);
        return order;
    }
}
