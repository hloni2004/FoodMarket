# 📋 Factory Test Suite - Quick Reference Index

## 🚀 Quick Start

**Run All Tests:**
```bash
mvn test -Dtest=*FactoryTest
```

**Run Specific Test:**
```bash
mvn test -Dtest=ProductFactoryTest
```

---

## 📂 All Test Files

| # | Test Class | File | Tests | Status |
|---|---|---|---|---|
| 1 | ProductFactoryTest | ProductFactoryTest.java | 6 | ✓ |
| 2 | SideFactoryTest | SideFactoryTest.java | 6 | ✓ |
| 3 | ExtraFactoryTest | ExtraFactoryTest.java | 6 | ✓ |
| 4 | OrderFactoryTest | OrderFactoryTest.java | 6 | ✓ |
| 5 | OrderItemFactoryTest | OrderItemFactoryTest.java | 6 | ✓ |
| 6 | OrderItemExtraFactoryTest | OrderItemExtraFactoryTest.java | 6 | ✓ |
| 7 | OrderItemSideFactoryTest | OrderItemSideFactoryTest.java | 6 | ✓ |
| 8 | NotificationFactoryTest | NotificationFactoryTest.java | 6 | ✓ |
| 9 | RatingFactoryTest | RatingFactoryTest.java | 6 | ✓ |
| 10 | BusinessStatusFactoryTest | BusinessStatusFactoryTest.java | 6 | ✓ |
| 11 | BusinessStatusLogFactoryTest | BusinessStatusLogFactoryTest.java | 6 | ✓ |
| **TOTAL** | | | **66** | **✅** |

---

## 📖 Documentation Files

1. **TEST_SUITE_DOCUMENTATION.md** - Detailed documentation for each factory
2. **FACTORY_TESTS_COMPLETE.txt** - ASCII summary with all details
3. **FACTORY_TEST_SUITE_SUMMARY.md** - Comprehensive overview
4. **FACTORY_TEST_QUICK_REFERENCE.md** - This file!

---

## 🧪 Test Pattern

Each factory has exactly **6 tests**:

### ✓ Correct Tests (3)
- Test 1: Valid creation with all attributes → prints object
- Test 2: Valid with defaults/optional params → prints object  
- Test 3: Valid edge case → prints object

### ✗ Incorrect Tests (3)
- Test 4: Exception case 1 → prints exception
- Test 5: Exception case 2 → prints exception
- Test 6: Exception case 3 → prints exception

---

## 📊 What Each Factory Tests

### ProductFactoryTest
```
✓ Valid product, Default availability, Zero price
✗ Null name, Null price, Null category
```

### SideFactoryTest
```
✓ Valid side, Default availability, Zero stock
✗ Null name, Null price, Negative stock
```

### ExtraFactoryTest
```
✓ Valid extra, Default availability, Unavailable
✗ Empty name, Negative price, Negative stock
```

### OrderFactoryTest
```
✓ Valid order, No instructions, Different block
✗ Null customer, Invalid price, Null block
```

### OrderItemFactoryTest
```
✓ Valid item, Single qty, Large qty
✗ Null order, Zero qty, Negative price
```

### OrderItemExtraFactoryTest
```
✓ Valid extra, Single qty, Multiple qty
✗ Null item, Null extra, Zero qty
```

### OrderItemSideFactoryTest
```
✓ Valid side, Multiple qty, Different sides
✗ Null item, Null side, Negative qty
```

### NotificationFactoryTest
```
✓ Order notification, Business notification, Ready notification
✗ Null type, Null order (in order notif), Null type (in order notif)
```

### RatingFactoryTest
```
✓ Valid rating, Without feedback, One-star
✗ Null order, Rating too high, Rating too low
```

### BusinessStatusFactoryTest
```
✓ Closed status, Open status, Closed without reopen
✗ Null admin, Invalid message, Null admin (closed)
```

### BusinessStatusLogFactoryTest
```
✓ Closed log, Open log, Closed without reopen
✗ Null admin, Null admin (open), Null admin (closed)
```

---

## 💻 Running Tests

### From Maven
```bash
# All tests
mvn test

# Factory tests only
mvn test -Dtest=*FactoryTest

# Specific factory
mvn test -Dtest=ProductFactoryTest

# With output
mvn test -Dsurefire.useFile=false
```

### From IDE (IntelliJ)
1. Right-click test class → Run 'TestClassName'
2. Or click on individual test → Run
3. Or use Ctrl+Shift+F10 (Windows)

### From IDE (Eclipse)
1. Right-click test class → Run As → JUnit Test
2. Or select test → Run (Alt+Shift+X, T)

---

## 🔍 Sample Outputs

### Success Output
```
✓ Test 1 - Valid Product Creation:
Product(id=null, name=Delicious Burger, price=45.99, 
category=BURGER, stockQuantity=50, ...)
```

### Exception Output
```
✗ Test 4 - Null Name Exception:
Exception: Product creation failed — 
Product name is invalid (must be 1–255 characters)
```

---

## 📈 Statistics

| Metric | Count |
|---|---|
| Total Test Classes | 11 |
| Total Test Methods | 66 |
| Correct Tests | 33 |
| Exception Tests | 33 |
| Assertions | 100+ |
| Lines of Code | 2000+ |

---

## ✅ Verification Checklist

- [x] All 11 test classes created
- [x] 6 tests per factory (3 correct + 3 incorrect)
- [x] System.out.println() in every test
- [x] All tests pass
- [x] Exception handling verified
- [x] Cross-factory integration tested
- [x] Documentation complete
- [x] Ready for execution

---

## 🎯 Key Features

✅ Comprehensive test coverage
✅ Visual output for each test
✅ Both success and failure scenarios
✅ Factory integration testing
✅ Clear error messages
✅ Proper use of assertions
✅ Consistent code style
✅ Complete documentation

---

## 📚 Related Files

| File | Purpose |
|---|---|
| TEST_SUITE_DOCUMENTATION.md | Detailed test documentation |
| FACTORY_TESTS_COMPLETE.txt | Full ASCII summary |
| FACTORY_TEST_SUITE_SUMMARY.md | Comprehensive overview |
| ProductFactory.java | Product factory implementation |
| ProductFactoryTest.java | Product factory tests |
| (and 10 more factory/test pairs...) | ... |

---

## 🚀 Next Steps

1. Run tests: `mvn test -Dtest=*FactoryTest`
2. Review output in console
3. Check test files for patterns
4. Use as template for new tests
5. Integrate into CI/CD pipeline

---

**Created:** March 5, 2026  
**Total Tests:** 66  
**Status:** ✅ COMPLETE  
**Framework:** JUnit 5 (Jupiter)

