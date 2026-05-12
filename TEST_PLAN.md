# Software Test Plan
## TechLoan â€” Lab Equipment Borrowing System
**Course:** IT345 â€” Advanced Software Engineering  
**Project:** Rentuma TechLoan  
**Branch:** feature/vertical-slice-refactoring  
**Date:** May 2026  

---

## 1. Introduction

### 1.1 Purpose
This document defines the test strategy, test scope, test cases, and acceptance criteria for the TechLoan Lab Equipment Borrowing System. It covers the Spring Boot REST API backend following Vertical Slice Architecture (VSA), and serves as the basis for the automated regression test suite in Part 4.

### 1.2 System Overview
TechLoan is a multi-platform system comprising:
- **Backend:** Spring Boot 3.3.5 REST API (Java 17), PostgreSQL/H2, JWT authentication
- **Frontend:** React 18 + Vite + Tailwind CSS single-page application
- **Mobile:** Android (Kotlin) MVVM application

The system allows students and faculty to borrow lab equipment managed by custodians. Key workflows include registration, inventory browsing, reservation submission, custodian approval/rejection, return processing, and automatic overdue penalty calculation.

### 1.3 Design Patterns Under Test
The following patterns are explicitly verified by this test plan:

| Pattern | Implementation Location |
|---|---|
| Observer | `BorrowEventPublisher`, `AuditListener`, `PenaltyListener` |
| Factory | `DTOFactory` â€” produces BorrowRequestDTO, LoanDTO, PenaltyDTO |
| Facade | `AuthFacade` â€” wraps `AuthService` + `GoogleAuthService` |
| Chain of Responsibility | `BorrowRequestValidator` chain (ItemName â†’ DueDate â†’ Description) |
| Builder | `User.UserBuilder` |
| Strategy | `RegistrationValidator` â€” `StandardRegistrationValidator` vs `GoogleRegistrationValidator` |

---

## 2. Test Scope

### 2.1 In Scope
- All REST API endpoints exposed by the backend
- Authentication and authorization rules (role-based access: STUDENT, FACULTY, CUSTODIAN)
- Business rules: inventory availability, reservation lifecycle, loan creation, penalty calculation
- Design pattern behavior (Observer events, Chain of Responsibility validation, Factory output)
- Scheduled penalty recalculation logic

### 2.2 Out of Scope
- Frontend UI rendering (manual testing only)
- Mobile Android UI (manual testing only)
- Google OAuth token verification (mocked)
- Email/notification delivery
- Payment service endpoints (stub only in current implementation)
- Performance and load testing

---

## 3. Test Strategy

### 3.1 Test Levels

| Level | Approach | Tools |
|---|---|---|
| Unit | Individual service methods with mocked dependencies | JUnit 5, Mockito |
| Integration | Full Spring context, real HTTP requests, H2 in-memory DB | @SpringBootTest, MockMvc, H2 |
| Regression | Full suite re-run on every branch commit | Maven Surefire, JUnit 5 |

### 3.2 Test Types
- **Functional Testing** â€” verify each endpoint produces the correct HTTP status and response body
- **Negative Testing** â€” verify invalid inputs and unauthorized access are rejected with appropriate error codes
- **Boundary Testing** â€” penalty cap (30 pts), quantity limits, date boundaries
- **Pattern Verification Testing** â€” assert that Observer events fire, chain rejects invalid input at correct link, factory produces correct DTOs

### 3.3 Test Data Strategy
- H2 in-memory database is used for all automated tests; it is reset between test classes via `@Transactional` rollback or `@DirtiesContext`
- A `TestDataFactory` helper class seeds a CUSTODIAN user, a STUDENT user, and one inventory item before each relevant test
- JWT tokens are generated programmatically via `JwtUtil` â€” no real Google tokens needed

### 3.4 Pass/Fail Criteria

| Metric | Target |
|---|---|
| Test pass rate | 100% of defined test cases |
| HTTP status correctness | Must match expected code exactly |
| Response body fields | All required fields present and correct type |
| Security: unauthorized access | Must return 401 or 403, never 200 |
| Pattern behavior | Event fired / chain rejected / DTO fields match |

---

## 4. Test Environment

### 4.1 Backend Test Stack
| Component | Version |
|---|---|
| Java | 21 (Eclipse Temurin) |
| Spring Boot | 3.3.5 |
| JUnit | 5 (via spring-boot-starter-test) |
| MockMvc | Spring MVC Test |
| H2 Database | In-memory, reset per test class |
| Mockito | 5.x (via spring-boot-starter-test) |

### 4.2 Test Configuration
- **Profile:** `test` (`src/test/resources/application-test.properties`)
- **Database:** `spring.datasource.url=jdbc:h2:mem:techloan_test`
- **JPA DDL:** `spring.jpa.hibernate.ddl-auto=create-drop`
- **Security:** Full Spring Security context loaded; JWT filter active

---

## 5. Test Cases

> **Legend**  
> Priority: **H** = High Â· **M** = Medium Â· **L** = Low  
> Expected HTTP: code returned on the happy path  

---

### 5.1 Authentication â€” `/api/auth`

#### TC-AUTH-001 â€” Register with valid standard credentials
- **Priority:** H
- **Preconditions:** No user with the test email exists
- **Input:** `{ fullName, email: "student@cit.edu", studentId, password (â‰¥8 chars), confirmPassword, role: "STUDENT" }`
- **Steps:** POST `/api/auth/register`
- **Expected HTTP:** 201 Created
- **Expected Body:** `{ token (non-null), refreshToken (non-null), user.email, user.role: "STUDENT" }`

#### TC-AUTH-002 â€” Register with duplicate email
- **Priority:** H
- **Preconditions:** User with `student@cit.edu` already exists
- **Input:** Same email as existing user
- **Steps:** POST `/api/auth/register`
- **Expected HTTP:** 409 Conflict
- **Expected Body:** `{ error }` field present

#### TC-AUTH-003 â€” Register with non-institutional email
- **Priority:** H
- **Input:** `email: "student@gmail.com"`
- **Steps:** POST `/api/auth/register`
- **Expected HTTP:** 400 Bad Request

#### TC-AUTH-004 â€” Register with mismatched passwords
- **Priority:** M
- **Input:** `password: "pass1234"`, `confirmPassword: "pass5678"`
- **Expected HTTP:** 400 Bad Request

#### TC-AUTH-005 â€” Login with valid credentials
- **Priority:** H
- **Preconditions:** Registered STUDENT user exists
- **Input:** `{ email, password }`
- **Steps:** POST `/api/auth/login`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ token, refreshToken, user.fullName }`

#### TC-AUTH-006 â€” Login with wrong password
- **Priority:** H
- **Input:** Correct email, incorrect password
- **Steps:** POST `/api/auth/login`
- **Expected HTTP:** 401 Unauthorized

#### TC-AUTH-007 â€” Get current user with valid token
- **Priority:** H
- **Preconditions:** Valid JWT for a STUDENT
- **Steps:** GET `/api/auth/me` with `Authorization: Bearer <token>`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ id, fullName, email, role: "STUDENT", penaltyPoints }`

#### TC-AUTH-008 â€” Get current user without token
- **Priority:** H
- **Steps:** GET `/api/auth/me` with no Authorization header
- **Expected HTTP:** 403 Forbidden

---

### 5.2 Inventory â€” `/api/inventory`

#### TC-INV-001 â€” Get all inventory items (any authenticated user)
- **Priority:** H
- **Preconditions:** 3 seeded inventory items exist; authenticated as STUDENT
- **Steps:** GET `/api/inventory/all`
- **Expected HTTP:** 200 OK
- **Expected Body:** Array of 3 items, each with `{ id, name, category, availableQuantity, available }`

#### TC-INV-002 â€” Get available items only
- **Priority:** M
- **Preconditions:** 3 items total, 2 marked available
- **Steps:** GET `/api/inventory/available`
- **Expected HTTP:** 200 OK
- **Expected Body:** Array of 2 items; all have `"available": true`

#### TC-INV-003 â€” Create inventory item as CUSTODIAN
- **Priority:** H
- **Preconditions:** Authenticated as CUSTODIAN
- **Input:** `{ name: "Arduino Uno", category: "Arduino", condition: "Good", quantity: 5, description: "â€¦" }`
- **Steps:** POST `/api/inventory`
- **Expected HTTP:** 201 Created
- **Expected Body:** Item with `id`, `availableQuantity: 5`, `available: true`

#### TC-INV-004 â€” Create inventory item as STUDENT (forbidden)
- **Priority:** H
- **Preconditions:** Authenticated as STUDENT
- **Steps:** POST `/api/inventory` with valid body
- **Expected HTTP:** 403 Forbidden

#### TC-INV-005 â€” Update inventory item as CUSTODIAN
- **Priority:** M
- **Preconditions:** Item with id=1 exists; authenticated as CUSTODIAN
- **Input:** `{ name: "Arduino Uno R3", quantity: 10 }`
- **Steps:** PUT `/api/inventory/1`
- **Expected HTTP:** 200 OK
- **Expected Body:** Item with updated `name` and `totalQuantity: 10`

#### TC-INV-006 â€” Delete inventory item as CUSTODIAN
- **Priority:** M
- **Preconditions:** Item exists; authenticated as CUSTODIAN
- **Steps:** DELETE `/api/inventory/{id}`
- **Expected HTTP:** 204 No Content
- **Verification:** Subsequent GET `/api/inventory/{id}` returns 404

#### TC-INV-007 â€” Get items by category
- **Priority:** L
- **Preconditions:** 2 items with category "Laptop", 1 item with category "Camera"
- **Steps:** GET `/api/inventory/category/Laptop`
- **Expected HTTP:** 200 OK
- **Expected Body:** Array of 2 items, all with `"category": "Laptop"`

#### TC-INV-008 â€” Factory Pattern: DTOFactory maps InventoryItem fields correctly
- **Priority:** M
- **Verification:** `DTOFactory.toInventoryItemDTO(item)` returns DTO with all fields matching entity
- **Expected:** `name`, `category`, `condition`, `availableQuantity`, `totalQuantity`, `imageUrl` all present

---

### 5.3 Reservations â€” `/api/reservations`

#### TC-RES-001 â€” Create reservation with valid data (STUDENT)
- **Priority:** H
- **Preconditions:** Item with `availableQuantity â‰¥ 1` exists; authenticated as STUDENT
- **Input:** `{ inventoryId, quantity: 1, purpose: "Lab demo", returnDate: tomorrow }`
- **Steps:** POST `/api/reservations`
- **Expected HTTP:** 201 Created
- **Expected Body:** `{ id, status: "PENDING", itemName, userEmail }`

#### TC-RES-002 â€” Chain of Responsibility: reject reservation with empty item description context
- **Priority:** H
- **Preconditions:** `BorrowRequestValidator` chain is configured via `ValidatorConfig`
- **Input:** `{ inventoryId, quantity: 1, purpose: "", returnDate: tomorrow }`
- **Steps:** POST `/api/reservations`
- **Expected HTTP:** 400 Bad Request
- **Expected Body:** Error message from `DescriptionValidator` or standard validation

#### TC-RES-003 â€” Chain of Responsibility: reject reservation with past return date
- **Priority:** H
- **Input:** `{ returnDate: yesterday }`
- **Steps:** POST `/api/reservations`
- **Expected HTTP:** 400 Bad Request
- **Expected Body:** Error from `DueDateValidator`

#### TC-RES-004 â€” Create reservation for insufficient quantity
- **Priority:** H
- **Preconditions:** Item has `availableQuantity: 1`
- **Input:** `{ quantity: 5 }`
- **Expected HTTP:** 409 Conflict
- **Expected Body:** `{ error: "BUSINESS-001: Insufficient item availability" }`

#### TC-RES-005 â€” CUSTODIAN cannot submit reservation
- **Priority:** H
- **Preconditions:** Authenticated as CUSTODIAN
- **Steps:** POST `/api/reservations` with valid body
- **Expected HTTP:** 403 Forbidden
- **Expected Body:** `{ error: "Custodians cannot submit reservation requests" }`

#### TC-RES-006 â€” Get reservations: STUDENT sees only own requests
- **Priority:** H
- **Preconditions:** 2 reservations for STUDENT A, 1 for STUDENT B; authenticated as STUDENT A
- **Steps:** GET `/api/reservations`
- **Expected HTTP:** 200 OK
- **Expected Body:** Array of 2 (only STUDENT A's)

#### TC-RES-007 â€” Get reservations: CUSTODIAN sees all requests
- **Priority:** H
- **Preconditions:** Reservations from multiple students; authenticated as CUSTODIAN
- **Steps:** GET `/api/reservations`
- **Expected HTTP:** 200 OK
- **Expected Body:** All reservations across all users

#### TC-RES-008 â€” Approve reservation (CUSTODIAN)
- **Priority:** H
- **Preconditions:** PENDING reservation exists; item has `availableQuantity: 3`
- **Steps:** PUT `/api/reservations/{id}/approve`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ status: "APPROVED" }`
- **Side Effects:** Item `availableQuantity` decremented by request quantity; a `Loan` record created

#### TC-RES-009 â€” Observer Pattern: approval fires BorrowStatusChangedEvent
- **Priority:** H
- **Preconditions:** `AuditListener` is active in application context
- **Steps:** PUT `/api/reservations/{id}/approve` (same as TC-RES-008)
- **Verification:** `AuditListener.onStatusChanged()` invoked; log entry or auditable side-effect observable
- **Expected:** No exception thrown; event published with `newStatus: APPROVED`

#### TC-RES-010 â€” Approve reservation as STUDENT (forbidden)
- **Priority:** H
- **Preconditions:** Authenticated as STUDENT
- **Steps:** PUT `/api/reservations/{id}/approve`
- **Expected HTTP:** 403 Forbidden

#### TC-RES-011 â€” Reject reservation (CUSTODIAN)
- **Priority:** H
- **Steps:** PUT `/api/reservations/{id}/reject`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ status: "REJECTED" }`

#### TC-RES-012 â€” Mark reservation as returned (CUSTODIAN)
- **Priority:** H
- **Preconditions:** APPROVED reservation; item `availableQuantity` is lower than `totalQuantity`
- **Steps:** PUT `/api/reservations/{id}/return`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ status: "RETURNED", actualReturnDate (non-null) }`
- **Side Effects:** Item `availableQuantity` restored; `available: true`

#### TC-RES-013 â€” Mark reservation as overdue (CUSTODIAN)
- **Priority:** H
- **Steps:** PUT `/api/reservations/{id}/overdue`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ status: "OVERDUE" }`

#### TC-RES-014 â€” Observer Pattern: OVERDUE status triggers PenaltyListener
- **Priority:** H
- **Preconditions:** APPROVED reservation/loan exists with past due date
- **Steps:** PUT `/api/reservations/{id}/overdue`
- **Verification:** `PenaltyCalculationService.calculatePenaltyForLoan()` is invoked; a `Penalty` record exists for the borrower after the call
- **Expected:** Penalty record created with `daysOverdue > 0` and `penaltyPoints â‰¤ 30`

#### TC-RES-015 â€” STUDENT cannot access another student's reservation
- **Priority:** H
- **Preconditions:** Reservation belongs to STUDENT B; authenticated as STUDENT A
- **Steps:** GET `/api/reservations/{id}`
- **Expected HTTP:** 403 Forbidden

---

### 5.4 Loans â€” `/api/loans`

#### TC-LOAN-001 â€” Get loans: CUSTODIAN sees all
- **Priority:** H
- **Preconditions:** 3 loan records from different users; authenticated as CUSTODIAN
- **Steps:** GET `/api/loans`
- **Expected HTTP:** 200 OK
- **Expected Body:** Array of 3 loans with `borrowerName`, `penaltyPoints` fields

#### TC-LOAN-002 â€” Get loans: STUDENT sees only own
- **Priority:** H
- **Preconditions:** 2 loans for STUDENT A, 1 for STUDENT B; authenticated as STUDENT A
- **Steps:** GET `/api/loans`
- **Expected HTTP:** 200 OK
- **Expected Body:** Array of 2 (STUDENT A's loans only)

#### TC-LOAN-003 â€” Loan created automatically on reservation approval
- **Priority:** H
- **Preconditions:** PENDING reservation exists
- **Steps:** PUT `/api/reservations/{id}/approve`; then GET `/api/loans`
- **Verification:** A loan appears with `reservationId` matching the approved reservation
- **Expected:** `Loan.dueDate == BorrowRequest.returnDate`; `Loan.isOverdue == false`

#### TC-LOAN-004 â€” Loan approval is idempotent (no duplicate loan)
- **Priority:** M
- **Preconditions:** Reservation already APPROVED with a Loan record
- **Steps:** Call approve endpoint again on same id
- **Verification:** Only one Loan record exists for that `reservationId`
- **Expected:** HTTP 200; no second Loan created (uses `findByReservationId` guard)

#### TC-LOAN-005 â€” Return loan via loan endpoint (CUSTODIAN)
- **Priority:** M
- **Preconditions:** Active loan exists; authenticated as CUSTODIAN
- **Steps:** POST `/api/loans/{id}/return`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ returnedAt (non-null), isOverdue: false }`
- **Side Effects:** Item `availableQuantity` restored; associated BorrowRequest status set to RETURNED

#### TC-LOAN-006 â€” Student cannot process loan return
- **Priority:** H
- **Preconditions:** Authenticated as STUDENT
- **Steps:** POST `/api/loans/{id}/return`
- **Expected HTTP:** 403 Forbidden

#### TC-LOAN-007 â€” Factory Pattern: LoanDTO contains borrowerName and penaltyPoints
- **Priority:** M
- **Verification:** `DTOFactory.toLoanDTO(loan, borrowerName, pts)` produces DTO with all 3 fields populated
- **Expected:** `loanDTO.borrowerName` equals the full name from UserRepository; `loanDTO.penaltyPoints` equals penalty record value or 0

---

### 5.5 Penalties â€” `/api/users/{id}/penalties`

#### TC-PEN-001 â€” Get own penalties (STUDENT)
- **Priority:** H
- **Preconditions:** STUDENT has 2 penalty records; authenticated as that STUDENT
- **Steps:** GET `/api/users/{userId}/penalties`
- **Expected HTTP:** 200 OK
- **Expected Body:** `{ userId, fullName, totalUnpaidPoints, penalties: [...] }`

#### TC-PEN-002 â€” Custodian can view any user's penalties
- **Priority:** H
- **Preconditions:** Authenticated as CUSTODIAN
- **Steps:** GET `/api/users/{studentId}/penalties`
- **Expected HTTP:** 200 OK

#### TC-PEN-003 â€” Student cannot view another student's penalties
- **Priority:** H
- **Preconditions:** Authenticated as STUDENT A; requesting STUDENT B's id
- **Steps:** GET `/api/users/{studentBId}/penalties`
- **Expected HTTP:** 403 Forbidden

#### TC-PEN-004 â€” Penalty points capped at 30 per loan
- **Priority:** H
- **Preconditions:** Loan with `dueDate = 60 days ago`
- **Steps:** Call `PenaltyCalculationService.calculatePenaltyForLoan(loanId)` directly
- **Verification:** `Penalty.penaltyPoints == 30` (not 60)
- **Expected:** Cap of `MAX_POINTS_PER_LOAN = 30` enforced

#### TC-PEN-005 â€” Unpaid penalty total excludes paid penalties
- **Priority:** M
- **Preconditions:** STUDENT has 2 penalties: one paid (10 pts), one unpaid (15 pts)
- **Steps:** GET `/api/users/{id}/penalties`
- **Expected Body:** `totalUnpaidPoints: 15` (paid penalty excluded from sum)

#### TC-PEN-006 â€” Scheduled job marks overdue loans and creates penalties
- **Priority:** H
- **Preconditions:** Loan with `returnedAt = null` and `dueDate = yesterday`
- **Steps:** Call `PenaltyCalculationService.calculateOverduePenalties()` directly
- **Verification:** `loan.isOverdue == true`; `Penalty` record created with `daysOverdue â‰¥ 1`
- **Expected:** `penaltyPoints â‰¥ 1`; user's `penaltyPoints` field updated in User table

#### TC-PEN-007 â€” Factory Pattern: PenaltyDTO fields match Penalty entity
- **Priority:** L
- **Verification:** `DTOFactory.toPenaltyDTO(penalty)` maps all fields correctly
- **Expected:** `id`, `loanId`, `userId`, `itemName`, `penaltyPoints`, `daysOverdue`, `calculatedAt`, `paid` all present

---

### 5.6 Design Pattern Integration Tests

#### TC-DP-001 â€” Chain of Responsibility passes valid request through all validators
- **Priority:** H
- **Preconditions:** Full validator chain wired via `ValidatorConfig`
- **Input:** Valid `CreateBorrowRequestDTO` (item name present, future date, non-empty description)
- **Steps:** Call `borrowRequestValidatorChain.validate(request)` directly
- **Expected:** No `ValidationException` thrown

#### TC-DP-002 â€” Chain of Responsibility: ItemNameValidator rejects blank name context
- **Priority:** H
- **Verification:** If inventoryId resolves to an item with blank `itemName`, `ItemNameValidator` throws `ValidationException` before `DueDateValidator` runs
- **Expected:** Exception message from `ItemNameValidator`

#### TC-DP-003 â€” Chain of Responsibility: DueDateValidator rejects past date
- **Priority:** H
- **Input:** `returnDate = LocalDate.now().minusDays(1)`
- **Steps:** Call `borrowRequestValidatorChain.validate(request)`
- **Expected:** `ValidationException` thrown by `DueDateValidator`

#### TC-DP-004 â€” Facade Pattern: AuthFacade routes standard login to AuthService
- **Priority:** M
- **Preconditions:** `AuthFacade` autowired with real `AuthService`
- **Steps:** Call `authFacade.login(loginRequest)`
- **Verification:** Response contains JWT; `AuthService.login()` path executed (not Google path)

#### TC-DP-005 â€” Builder Pattern: UserBuilder creates user with all fields
- **Priority:** L
- **Steps:** `User.builder().fullName("...").email("...").password("...").role(STUDENT).build()`
- **Expected:** User object non-null; all set fields accessible via getters; unset optional fields null

---

## 6. Entry and Exit Criteria

### 6.1 Entry Criteria
- Backend source code compiles without errors (`mvnw compile` exits 0)
- H2 test datasource is configured in `src/test/resources/application-test.properties`
- All dependencies are resolvable from Maven Central

### 6.2 Exit Criteria
- All test cases in Section 5 have been executed
- Pass rate is 100% (zero failures, zero errors)
- No test case skipped without documented reason
- Code coverage of service layer â‰¥ 80% (measured by JaCoCo)

---

## 7. Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Scheduled job timing in tests | `@Scheduled` won't fire automatically during unit tests | Call `calculateOverduePenalties()` directly in TC-PEN-006 |
| Spring Security context in MockMvc | 403 on all requests if security not configured for test | Use `@WithMockUser` or inject real JWT via `JwtUtil` in test setup |
| H2 vs MySQL dialect differences | Queries using MySQL-specific syntax may fail on H2 | Use JPQL/HQL only; avoid native queries with MySQL functions |
| Google OAuth in tests | `verifyIdToken()` calls external Google endpoint | Mock `GoogleAuthService` for TC-AUTH-009 |
| Transactional isolation | Tests may leave dirty state affecting each other | Annotate each test class with `@Transactional` for automatic rollback |

---

## 8. Test Deliverables

| Deliverable | Description | Location |
|---|---|---|
| Test Plan (this document) | Strategy, scope, all test cases | `TEST_PLAN.md` |
| Automated Test Suite | JUnit 5 + MockMvc test classes | `backend/src/test/java/...` |
| Test Execution Report | Pass/fail results, coverage report | `TEST_REPORT.md` |
| JaCoCo Coverage Report | HTML coverage report | `backend/target/site/jacoco/` |

---

*End of Test Plan*
