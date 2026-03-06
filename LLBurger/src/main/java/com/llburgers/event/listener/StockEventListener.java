package com.llburgers.event.listener;

import com.llburgers.domain.*;
import com.llburgers.event.OrderCancelledEvent;
import com.llburgers.event.OrderPlacedEvent;
import com.llburgers.event.StockUpdatedEvent;
import com.llburgers.repository.ExtraRepository;
import com.llburgers.repository.ProductRepository;
import com.llburgers.repository.SideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Translates order-lifecycle events into fine-grained {@link StockUpdatedEvent}s
 * so that dashboards and analytics systems can react to every stock change
 * individually, in real time.
 *
 * <p>This listener fires <em>synchronously</em> (before commit) so that
 * StockUpdatedEvent consumers see the latest stock values within the same
 * transaction context.  If you need post-commit behaviour, change the
 * annotation to {@code @TransactionalEventListener}.</p>
 */
@Component
public class StockEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockEventListener.class);

    private final ApplicationEventPublisher publisher;
    private final ProductRepository productRepository;
    private final ExtraRepository extraRepository;
    private final SideRepository sideRepository;

    public StockEventListener(ApplicationEventPublisher publisher,
                              ProductRepository productRepository,
                              ExtraRepository extraRepository,
                              SideRepository sideRepository) {
        this.publisher = publisher;
        this.productRepository = productRepository;
        this.extraRepository = extraRepository;
        this.sideRepository = sideRepository;
    }

    // ─── Order Placed → stock decreased ───────────────────────────────────────

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        Order order = event.order();
        log.debug("[STOCK-EVENT] Processing stock events for placed order={}", order.getId());

        for (UUID productId : event.affectedProductIds()) {
            productRepository.findById(productId).ifPresent(p ->
                    publisher.publishEvent(new StockUpdatedEvent(
                            "PRODUCT", p.getId(), p.getName(),
                            p.getStockQuantity(), p.getStockQuantity(), 0)));
        }
        for (UUID extraId : event.affectedExtraIds()) {
            extraRepository.findById(extraId).ifPresent(e ->
                    publisher.publishEvent(new StockUpdatedEvent(
                            "EXTRA", e.getId(), e.getName(),
                            e.getStockQuantity(), e.getStockQuantity(), 0)));
        }
        for (UUID sideId : event.affectedSideIds()) {
            sideRepository.findById(sideId).ifPresent(s ->
                    publisher.publishEvent(new StockUpdatedEvent(
                            "SIDE", s.getId(), s.getName(),
                            s.getStockQuantity(), s.getStockQuantity(), 0)));
        }
    }

    // ─── Order Cancelled → stock restored ─────────────────────────────────────

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        Order order = event.order();
        log.debug("[STOCK-EVENT] Processing stock events for cancelled order={}", order.getId());

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            publisher.publishEvent(new StockUpdatedEvent(
                    "PRODUCT", product.getId(), product.getName(),
                    product.getStockQuantity() - item.getQuantity(),
                    product.getStockQuantity(),
                    item.getQuantity()));

            for (OrderItemExtra oie : item.getExtras()) {
                Extra extra = oie.getExtra();
                publisher.publishEvent(new StockUpdatedEvent(
                        "EXTRA", extra.getId(), extra.getName(),
                        extra.getStockQuantity() - oie.getQuantity(),
                        extra.getStockQuantity(),
                        oie.getQuantity()));
            }

            for (OrderItemSide ois : item.getSides()) {
                Side side = ois.getSide();
                publisher.publishEvent(new StockUpdatedEvent(
                        "SIDE", side.getId(), side.getName(),
                        side.getStockQuantity() - ois.getQuantity(),
                        side.getStockQuantity(),
                        ois.getQuantity()));
            }
        }
    }

    // ─── StockUpdatedEvent consumer (logging) ─────────────────────────────────

    /**
     * Default consumer that simply logs every stock change.
     * Replace or supplement with WebSocket pushes / SSE for real-time dashboard.
     */
    @EventListener
    public void onStockUpdated(StockUpdatedEvent event) {
        log.info("[STOCK] {} {} ({}): stock {} → {} (delta={})",
                event.itemType(), event.itemId(), event.itemName(),
                event.oldStock(), event.newStock(), event.delta());
    }
}
