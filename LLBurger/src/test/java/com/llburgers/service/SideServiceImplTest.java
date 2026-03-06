package com.llburgers.service;

import com.llburgers.domain.Side;
import com.llburgers.repository.SideRepository;
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
class SideServiceImplTest {

    @Autowired
    private ISideService sideService;

    @Autowired
    private SideRepository sideRepository;

    private Side savedSide;

    @BeforeEach
    void setUp() {
        savedSide = sideRepository.save(Side.builder()
                .name("Test Chips " + UUID.randomUUID())
                .price(new BigDecimal("20.00"))
                .stockQuantity(30)
                .availability(true)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists a side and generates an ID")
    void create_persistsSideWithGeneratedId() {
        Side side = Side.builder()
                .name("Onion Rings " + UUID.randomUUID())
                .price(new BigDecimal("25.00"))
                .stockQuantity(15)
                .availability(true)
                .build();

        Side created = sideService.create(side);

        assertNotNull(created.getId());
        assertTrue(sideRepository.existsById(created.getId()));
        assertEquals(new BigDecimal("25.00"), created.getPrice());
    }

    @Test
    @DisplayName("PASS – read() returns the correct side from the database")
    void read_returnsCorrectSide() {
        Side fetched = sideService.read(savedSide.getId());

        assertNotNull(fetched);
        assertEquals(savedSide.getId(), fetched.getId());
        assertEquals(savedSide.getName(), fetched.getName());
    }

    @Test
    @DisplayName("PASS – updateStock() to 0 sets availability false; to >0 sets it true")
    void updateStock_togglesAvailabilityCorrectly() {
        Side outOfStock = sideService.updateStock(savedSide.getId(), 0);
        assertFalse(outOfStock.isAvailability());
        assertEquals(0, outOfStock.getStockQuantity());

        Side restocked = sideService.updateStock(savedSide.getId(), 12);
        assertTrue(restocked.isAvailability());
        assertEquals(12, restocked.getStockQuantity());
    }

    @Test
    @DisplayName("PASS – markAvailable() sets availability to true")
    void markAvailable_setsAvailabilityTrue() {
        sideService.updateStock(savedSide.getId(), 0); // force unavailable
        Side result = sideService.markAvailable(savedSide.getId());

        assertTrue(result.isAvailability());
    }

    @Test
    @DisplayName("PASS – findByAvailability(true) only returns available sides")
    void findByAvailability_returnsOnlyAvailableSides() {
        List<Side> available = sideService.findByAvailability(true);

        assertTrue(available.stream().allMatch(Side::isAvailability));
        assertTrue(available.stream().anyMatch(s -> s.getId().equals(savedSide.getId())));
    }

    @Test
    @DisplayName("PASS – reduceStock() correctly decrements the stock quantity")
    void reduceStock_decrementsStock() {
        sideService.updateStock(savedSide.getId(), 10);

        Side result = sideService.reduceStock(savedSide.getId(), 3);

        assertEquals(7, result.getStockQuantity());
    }

    // ─── FAILING TEST ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        // ❌ SideServiceImpl throws for unknown IDs
        assertDoesNotThrow(
                () -> sideService.read(UUID.randomUUID()),
                "Intentionally wrong — should FAIL"
        );
    }

    @Test
    @DisplayName("FAIL – asserts wrong price on fetched side (intentionally wrong)")
    void read_assertsWrongPrice_fail() {
        Side fetched = sideService.read(savedSide.getId());

        // ❌ Actual price is 20.00, not 99.99
        assertEquals(new BigDecimal("99.99"), fetched.getPrice(),
                "Intentionally wrong assertion — this test should FAIL");
    }
}
