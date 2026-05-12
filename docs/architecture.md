# TechLoan System Architecture

**Course:** IT342 — Advanced Software Engineering  
**Project:** Rentuma TechLoan  
**Author:** Trixie Ann Rentuma  

---

## 1. System Overview

TechLoan is a multi-platform lab equipment borrowing system for CIT-U students. It consists of three clients — a React web app, an Android mobile app, and a Spring Boot backend — backed by a PostgreSQL database hosted on Supabase.

```
┌──────────────────────────────────────────────────────────┐
│                      Clients                             │
│                                                          │
│  ┌─────────────────┐     ┌──────────────────────────┐   │
│  │  Web App        │     │  Android App (Kotlin)     │   │
│  │  React + Vite   │     │  MVVM + Retrofit          │   │
│  │  Tailwind CSS   │     │  ViewBinding + LiveData   │   │
│  └────────┬────────┘     └────────────┬─────────────┘   │
│           │  HTTPS / REST JSON        │                  │
└───────────┼───────────────────────────┼──────────────────┘
            │                           │
            ▼                           ▼
┌───────────────────────────────────────────────────────────┐
│               Spring Boot Backend (Java 21)               │
│                                                           │
│  ┌─────────────────────────────────────────────────────┐  │
│  │              Vertical Slice Architecture            │  │
│  │                                                     │  │
│  │  features/                                          │  │
│  │   ├── auth/       (register, login, Google OAuth)   │  │
│  │   ├── inventory/  (CRUD + image upload)             │  │
│  │   ├── reservation/(borrow requests, QR code, PDF)   │  │
│  │   ├── loan/       (active loans, return, overdue)   │  │
│  │   ├── penalty/    (penalty points, calculation)     │  │
│  │   ├── payment/    (PayMongo GCash/Maya integration)      │  │
│  │   └── holiday/    (PH public holidays API)          │  │
│  │                                                     │  │
│  │  shared/                                            │  │
│  │   ├── security/   (JWT filter, SecurityConfig)      │  │
│  │   └── exception/  (GlobalExceptionHandler)          │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                           │
│  ┌────────────────────┐  ┌──────────────────────────────┐ │
│  │  Spring Security   │  │  External API Integrations   │ │
│  │  JWT Auth Filter   │  │  - PayMongo (GCash / Maya)   │ │
│  │  BCrypt Passwords  │  │  - Google OAuth2 / OpenID    │ │
│  │  RBAC (2 roles)    │  │  - Nager.Date Holidays API   │ │
│  └────────────────────┘  │  - SMTP Email (Spring Mail)  │ │
│                          └──────────────────────────────┘ │
└───────────────────────────────────────┬───────────────────┘
                                        │ JPA / Hibernate
                                        ▼
┌───────────────────────────────────────────────────────────┐
│          PostgreSQL (Supabase — aws-ap-southeast-2)       │
│                                                           │
│  Tables:                                                  │
│   users          inventory_items    borrow_requests       │
│   loans          penalties          payments              │
│   refresh_tokens borrow_events                            │
└───────────────────────────────────────────────────────────┘
```

---

## 2. Backend Layer Detail (Vertical Slice Architecture)

Each feature slice is self-contained and contains its own:
- **Controller** — HTTP entry point, handles request/response
- **Service** — Business logic
- **Repository** — JPA data access interface
- **Model** — JPA entity (mapped to DB table)
- **DTO** — Data Transfer Objects for API responses

```
features/reservation/
├── BorrowController.java       ← REST endpoints
├── BorrowService.java          ← Business logic
├── BorrowEventPublisher.java   ← Observer: emits events on status change
├── BorrowEventListener.java    ← Observer: listens and audits events
├── InventoryImageController.java
├── dto/
│   ├── BorrowRequestDTO.java
│   └── CreateReservationRequest.java
├── model/
│   └── BorrowRequest.java
├── repository/
│   └── BorrowRequestRepository.java
└── validation/
    ├── BorrowRequestValidator.java   ← Chain of Responsibility: base
    ├── ItemNameValidator.java        ← Chain link 1
    └── DueDateValidator.java         ← Chain link 2
```

---

## 3. Security Flow

```
HTTP Request
     │
     ▼
JwtAuthFilter (OncePerRequestFilter)
     │  Extracts & validates JWT from Authorization header
     ▼
SecurityConfig (permitAll vs authenticated)
     │
     ▼
Controller Method
     │
     │  RBAC enforced via @AuthenticationPrincipal + role checks
     ▼
Service Layer
```

**Token lifecycle:**
1. `POST /api/auth/login` → returns `accessToken` (24h) + `refreshToken` (7d)
2. Clients store token in `localStorage` (web) / `SharedPreferences` (Android)
3. All protected requests include `Authorization: Bearer <token>`
4. Google OAuth: ID token verified via Google API → JWT issued

---

## 4. Design Patterns Used

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Facade** | `AuthFacade` | Hides complexity of register/login/Google flows |
| **Factory** | `DTOFactory` | Centralises DTO construction logic |
| **Builder** | `User.UserBuilder` | Fluent user construction during registration |
| **Observer** | `BorrowEventPublisher` + `AuditListener` | Decoupled event-driven audit logging |
| **Chain of Responsibility** | `ItemNameValidator`, `DueDateValidator` | Sequential validation pipeline |
| **Strategy** | `StandardRegistrationValidator` | Swappable registration validation rules |

---

## 5. External API Integrations

| Integration | Requirement | Endpoint |
|-------------|-------------|----------|
| **PayMongo** (4.4) | Payment gateway (GCash + Maya) | `https://api.paymongo.com/v1/checkout_sessions` |
| **Google OAuth2** (4.2) | Social login | `https://accounts.google.com` |
| **Nager.Date Holidays** (4.1) | Public API | `https://date.nager.at/api/v3/PublicHolidays/{year}/PH` |
| **SMTP Email** (4.6) | Transactional email | Spring Mail (Gmail SMTP / configurable) |

---

## 6. File Upload (Requirement 4.3)

- **Endpoint:** `POST /api/inventory/{id}/image`
- **Storage:** Server disk at `uploads/equipment/item-{id}-{uuid}.{ext}`
- **Served at:** `GET /uploads/equipment/{filename}` (static resource)
- **Constraints:** JPEG/PNG only, max 5 MB
- **Database:** `inventory_items.image_url` stores relative path

---

## 7. Database Schema (ERD Summary)

```
users ──────────────── borrow_requests ─── loans
  │                           │                │
  │                    inventory_items         │
  │                                            │
  └── penalties ──────────────────────── payments
```

Key relationships:
- `borrow_requests.user_id` → `users.id`
- `borrow_requests.inventory_id` → `inventory_items.id`
- `loans.reservation_id` → `borrow_requests.id`
- `penalties.loan_id` → `loans.id`
- `payments.penalty_id` → `penalties.id`

---

## 8. Mobile App Architecture (Android MVVM)

```
Activity (View)
     │ observes
     ▼
ViewModel (LiveData<State>)
     │ calls
     ▼
Repository / API Service (Retrofit)
     │ HTTP
     ▼
Spring Boot Backend
```

- Sealed class `State` drives UI: `Loading`, `Success`, `Error`
- `SharedPreferences` stores JWT, userId, role after login
- Role-based routing: `CUSTODIAN` → `CustodianDashboardActivity`, others → `DashboardActivity`
