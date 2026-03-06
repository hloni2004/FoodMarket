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
class RatingServiceImplTest {

    @Autowired private IRatingService ratingService;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;

    private Order savedOrder;
    private Order savedOrder2;

    @BeforeEach
    void setUp() {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Rating Customer")
                .email("rating.cust." + UUID.randomUUID() + "@llburgers.com")
                .password("ratPass99")
                .role(Role.CUSTOMER)
                .block(Block.A)
                .roomNumber("105")
                .active(true)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Rating Burger " + UUID.randomUUID())
                .price(new BigDecimal("55.00"))
                .category(ProductCategory.BURGER)
                .stockQuantity(10)
                .availability(true)
                .build());

        // Save orders directly to bypass order workflow for rating test setup
        savedOrder = orderRepository.save(Order.builder()
                .customer(customer)
                .totalPrice(new BigDecimal("55.00"))
                .status(OrderStatus.DELIVERED)
                .deliveryBlock(Block.A)
                .deliveryRoomNumber("105")
                .build());

        savedOrder2 = orderRepository.save(Order.builder()
                .customer(customer)
                .totalPrice(new BigDecimal("55.00"))
                .status(OrderStatus.DELIVERED)
                .deliveryBlock(Block.A)
                .deliveryRoomNumber("105")
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() saves a rating for an order with a generated ID")
    void create_persistsRatingWithGeneratedId() {
        Rating rating = Rating.builder()
                .order(savedOrder)
                .foodRating(5)
                .deliveryRating(4)
                .feedback("Great burger!")
                .build();

        Rating created = ratingService.create(rating);

        assertNotNull(created.getId());
        assertTrue(ratingRepository.existsById(created.getId()));
        assertEquals(5, created.getFoodRating());
        assertEquals(4, created.getDeliveryRating());
    }

    @Test
    @DisplayName("PASS – existsByOrderId() returns true after a rating is created")
    void existsByOrderId_returnsTrueAfterCreate() {
        ratingRepository.save(Rating.builder()
                .order(savedOrder)
                .foodRating(4)
                .deliveryRating(3)
                .build());

        assertTrue(ratingService.existsByOrderId(savedOrder.getId()));
        assertFalse(ratingService.existsByOrderId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("PASS – findByOrderId() returns the rating for the given order")
    void findByOrderId_returnsCorrectRating() {
        Rating saved = ratingRepository.save(Rating.builder()
                .order(savedOrder)
                .foodRating(3)
                .deliveryRating(5)
                .feedback("Fast delivery!")
                .build());

        Rating fetched = ratingService.findByOrderId(savedOrder.getId());

        assertEquals(saved.getId(), fetched.getId());
        assertEquals(3, fetched.getFoodRating());
    }

    @Test
    @DisplayName("PASS – findByFoodRating() returns only ratings with the given food score")
    void findByFoodRating_returnsMatchingRatings() {
        ratingRepository.save(Rating.builder().order(savedOrder).foodRating(5).deliveryRating(5).build());
        ratingRepository.save(Rating.builder().order(savedOrder2).foodRating(3).deliveryRating(3).build());

        List<Rating> fiveStars = ratingService.findByFoodRating(5);

        assertTrue(fiveStars.stream().allMatch(r -> r.getFoodRating() == 5));
    }

    @Test
    @DisplayName("PASS – getAverageFoodRating() returns a non-null average when ratings exist")
    void getAverageFoodRating_returnsNonNullAverage() {
        ratingRepository.save(Rating.builder().order(savedOrder).foodRating(4).deliveryRating(4).build());

        Double avg = ratingService.getAverageFoodRating();

        assertNotNull(avg);
        assertTrue(avg >= 1.0 && avg <= 5.0);
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – create() a second rating for the same order expects no exception (intentionally wrong)")
    void create_duplicateRatingForSameOrder_expectsNoException_fail() {
        ratingRepository.save(Rating.builder()
                .order(savedOrder)
                .foodRating(5)
                .deliveryRating(5)
                .build());

        // ❌ RatingServiceImpl throws IllegalStateException for duplicate rating
        assertDoesNotThrow(() -> ratingService.create(Rating.builder()
                        .order(savedOrder)
                        .foodRating(3)
                        .deliveryRating(3)
                        .build()),
                "Intentionally wrong — duplicate rating throws; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – findByOrderId() with unknown order ID expects no exception (intentionally wrong)")
    void findByOrderId_unknownOrderId_expectsNoException_fail() {
        assertDoesNotThrow(() -> ratingService.findByOrderId(UUID.randomUUID()),
                "Intentionally wrong — throws for unknown order; this should FAIL");
    }
}
