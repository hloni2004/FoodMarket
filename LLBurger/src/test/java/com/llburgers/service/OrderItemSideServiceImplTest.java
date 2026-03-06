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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OrderItemSideServiceImplTest {

    @Autowired private IOrderItemSideService orderItemSideService;
    @Autowired private OrderItemSideRepository orderItemSideRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private SideRepository sideRepository;

    private OrderItem savedOrderItem;
    private Side side;
    private OrderItemSide savedOrderItemSide;

    @BeforeEach
    void setUp() {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Side Customer")
                .email("side.cust." + UUID.randomUUID() + "@llburgers.com")
                .password("sidePass99")
                .role(Role.CUSTOMER)
                .block(Block.A)
                .roomNumber("103")
                .active(true)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Side Burger " + UUID.randomUUID())
                .price(new BigDecimal("55.00"))
                .category(ProductCategory.BURGER)
                .stockQuantity(10)
                .availability(true)
                .build());

        side = sideRepository.save(Side.builder()
                .name("Test Chips " + UUID.randomUUID())
                .price(new BigDecimal("20.00"))
                .stockQuantity(30)
                .availability(true)
                .build());

        Order savedOrder = orderRepository.save(Order.builder()
                .customer(customer)
                .totalPrice(new BigDecimal("55.00"))
                .status(OrderStatus.PROCESSING)
                .deliveryBlock(Block.A)
                .deliveryRoomNumber("103")
                .build());

        savedOrderItem = orderItemRepository.save(OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .quantity(1)
                .totalPrice(new BigDecimal("55.00"))
                .build());

        savedOrderItemSide = orderItemSideRepository.save(OrderItemSide.builder()
                .orderItem(savedOrderItem)
                .side(side)
                .quantity(1)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists an order item side and generates an ID")
    void create_persistsOrderItemSideWithGeneratedId() {
        OrderItemSide ois = OrderItemSide.builder()
                .orderItem(savedOrderItem)
                .side(side)
                .quantity(2)
                .build();

        OrderItemSide created = orderItemSideService.create(ois);

        assertNotNull(created.getId());
        assertTrue(orderItemSideRepository.existsById(created.getId()));
        assertEquals(2, created.getQuantity());
    }

    @Test
    @DisplayName("PASS – read() returns the correct order item side")
    void read_returnsCorrectOrderItemSide() {
        OrderItemSide fetched = orderItemSideService.read(savedOrderItemSide.getId());

        assertEquals(savedOrderItemSide.getId(), fetched.getId());
        assertEquals(side.getId(), fetched.getSide().getId());
        assertEquals(1, fetched.getQuantity());
    }

    @Test
    @DisplayName("PASS – findByOrderItemId() returns sides for a given order item")
    void findByOrderItemId_returnsSidesForOrderItem() {
        List<OrderItemSide> results = orderItemSideService.findByOrderItemId(savedOrderItem.getId());

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(s -> s.getId().equals(savedOrderItemSide.getId())));
    }

    @Test
    @DisplayName("PASS – findBySideId() returns order item sides for a given side")
    void findBySideId_returnsMatchingSides() {
        List<OrderItemSide> results = orderItemSideService.findBySideId(side.getId());

        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(s -> s.getSide().getId().equals(side.getId())));
    }

    @Test
    @DisplayName("PASS – countBySideId() returns at least 1 after saving one order item side")
    void countBySideId_returnsPositiveCount() {
        long count = orderItemSideService.countBySideId(side.getId());

        assertTrue(count >= 1);
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        assertDoesNotThrow(() -> orderItemSideService.read(UUID.randomUUID()),
                "Intentionally wrong — OrderItemSideServiceImpl throws; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – asserts wrong quantity on fetched order item side (intentionally wrong)")
    void read_assertsWrongQuantity_fail() {
        OrderItemSide fetched = orderItemSideService.read(savedOrderItemSide.getId());

        assertEquals(99, fetched.getQuantity(),
                "Intentionally wrong — actual quantity is 1; this should FAIL");
    }
}
