# Factory Test Suite Documentation

## Overview
Comprehensive test suite for all 11 factory classes in the FoodMarket project. Each factory has exactly **6 test cases**:
- **3 Correct Tests** - Valid inputs that successfully create objects with `System.out.println()` output
- **3 Incorrect Tests** - Invalid inputs that throw `IllegalArgumentException` with descriptive messages

All tests are designed to pass while demonstrating both success and failure scenarios.

---

## Test Files Created

### 1. ProductFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/ProductFactoryTest.java`

**Correct Tests:**
- ✓ Test 1: Valid Product Creation (with all attributes)
- ✓ Test 2: Product with Default Availability (null imageUrl)
- ✓ Test 3: Product with Zero Price (edge case)

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Name Exception
- ✗ Test 5: Null Price Exception
- ✗ Test 6: Null Category Exception

---

### 2. SideFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/SideFactoryTest.java`

**Correct Tests:**
- ✓ Test 1: Valid Side Creation (with all attributes)
- ✓ Test 2: Side with Default Availability
- ✓ Test 3: Side with Zero Stock

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Name Exception
- ✗ Test 5: Null Price Exception
- ✗ Test 6: Negative Stock Quantity Exception

---

### 3. ExtraFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/ExtraFactoryTest.java`

**Correct Tests:**
- ✓ Test 1: Valid Extra Creation
- ✓ Test 2: Extra with Default Availability
- ✓ Test 3: Unavailable Extra

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Empty Name Exception
- ✗ Test 5: Negative Price Exception
- ✗ Test 6: Negative Stock Exception

---

### 4. OrderFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/OrderFactoryTest.java`

**Setup:** Creates a Customer using UserFactory.createCustomer()

**Correct Tests:**
- ✓ Test 1: Valid Order Creation (with special instructions)
- ✓ Test 2: Order without Special Instructions
- ✓ Test 3: Order with Different Block (Block C)

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Customer Exception
- ✗ Test 5: Invalid Price Exception (zero price)
- ✗ Test 6: Null Delivery Block Exception

---

### 5. OrderItemFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/OrderItemFactoryTest.java`

**Setup:** Creates an Order and Product using respective factories

**Correct Tests:**
- ✓ Test 1: Valid OrderItem Creation (quantity 2)
- ✓ Test 2: OrderItem with Single Quantity
- ✓ Test 3: OrderItem with Large Quantity (10)

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Order Exception
- ✗ Test 5: Zero Quantity Exception
- ✗ Test 6: Invalid Total Price Exception (negative)

---

### 6. OrderItemExtraFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/OrderItemExtraFactoryTest.java`

**Setup:** Creates Order, Product, OrderItem, and Extra

**Correct Tests:**
- ✓ Test 1: Valid OrderItemExtra Creation (quantity 2)
- ✓ Test 2: OrderItemExtra with Single Quantity
- ✓ Test 3: OrderItemExtra with Multiple Quantity (3)

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null OrderItem Exception
- ✗ Test 5: Null Extra Exception
- ✗ Test 6: Zero Quantity Exception

---

### 7. OrderItemSideFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/OrderItemSideFactoryTest.java`

**Setup:** Creates Order, Product, OrderItem, and Side

**Correct Tests:**
- ✓ Test 1: Valid OrderItemSide Creation
- ✓ Test 2: OrderItemSide with Multiple Quantity (3)
- ✓ Test 3: OrderItemSide with Different Side

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null OrderItem Exception
- ✗ Test 5: Null Side Exception
- ✗ Test 6: Negative Quantity Exception

---

### 8. NotificationFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/NotificationFactoryTest.java`

**Setup:** Creates a Customer and Order

**Correct Tests:**
- ✓ Test 1: Valid Order Notification
- ✓ Test 2: Business Opened Notification (no order)
- ✓ Test 3: Order Ready Notification

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Notification Type Exception
- ✗ Test 5: Null Order in OrderNotification Exception
- ✗ Test 6: Null Type in OrderNotification Exception

---

### 9. RatingFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/RatingFactoryTest.java`

**Setup:** Creates a Customer and Order

**Correct Tests:**
- ✓ Test 1: Valid Rating Creation (5-star food, 4-star delivery with feedback)
- ✓ Test 2: Rating without Feedback
- ✓ Test 3: One Star Rating

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Order Exception
- ✗ Test 5: Food Rating Too High (6 stars) Exception
- ✗ Test 6: Delivery Rating Too Low (0 stars) Exception

---

### 10. BusinessStatusFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/BusinessStatusFactoryTest.java`

**Setup:** Creates an Admin using UserFactory.createAdmin()

**Correct Tests:**
- ✓ Test 1: Valid Closed Status (with reopen time)
- ✓ Test 2: Open Status
- ✓ Test 3: Closed Status without Reopen Time

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Admin Exception
- ✗ Test 5: Invalid Closed Message (whitespace) Exception
- ✗ Test 6: Null Admin in Closed Status Exception

---

### 11. BusinessStatusLogFactoryTest.java
**Location:** `src/test/java/com/llburgers/factory/BusinessStatusLogFactoryTest.java`

**Setup:** Creates an Admin using UserFactory.createAdmin()

**Correct Tests:**
- ✓ Test 1: Valid Closed Log Creation
- ✓ Test 2: Open Log
- ✓ Test 3: Closed Log without Reopen Time

**Incorrect Tests (All Pass with Exceptions):**
- ✗ Test 4: Null Admin Exception
- ✗ Test 5: Null Admin in Open Log Exception
- ✗ Test 6: Null Admin in Closed Log Exception

---

## Test Output Features

Each test includes **System.out.println()** statements that display:
- ✓ marker for correct tests (success cases)
- ✗ marker for incorrect tests (exception cases)
- Object toString() representation showing object properties
- Exception messages for error cases

### Example Output Format:
```
✓ Test 1 - Valid Product Creation:
Product(id=null, name=Delicious Burger, price=45.99, category=BURGER, ...)

✗ Test 4 - Null Name Exception:
Exception: Product creation failed — Product name is invalid (must be 1–255 characters)
```

---

## Running the Tests

### Run all factory tests:
```bash
mvn test -Dtest=*FactoryTest
```

### Run specific factory test:
```bash
mvn test -Dtest=ProductFactoryTest
```

### Run and capture output:
```bash
mvn test -Dtest=ProductFactoryTest -X
```

---

## Test Coverage Summary

| Factory Class | Total Tests | Correct | Incorrect | Status |
|---|---|---|---|---|
| ProductFactoryTest | 6 | 3 | 3 | ✓ Created |
| SideFactoryTest | 6 | 3 | 3 | ✓ Created |
| ExtraFactoryTest | 6 | 3 | 3 | ✓ Created |
| OrderFactoryTest | 6 | 3 | 3 | ✓ Created |
| OrderItemFactoryTest | 6 | 3 | 3 | ✓ Created |
| OrderItemExtraFactoryTest | 6 | 3 | 3 | ✓ Created |
| OrderItemSideFactoryTest | 6 | 3 | 3 | ✓ Created |
| NotificationFactoryTest | 6 | 3 | 3 | ✓ Created |
| RatingFactoryTest | 6 | 3 | 3 | ✓ Created |
| BusinessStatusFactoryTest | 6 | 3 | 3 | ✓ Created |
| BusinessStatusLogFactoryTest | 6 | 3 | 3 | ✓ Created |
| **TOTAL** | **66** | **33** | **33** | **✓ All Created** |

---

## Key Features

✅ **Comprehensive Coverage** - 3 correct + 3 incorrect tests per factory
✅ **Visual Output** - System.out.println() for each test showing object state
✅ **Exception Handling** - All incorrect tests verify proper exception throwing
✅ **Reusable Setup** - BeforeEach methods create test fixtures
✅ **Clear Test Names** - Descriptive names indicate what is being tested
✅ **Consistent Pattern** - All tests follow the same structure for easy navigation
✅ **All Tests Pass** - Correct tests verify success, incorrect tests verify exception throwing

---

**Created:** March 5, 2026
**Total Lines of Test Code:** 2,000+ lines
**Framework:** JUnit 5 (Jupiter)

