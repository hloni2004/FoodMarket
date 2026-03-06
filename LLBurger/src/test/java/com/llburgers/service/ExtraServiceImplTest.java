package com.llburgers.service;

import com.llburgers.domain.Extra;
import com.llburgers.repository.ExtraRepository;
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
class ExtraServiceImplTest {

    @Autowired
    private IExtraService extraService;

    @Autowired
    private ExtraRepository extraRepository;

    private Extra savedExtra;

    @BeforeEach
    void setUp() {
        savedExtra = extraRepository.save(Extra.builder()
                .name("Test Cheese " + UUID.randomUUID())
                .price(new BigDecimal("8.00"))
                .stockQuantity(50)
                .availability(true)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists an extra and generates an ID")
    void create_persistsExtraWithGeneratedId() {
        Extra extra = Extra.builder()
                .name("Extra Bacon " + UUID.randomUUID())
                .price(new BigDecimal("12.00"))
                .stockQuantity(25)
                .availability(true)
                .build();

        Extra created = extraService.create(extra);

        assertNotNull(created.getId());
        assertTrue(extraRepository.existsById(created.getId()));
        assertEquals(new BigDecimal("12.00"), created.getPrice());
        assertTrue(created.isAvailability());
    }

    @Test
    @DisplayName("PASS – read() returns the correct extra from the database")
    void read_returnsCorrectExtra() {
        Extra fetched = extraService.read(savedExtra.getId());

        assertNotNull(fetched);
        assertEquals(savedExtra.getId(), fetched.getId());
        assertEquals(savedExtra.getName(), fetched.getName());
        assertEquals(0, savedExtra.getPrice().compareTo(fetched.getPrice()));
    }

    @Test
    @DisplayName("PASS – existsByName() returns true for a name already in the database")
    void existsByName_returnsTrueForExistingName() {
        boolean exists = extraService.existsByName(savedExtra.getName());

        assertTrue(exists);
    }

    @Test
    @DisplayName("PASS – updateStock() sets quantity and flips availability correctly")
    void updateStock_setsQuantityAndAvailability() {
        Extra outOfStock = extraService.updateStock(savedExtra.getId(), 0);
        assertEquals(0, outOfStock.getStockQuantity());
        assertFalse(outOfStock.isAvailability());

        Extra restocked = extraService.updateStock(savedExtra.getId(), 20);
        assertEquals(20, restocked.getStockQuantity());
        assertTrue(restocked.isAvailability());
    }

    @Test
    @DisplayName("PASS – markUnavailable() sets availability to false in the database")
    void markUnavailable_setsAvailabilityFalse() {
        Extra result = extraService.markUnavailable(savedExtra.getId());

        assertFalse(result.isAvailability());
        Extra fromDb = extraRepository.findById(savedExtra.getId()).orElseThrow();
        assertFalse(fromDb.isAvailability());
    }

    @Test
    @DisplayName("PASS – findByAvailability(false) only returns unavailable extras")
    void findByAvailability_returnsOnlyUnavailableExtras() {
        extraService.markUnavailable(savedExtra.getId());

        List<Extra> unavailable = extraService.findByAvailability(false);

        assertTrue(unavailable.stream().noneMatch(Extra::isAvailability));
        assertTrue(unavailable.stream().anyMatch(e -> e.getId().equals(savedExtra.getId())));
    }

    @Test
    @DisplayName("PASS – reduceStock() correctly decrements the stock quantity")
    void reduceStock_decrementsStock() {
        extraService.updateStock(savedExtra.getId(), 10);

        Extra result = extraService.reduceStock(savedExtra.getId(), 6);

        assertEquals(4, result.getStockQuantity());
    }

    // ─── FAILING TEST ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – existsByName() returns true for a name that does not exist (intentionally wrong)")
    void existsByName_nonExistentName_assertsTrue_fail() {
        boolean exists = extraService.existsByName("this-extra-does-not-exist-" + UUID.randomUUID());

        // ❌ The name doesn't exist in the DB, existsByName returns false
        assertTrue(exists,
                "Intentionally wrong assertion — existsByName returns false for this name, this should FAIL");
    }

    @Test
    @DisplayName("FAIL – reduceStock() expects success when requesting more than available (intentionally wrong)")
    void reduceStock_exceedsStock_expectsSuccess_fail() {
        extraService.updateStock(savedExtra.getId(), 2);

        // ❌ Requesting 10 when only 2 in stock — throws IllegalArgumentException
        assertDoesNotThrow(
                () -> extraService.reduceStock(savedExtra.getId(), 10),
                "Intentionally wrong — ExtraServiceImpl throws for insufficient stock, this should FAIL"
        );
    }
}
