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
class OrderItemExtraServiceImplTest {

    @Autowired private IOrderItemExtraService orderItemExtraService;
    @Autowired private OrderItemExtraRepository orderItemExtraRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ExtraRepository extraRepository;

    private OrderItem savedOrderItem;
    private Extra extra;
    private OrderItemExtra savedOrderItemExtra;

    @BeforeEach
    void setUp() {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Extra Customer")
                .email("extra.cust." + UUID.randomUUID() + "@llburgers.com")
                .password("extraPass99")
                .role(Role.CUSTOMER)
                .block(Block.B)
                .roomNumber("210")
                .active(true)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Extra Burger " + UUID.randomUUID())
                .price(new BigDecimal("60.00"))
                .category(ProductCategory.BURGER)
                .stockQuantity(10)
                .availability(true)
                .build());

        extra = extraRepository.save(Extra.builder()
                .name("Test Jalapeno " + UUID.randomUUID())
                .price(new BigDecimal("10.00"))
                .stockQuantity(50)
                .availability(true)
                .build());

        Order savedOrder = orderRepository.save(Order.builder()
                .customer(customer)
                .totalPrice(new BigDecimal("60.00"))
                .status(OrderStatus.PROCESSING)
                .deliveryBlock(Block.B)
                .deliveryRoomNumber("210")
                .build());

        savedOrderItem = orderItemRepository.save(OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .quantity(1)
                .totalPrice(new BigDecimal("60.00"))
                .build());

        savedOrderItemExtra = orderItemExtraRepository.save(OrderItemExtra.builder()
                .orderItem(savedOrderItem)
                .extra(extra)
                .quantity(1)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists an order item extra and generates an ID")
    void create_persistsOrderItemExtraWithGeneratedId() {
        OrderItemExtra oie = OrderItemExtra.builder()
                .orderItem(savedOrderItem)
                .extra(extra)
                .quantity(3)
                .build();

        OrderItemExtra created = orderItemExtraService.create(oie);

        assertNotNull(created.getId());
        assertTrue(orderItemExtraRepository.existsById(created.getId()));
        assertEquals(3, created.getQuantity());
    }

    @Test
    @DisplayName("PASS – read() returns the correct order item extra")
    void read_returnsCorrectOrderItemExtra() {
        OrderItemExtra fetched = orderItemExtraService.read(savedOrderItemExtra.getId());

        assertEquals(savedOrderItemExtra.getId(), fetched.getId());
        assertEquals(extra.getId(), fetched.getExtra().getId());
        assertEquals(1, fetched.getQuantity());
    }

    @Test
    @DisplayName("PASS – findByOrderItemId() returns extras for a given order item")
    void findByOrderItemId_returnsExtrasForOrderItem() {
        List<OrderItemExtra> results = orderItemExtraService.findByOrderItemId(savedOrderItem.getId());

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.getId().equals(savedOrderItemExtra.getId())));
    }

    @Test
    @DisplayName("PASS – findByExtraId() returns order item extras for a given extra")
    void findByExtraId_returnsMatchingExtras() {
        List<OrderItemExtra> results = orderItemExtraService.findByExtraId(extra.getId());

        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(e -> e.getExtra().getId().equals(extra.getId())));
    }

    @Test
    @DisplayName("PASS – countByExtraId() returns at least 1 after saving one order item extra")
    void countByExtraId_returnsPositiveCount() {
        long count = orderItemExtraService.countByExtraId(extra.getId());

        assertTrue(count >= 1);
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        assertDoesNotThrow(() -> orderItemExtraService.read(UUID.randomUUID()),
                "Intentionally wrong — OrderItemExtraServiceImpl throws; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – asserts wrong quantity on fetched order item extra (intentionally wrong)")
    void read_assertsWrongQuantity_fail() {
        OrderItemExtra fetched = orderItemExtraService.read(savedOrderItemExtra.getId());

        assertEquals(99, fetched.getQuantity(),
                "Intentionally wrong — actual quantity is 1; this should FAIL");
    }
}
