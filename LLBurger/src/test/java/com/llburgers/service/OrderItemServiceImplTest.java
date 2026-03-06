package com.llburgers.service;

import com.llburgers.domain.*;
import com.llburgers.domain.enums.*;
import com.llburgers.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OrderItemServiceImplTest {

    @Autowired private IOrderItemService orderItemService;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;

    private Order savedOrder;
    private Product product;
    private OrderItem savedOrderItem;

    @BeforeEach
    void setUp() {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Item Customer")
                .email("item.cust." + UUID.randomUUID() + "@llburgers.com")
                .password("itemPass99")
                .role(Role.CUSTOMER)
                .block(Block.B)
                .roomNumber("202")
                .active(true)
                .build());

        product = productRepository.save(Product.builder()
                .name("Item Burger " + UUID.randomUUID())
                .price(new BigDecimal("60.00"))
                .category(ProductCategory.BURGER)
                .stockQuantity(20)
                .availability(true)
                .build());

        savedOrder = orderRepository.save(Order.builder()
                .customer(customer)
                .totalPrice(new BigDecimal("60.00"))
                .status(OrderStatus.PROCESSING)
                .deliveryBlock(Block.B)
                .deliveryRoomNumber("202")
                .build());

        savedOrderItem = orderItemRepository.save(OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .quantity(1)
                .totalPrice(new BigDecimal("60.00"))
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists an order item and generates an ID")
    void create_persistsOrderItemWithGeneratedId() {
        OrderItem item = OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .quantity(2)
                .totalPrice(new BigDecimal("120.00"))
                .build();

        OrderItem created = orderItemService.create(item);

        assertNotNull(created.getId());
        assertTrue(orderItemRepository.existsById(created.getId()));
        assertEquals(2, created.getQuantity());
    }

    @Test
    @DisplayName("PASS – read() returns the correct order item from the database")
    void read_returnsCorrectOrderItem() {
        OrderItem fetched = orderItemService.read(savedOrderItem.getId());

        assertEquals(savedOrderItem.getId(), fetched.getId());
        assertEquals(1, fetched.getQuantity());
        assertEquals(product.getId(), fetched.getProduct().getId());
    }

    @Test
    @DisplayName("PASS – findByOrderId() returns order items for the given order")
    void findByOrderId_returnsItemsForOrder() {
        List<OrderItem> items = orderItemService.findByOrderId(savedOrder.getId());

        assertFalse(items.isEmpty());
        assertTrue(items.stream().anyMatch(i -> i.getId().equals(savedOrderItem.getId())));
    }

    @Test
    @DisplayName("PASS – findByProductId() returns order items containing the given product")
    void findByProductId_returnsItemsForProduct() {
        List<OrderItem> items = orderItemService.findByProductId(product.getId());

        assertFalse(items.isEmpty());
        assertTrue(items.stream().allMatch(i -> i.getProduct().getId().equals(product.getId())));
    }

    @Test
    @DisplayName("PASS – countByProductId() returns at least 1 when product has been ordered")
    void countByProductId_returnsPositiveCount() {
        long count = orderItemService.countByProductId(product.getId());

        assertTrue(count >= 1);
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        assertDoesNotThrow(() -> orderItemService.read(UUID.randomUUID()),
                "Intentionally wrong — OrderItemServiceImpl throws; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – asserts wrong quantity on fetched order item (intentionally wrong)")
    void read_assertsWrongQuantity_fail() {
        OrderItem fetched = orderItemService.read(savedOrderItem.getId());

        assertEquals(99, fetched.getQuantity(),
                "Intentionally wrong — actual quantity is 1; this should FAIL");
    }
}
