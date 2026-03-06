package com.llburgers.service;

import com.llburgers.domain.Customer;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.Role;
import com.llburgers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CustomerServiceImplTest {

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        savedCustomer = customerRepository.save(Customer.builder()
                .name("Test Customer")
                .email("customer." + UUID.randomUUID() + "@llburgers.com")
                .password("customerPass1")
                .role(Role.CUSTOMER)
                .block(Block.A)
                .roomNumber("101")
                .active(true)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() saves a customer and returns it with a generated ID")
    void create_persistsCustomerWithGeneratedId() {
        Customer c = Customer.builder()
                .name("New Customer")
                .email("new.customer." + UUID.randomUUID() + "@llburgers.com")
                .password("newPass99")
                .role(Role.CUSTOMER)
                .block(Block.B)
                .roomNumber("202")
                .active(true)
                .build();

        Customer created = customerService.create(c);

        assertNotNull(created.getId());
        assertTrue(customerRepository.existsById(created.getId()));
        assertEquals(Block.B, created.getBlock());
        assertEquals("202", created.getRoomNumber());
    }

    @Test
    @DisplayName("PASS – read() returns the correct customer from the database")
    void read_returnsCorrectCustomer() {
        Customer fetched = customerService.read(savedCustomer.getId());

        assertNotNull(fetched);
        assertEquals(savedCustomer.getId(), fetched.getId());
        assertEquals(savedCustomer.getEmail(), fetched.getEmail());
        assertEquals(Block.A, fetched.getBlock());
    }

    @Test
    @DisplayName("PASS – findByEmail() returns the customer matching the given email")
    void findByEmail_returnsMatchingCustomer() {
        Customer fetched = customerService.findByEmail(savedCustomer.getEmail());

        assertEquals(savedCustomer.getId(), fetched.getId());
        assertEquals(savedCustomer.getEmail(), fetched.getEmail());
    }

    @Test
    @DisplayName("PASS – deactivate() persists active=false to the database")
    void deactivate_persistsActiveFalse() {
        Customer deactivated = customerService.deactivate(savedCustomer.getId());

        assertFalse(deactivated.isActive());
        Customer fromDb = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertFalse(fromDb.isActive());
    }

    @Test
    @DisplayName("PASS – updateDeliveryDetails() persists new block and room number")
    void updateDeliveryDetails_persistsNewBlockAndRoom() {
        Customer updated = customerService.updateDeliveryDetails(savedCustomer.getId(), Block.C, "305");

        assertEquals(Block.C, updated.getBlock());
        assertEquals("305", updated.getRoomNumber());

        Customer fromDb = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertEquals(Block.C, fromDb.getBlock());
        assertEquals("305", fromDb.getRoomNumber());
    }

    @Test
    @DisplayName("PASS – findByBlock() only returns customers in the requested block")
    void findByBlock_returnsOnlyCustomersInBlock() {
        List<Customer> blockACustomers = customerService.findByBlock(Block.A);

        assertTrue(blockACustomers.stream().allMatch(c -> c.getBlock() == Block.A));
        assertTrue(blockACustomers.stream().anyMatch(c -> c.getId().equals(savedCustomer.getId())));
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – findByEmail() with unknown email expects no exception (intentionally wrong)")
    void findByEmail_unknownEmail_expectsNoException_fail() {
        // ❌ CustomerServiceImpl throws IllegalArgumentException for unknown emails
        assertDoesNotThrow(
                () -> customerService.findByEmail("ghost@nowhere.com"),
                "Intentionally wrong — should FAIL"
        );
    }

    @Test
    @DisplayName("FAIL – deactivate() asserts customer is still active after deactivation (intentionally wrong)")
    void deactivate_assertsStillActive_fail() {
        customerService.deactivate(savedCustomer.getId());
        Customer fromDb = customerRepository.findById(savedCustomer.getId()).orElseThrow();

        // ❌ Customer was deactivated — active is false, but assertion checks for true
        assertTrue(fromDb.isActive(),
                "Intentionally wrong assertion — customer was deactivated so this should FAIL");
    }
}
