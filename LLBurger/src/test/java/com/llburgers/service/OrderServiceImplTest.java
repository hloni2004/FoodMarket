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
class OrderServiceImplTest {

    @Autowired private IOrderService orderService;
    @Autowired private IBusinessStatusService businessStatusService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BusinessStatusRepository businessStatusRepository;
    @Autowired private AdminRepository adminRepository;

    private Customer customer;
    private Product product;
    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = adminRepository.save(Admin.builder()
                .name("Order Admin")
                .email("order.admin." + UUID.randomUUID() + "@llburgers.com")
                .password("orderAdminPass1")
                .role(Role.ADMIN)
                .adminLevel(AdminLevel.SUPER_ADMIN)
                .active(true)
                .build());

        customer = customerRepository.save(Customer.builder()
                .name("Order Customer")
                .email("order.customer." + UUID.randomUUID() + "@llburgers.com")
                .password("custPass99")
                .role(Role.CUSTOMER)
                .block(Block.B)
                .roomNumber("201")
                .active(true)
                .build());

        product = productRepository.save(Product.builder()
                .name("Test Burger " + UUID.randomUUID())
                .price(new BigDecimal("60.00"))
                .category(ProductCategory.BURGER)
                .stockQuantity(50)
                .availability(true)
                .build());

        // Ensure business is open for tests that need it
        businessStatusService.openBusiness(admin);
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() places a valid order, persists it, and reduces stock")
    void create_placesOrderAndReducesStock() {
        int stockBefore = productRepository.findById(product.getId()).orElseThrow().getStockQuantity();

        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(2)
                .totalPrice(product.getPrice().multiply(new BigDecimal("2")))
                .extras(new ArrayList<>())
                .sides(new ArrayList<>())
                .build();

        Order order = Order.builder()
                .customer(customer)
                .deliveryBlock(Block.B)
                .deliveryRoomNumber("201")
                .orderItems(List.of(item))
                .build();
        item.setOrder(order);

        Order created = orderService.create(order);

        assertNotNull(created.getId());
        assertEquals(OrderStatus.PROCESSING, created.getStatus());
        assertEquals(0, new BigDecimal("120.00").compareTo(created.getTotalPrice()));

        int stockAfter = productRepository.findById(product.getId()).orElseThrow().getStockQuantity();
        assertEquals(stockBefore - 2, stockAfter);
    }

    @Test
    @DisplayName("PASS – read() returns the correct order from the database")
    void read_returnsCorrectOrder() {
        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(1)
                .totalPrice(new BigDecimal("60.00"))
                .extras(new ArrayList<>())
                .sides(new ArrayList<>())
                .build();
        Order order = Order.builder()
                .customer(customer)
                .deliveryBlock(Block.A)
                .deliveryRoomNumber("101")
                .orderItems(List.of(item))
                .build();
        item.setOrder(order);
        Order saved = orderService.create(order);

        Order fetched = orderService.read(saved.getId());

        assertNotNull(fetched);
        assertEquals(saved.getId(), fetched.getId());
        assertEquals(Block.A, fetched.getDeliveryBlock());
    }

    @Test
    @DisplayName("PASS – updateStatus() transitions order from PROCESSING to ON_THE_WAY")
    void updateStatus_processingToOnTheWay() {
        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(1)
                .totalPrice(new BigDecimal("60.00"))
                .extras(new ArrayList<>())
                .sides(new ArrayList<>())
                .build();
        Order order = Order.builder()
                .customer(customer)
                .deliveryBlock(Block.C)
                .deliveryRoomNumber("301")
                .orderItems(List.of(item))
                .build();
        item.setOrder(order);
        Order saved = orderService.create(order);

        Order updated = orderService.updateStatus(saved.getId(), OrderStatus.ON_THE_WAY);

        assertEquals(OrderStatus.ON_THE_WAY, updated.getStatus());
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – create() when business is closed expects no exception (intentionally wrong)")
    void create_whenBusinessClosed_expectsNoException_fail() {
        businessStatusService.closeBusiness(admin, "Closed", null);

        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(1)
                .totalPrice(new BigDecimal("60.00"))
                .extras(new ArrayList<>())
                .sides(new ArrayList<>())
                .build();
        Order order = Order.builder()
                .customer(customer)
                .deliveryBlock(Block.B)
                .deliveryRoomNumber("201")
                .orderItems(List.of(item))
                .build();
        item.setOrder(order);

        // ❌ OrderServiceImpl throws IllegalStateException when business is closed
        assertDoesNotThrow(() -> orderService.create(order),
                "Intentionally wrong — business is closed; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – updateStatus() from PROCESSING directly to DELIVERED expects success (intentionally wrong)")
    void updateStatus_skipStep_expectsSuccess_fail() {
        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(1)
                .totalPrice(new BigDecimal("60.00"))
                .extras(new ArrayList<>())
                .sides(new ArrayList<>())
                .build();
        Order order = Order.builder()
                .customer(customer)
                .deliveryBlock(Block.B)
                .deliveryRoomNumber("201")
                .orderItems(List.of(item))
                .build();
        item.setOrder(order);
        Order saved = orderService.create(order);

        // ❌ Must go PROCESSING → ON_THE_WAY first; skipping to DELIVERED throws
        assertDoesNotThrow(() -> orderService.updateStatus(saved.getId(), OrderStatus.DELIVERED),
                "Intentionally wrong — workflow enforces PROCESSING → ON_THE_WAY; this should FAIL");
    }
}
