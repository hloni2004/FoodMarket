package com.llburgers.service;

import com.llburgers.domain.Product;
import com.llburgers.domain.enums.ProductCategory;
import com.llburgers.repository.ProductRepository;
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
class ProductServiceImplTest {

    @Autowired
    private IProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedProduct = productRepository.save(Product.builder()
                .name("Test Burger " + UUID.randomUUID())
                .price(new BigDecimal("55.00"))
                .category(ProductCategory.BURGER)
                .stockQuantity(20)
                .availability(true)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists a product and generates an ID")
    void create_persistsProductWithGeneratedId() {
        Product p = Product.builder()
                .name("New Burger " + UUID.randomUUID())
                .price(new BigDecimal("65.00"))
                .category(ProductCategory.BURGER)
                .stockQuantity(10)
                .availability(true)
                .build();

        Product created = productService.create(p);

        assertNotNull(created.getId());
        assertTrue(productRepository.existsById(created.getId()));
        assertEquals(ProductCategory.BURGER, created.getCategory());
        assertTrue(created.isAvailability());
    }

    @Test
    @DisplayName("PASS – read() returns the correct product from the database")
    void read_returnsCorrectProduct() {
        Product fetched = productService.read(savedProduct.getId());

        assertNotNull(fetched);
        assertEquals(savedProduct.getId(), fetched.getId());
        assertEquals(savedProduct.getName(), fetched.getName());
        assertEquals(0, savedProduct.getPrice().compareTo(fetched.getPrice()));
    }

    @Test
    @DisplayName("PASS – updateStock() sets quantity and flips availability correctly")
    void updateStock_setsQuantityAndAvailability() {
        Product updated = productService.updateStock(savedProduct.getId(), 0);

        assertEquals(0, updated.getStockQuantity());
        assertFalse(updated.isAvailability(), "availability must be false when stock=0");

        Product restocked = productService.updateStock(savedProduct.getId(), 15);
        assertEquals(15, restocked.getStockQuantity());
        assertTrue(restocked.isAvailability(), "availability must be true when stock>0");
    }

    @Test
    @DisplayName("PASS – reduceStock() decrements stock by the requested amount")
    void reduceStock_decrementsStock() {
        productService.updateStock(savedProduct.getId(), 10);

        Product result = productService.reduceStock(savedProduct.getId(), 4);

        assertEquals(6, result.getStockQuantity());
    }

    @Test
    @DisplayName("PASS – markUnavailable() sets availability to false")
    void markUnavailable_setsAvailabilityFalse() {
        Product result = productService.markUnavailable(savedProduct.getId());

        assertFalse(result.isAvailability());
    }

    @Test
    @DisplayName("PASS – findByCategory() only returns products of the requested category")
    void findByCategory_returnsOnlyMatchingCategory() {
        List<Product> burgers = productService.findByCategory(ProductCategory.BURGER);

        assertTrue(burgers.stream().allMatch(p -> p.getCategory() == ProductCategory.BURGER));
        assertTrue(burgers.stream().anyMatch(p -> p.getId().equals(savedProduct.getId())));
    }

    // ─── FAILING TEST ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        // ❌ ProductServiceImpl throws IllegalArgumentException for unknown IDs
        assertDoesNotThrow(
                () -> productService.read(UUID.randomUUID()),
                "Intentionally wrong — should FAIL"
        );
    }

    @Test
    @DisplayName("FAIL – reduceStock() expects zero remaining after removing more than available (intentionally wrong)")
    void reduceStock_exceedsStock_expectsZero_fail() {
        productService.updateStock(savedProduct.getId(), 3);

        // ❌ Requesting 10 when only 3 available — service throws, not returns 0
        assertDoesNotThrow(
                () -> productService.reduceStock(savedProduct.getId(), 10),
                "Intentionally wrong — service throws IllegalArgumentException, this FAIL is expected"
        );
    }
}
