# Regression Test Report â€” TechLoan (IT345)

**Project:** Rentuma TechLoan System  
**Course:** IT345 â€“ Advanced Software Engineering  
**Date Executed:** 2026-05-09  
**Executed By:** trixieann30  
**Branch:** feature/vertical-slice-refactoring  
**Test Framework:** JUnit 5 + Spring Boot Test + MockMvc + H2 (in-memory)  
**Build Tool:** Maven 3.9.6  

---

## 1. Executive Summary

| Metric | Value |
|--------|-------|
| Total Test Cases Executed | 52 |
| Passed | 52 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 0 |
| Overall Result | **PASS** |
| Total Execution Time | ~28 s |

All 52 automated regression tests passed on a clean run against the H2 in-memory database under the `test` Spring profile. No failures, no errors, no skipped tests.

---

## 2. Test Environment

| Item | Detail |
|------|--------|
| Java | 21 (Eclipse Temurin) |
| Spring Boot | 3.3.5 |
| Test Database | H2 2.x (in-memory, `jdbc:h2:mem:testdb`) |
| JPA DDL | `create-drop` (schema rebuilt per test run) |
| Active Profile | `test` |
| Scheduling | Disabled via `@Profile("!test")` on `SchedulingConfig` |
| Inventory Seeder | Disabled via `@Profile("!test")` on `InventoryInitializer` |
| JWT Secret | Same key as production (`app.jwt.secret`) |
| Google OAuth | Bypassed (not exercised in automated tests) |

---

## 3. Test Suite Results by Class

### 3.1 AuthControllerTest â€” 8 tests | 9.8 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-AUTH-001 | Register valid student returns 201 + token | PASS | Token present in response |
| TC-AUTH-002 | Register duplicate email returns 409 | PASS | `IllegalStateException` â†’ 409 |
| TC-AUTH-003 | Register missing required fields returns 400 | PASS | Bean validation triggers |
| TC-AUTH-004 | Login valid credentials returns 200 + token | PASS | Token present in response |
| TC-AUTH-005 | Login wrong password returns 401 | PASS | `BadCredentialsException` â†’ 401 |
| TC-AUTH-006 | Login non-existent email returns 401 | PASS | `BadCredentialsException` â†’ 401 |
| TC-AUTH-007 | GET /auth/me with valid JWT returns 200 | PASS | Email returned in body |
| TC-AUTH-008 | GET /auth/me without JWT returns 4xx | PASS | Spring Security filter rejects |

### 3.2 InventoryControllerTest â€” 8 tests | 2.96 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-INV-001 | GET /inventory/available returns only available items | PASS | Unavailable item excluded |
| TC-INV-002 | GET /inventory/all returns all items | PASS | Both available and unavailable |
| TC-INV-003 | GET /inventory/{id} returns item when found | PASS | Correct item returned |
| TC-INV-004 | GET /inventory/{id} returns 404 when not found | PASS | 404 status confirmed |
| TC-INV-005 | Custodian POST /inventory creates item (201) | PASS | Item code auto-generated |
| TC-INV-006 | Student POST /inventory returns 403 | PASS | RBAC enforced |
| TC-INV-007 | Custodian PUT /inventory/{id} updates item (200) | PASS | Name updated correctly |
| TC-INV-008 | Custodian DELETE /inventory/{id} returns 200 | PASS | Success message present |

### 3.3 BorrowControllerTest (Reservations) â€” 15 tests | 7.35 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-RES-001 | Student submits valid reservation â†’ 201 PENDING | PASS | Status = PENDING |
| TC-RES-002 | Custodian submits reservation â†’ 403 | PASS | Business rule enforced |
| TC-RES-003 | Reservation for non-existent item â†’ 404 | PASS | BUSINESS-001 error code |
| TC-RES-004 | Reservation exceeds available quantity â†’ 409 | PASS | BUSINESS-001 error code |
| TC-RES-005 | Null inventoryId triggers validator â†’ 400 | PASS | Bean validation fires |
| TC-RES-006 | Past return date â†’ 400 | PASS | Bean `@Future` validation |
| TC-RES-007 | Custodian GET /reservations returns all | PASS | 2 records returned |
| TC-RES-008 | Student GET /reservations returns own only | PASS | Scoped by userId |
| TC-RES-009 | GET /reservations/{id} by owner â†’ 200 | PASS | Correct record returned |
| TC-RES-010 | GET /reservations/{id} by other student â†’ 403 | PASS | Ownership check enforced |
| TC-RES-011 | Custodian approves â†’ status APPROVED | PASS | Loan also created |
| TC-RES-012 | Custodian rejects â†’ status REJECTED | PASS | Status updated |
| TC-RES-013 | Custodian processes return â†’ status RETURNED | PASS | Inventory qty restored |
| TC-RES-014 | Custodian marks overdue â†’ status OVERDUE | PASS | Status updated |
| TC-RES-015 | Student attempts approve â†’ 403 | PASS | RBAC enforced |

### 3.4 LoanControllerTest â€” 7 tests | 4.54 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-LOAN-001 | Custodian GET /loans returns all loans | PASS | All 2 loans returned |
| TC-LOAN-002 | Student GET /loans returns own loans only | PASS | Scoped by userId |
| TC-LOAN-003 | GET /loans?isOverdue=true returns only overdue | PASS | Filter works correctly |
| TC-LOAN-004 | Custodian POST /loans/{id}/return â†’ 200 | PASS | returnedAt populated |
| TC-LOAN-005 | Student POST /loans/{id}/return â†’ 403 | PASS | RBAC enforced |
| TC-LOAN-006 | Approving reservation creates loan record | PASS | Loan found by reservationId |
| TC-LOAN-007 | Returning loan sets returnedAt, isOverdue=false | PASS | Both fields updated |

### 3.5 PenaltyCalculationServiceTest â€” 3 tests | 1.01 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-PEN-004 | Overdue loan generates penalty record | PASS | penaltyPoints = daysOverdue |
| TC-PEN-005 | Penalty capped at 30 points (45 days overdue) | PASS | Points = 30 |
| TC-PEN-006 | Non-overdue loan produces no penalty record | PASS | No row inserted |

### 3.6 PenaltyControllerTest â€” 4 tests | 2.23 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-PEN-001 | Custodian GET /users/{id}/penalties â†’ 200 | PASS | Summary and list returned |
| TC-PEN-002 | Student GET own penalties â†’ 200 | PASS | Own record accessible |
| TC-PEN-003 | Student GET other user penalties â†’ 403 | PASS | Ownership check enforced |
| TC-PEN-007 | totalPoints sums only unpaid penalties | PASS | Paid penalty excluded |

### 3.7 BorrowRequestValidatorTest (Chain of Responsibility) â€” 4 tests | 0.007 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-DP-001 | ItemNameValidator rejects null inventoryId | PASS | ValidationException thrown |
| TC-DP-002 | DueDateValidator rejects past return date | PASS | ValidationException thrown |
| TC-DP-003 | DueDateValidator rejects date > 6 months | PASS | ValidationException thrown |
| â€”       | Valid request passes full chain without throwing | PASS | No exception |

### 3.8 DTOFactoryTest (Factory Pattern) â€” 3 tests | 0.002 s

| TC ID | Test Name | Result | Notes |
|-------|-----------|--------|-------|
| TC-DP-004 | toBorrowRequestDTO maps all fields correctly | PASS | All 7 fields verified |
| TC-DP-005 | toLoanDTO maps fields and calculates daysOverdue | PASS | daysOverdue â‰¥ 3 confirmed |
| TC-DP-005b | toPenaltyDTO maps all Penalty fields | PASS | All 7 fields verified |

---

## 4. Defects Found

No defects were found during automated testing. One configuration issue was identified and resolved during test development:

| ID | Issue | Resolution |
|----|-------|------------|
| FIX-001 | `PenaltySummaryDTO` field named `totalPoints`, not `totalUnpaidPoints` | Test assertion corrected to match actual DTO |
| FIX-002 | `SchedulingConfig` and `InventoryInitializer` ran during tests, polluting state | Added `@Profile("!test")` to both classes |

---

## 5. Design Pattern Coverage

| Pattern | Class Under Test | TC IDs | Result |
|---------|-----------------|--------|--------|
| Chain of Responsibility | `ItemNameValidator`, `DueDateValidator` | TC-DP-001, TC-DP-002, TC-DP-003 | PASS |
| Factory | `DTOFactory` | TC-DP-004, TC-DP-005, TC-DP-005b | PASS |
| Facade | `AuthFacade` (via `AuthController`) | TC-AUTH-001 â€“ TC-AUTH-008 | PASS |
| Builder | `User.UserBuilder` (used in test setup) | All test setup | PASS |
| Observer | `BorrowEventPublisher` â†’ `AuditListener` | TC-RES-011, TC-RES-013 | PASS |
| Strategy | `StandardRegistrationValidator` | TC-AUTH-001, TC-AUTH-002 | PASS |

---

## 6. Exit Criteria Assessment

| Criterion | Status |
|-----------|--------|
| All 52 test cases executed | MET |
| Pass rate â‰¥ 95% | MET (100%) |
| Zero failed tests | MET |
| Zero test errors | MET |
| All feature areas covered (Auth, Inventory, Reservation, Loan, Penalty) | MET |
| All 6 design patterns exercised | MET |
| Build exits with `BUILD SUCCESS` | MET |

**Conclusion: All exit criteria met. The TechLoan backend passes regression testing.**

---

## 7. Maven Surefire Output (Abridged)

```
[INFO] Tests run:  8, Failures: 0, Errors: 0, Skipped: 0  -- AuthControllerTest          (9.8 s)
[INFO] Tests run:  8, Failures: 0, Errors: 0, Skipped: 0  -- InventoryControllerTest      (2.96 s)
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0  -- BorrowControllerTest         (7.35 s)
[INFO] Tests run:  7, Failures: 0, Errors: 0, Skipped: 0  -- LoanControllerTest           (4.54 s)
[INFO] Tests run:  3, Failures: 0, Errors: 0, Skipped: 0  -- PenaltyCalculationServiceTest (1.01 s)
[INFO] Tests run:  4, Failures: 0, Errors: 0, Skipped: 0  -- PenaltyControllerTest        (2.23 s)
[INFO] Tests run:  4, Failures: 0, Errors: 0, Skipped: 0  -- BorrowRequestValidatorTest   (0.007 s)
[INFO] Tests run:  3, Failures: 0, Errors: 0, Skipped: 0  -- DTOFactoryTest               (0.002 s)
[INFO] -------------------------------------------------------
[INFO] Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
