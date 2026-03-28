# IT342 Phase 2 – Mobile Development Summary

## Project Information
- **Repository**: IT345-Rentuma-TechLoan
- **Commit Hash**: da1489b
- **Commit Message**: IT342 Phase 2 – Mobile Development Completed
- **Date**: March 28, 2026

---

## 1. Registration Flow

### How Registration Works
1. **User navigates to Registration Screen** - The app displays a visually enhanced registration form with:
   - Maroon color theme (#8B0000) header card with "Join TechLoan" branding
   - Three input fields organized in a white card container:
     - Full Name (textPersonName input type, auto-capitalization)
     - Email Address (textEmailAddress validation)
     - Password (with toggle visibility, minimum 6 characters)

2. **Form Submission** - When user clicks "Create Account":
   - All fields are validated for non-empty values
   - Email format is verified
   - Password minimum length (6 characters) is checked
   - Loading progress bar appears during submission

3. **API Call** - Registration request sent to backend:
   - **Endpoint**: `POST /api/auth/register`
   - **Request Body**:
     ```json
     {
       "name": "user's full name",
       "email": "user@email.com",
       "password": "hashed_password"
     }
     ```
   - **Response**: User object with ID and JWT token

4. **Successful Registration**:
   - User receives confirmation message (green success indicator)
   - JWT token stored securely in SharedPreferences
   - User redirected to Login screen or Dashboard
   - Account created in PostgreSQL database (Supabase)

### UI Components
- **Material Design TextInputLayout** with outlined style
- **CardView** containers with elevation and corner radius for depth
- **Custom button styling** (46dp height for optimal touch targets on mobile)
- **Color-coded messages** - Red for errors, Green for success

---

## 2. Login Flow

### How Login Works
1. **User navigates to Login Screen** - Displays an enhanced login form featuring:
   - "Welcome Back" header card with TechLoan branding (primary color background)
   - Two input fields in a white card:
     - Email Address
     - Password (with visibility toggle)
   - "Create Account" link for new users

2. **Credential Entry** - User enters:
   - Email (validated for email format)
   - Password (minimum 6 characters)

3. **Authentication Request** - Upon "Sign In" button click:
   - Form validates inputs client-side
   - Loading indicator displays
   - Request sent to backend authentication service

4. **API Call** - Login request details:
   - **Endpoint**: `POST /api/auth/login`
   - **Request Body**:
     ```json
     {
       "email": "user@email.com",
       "password": "user_password"
     }
     ```
   - **Response**: 
     ```json
     {
       "user": { "id": "...", "name": "...", "email": "..." },
       "accessToken": "JWT_TOKEN_24h",
       "refreshToken": "REFRESH_TOKEN_7d"
     }
     ```

5. **Token Management**:
   - Access token stored in SharedPreferences (24-hour expiration)
   - Refresh token stored securely (7-day expiration)
   - Tokens used for subsequent API requests

6. **After Login**:
   - Successful authentication message (green indicator)
   - User redirected to Dashboard
   - Session established with backend

### Security Features
- Passwords sent over HTTPS (TLS encryption)
- JWT tokens for stateless authentication
- Token refresh mechanism for extended sessions
- Password visibility toggle for user convenience

---

## 3. After Login (Dashboard)

### Dashboard Features
- **Greeting Card** with user name and motivational message
- **Summary Statistics**:
  - Active Loans count (📦 icon, blue card)
  - Pending Requests count (⏱️ icon, amber card)
- **Action Buttons**:
  - Browse Equipment (search functionality)
  - My Items (view user's current rentals)
- **Recent Activity Section** - Timeline of actions
- **Bottom Navigation** - Quick access to main sections
- **Sign Out Button** - Safe session termination

---

## 4. API Integration

### Base API Configuration
- **Backend URL**: `https://aws-1-ap-southeast-2.pooler.supabase.com`
- **Authentication**: JWT Bearer tokens in Authorization header
- **Content Type**: `application/json`

### API Endpoints Used

#### Authentication Endpoints
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/register` | Create new user account |
| POST | `/api/auth/login` | Authenticate user credentials |
| POST | `/api/auth/refresh` | Refresh expired access token |
| POST | `/api/auth/logout` | Terminate user session |

#### Data Endpoints
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/equipment` | Fetch available equipment |
| GET | `/api/rentals` | Get user's active rentals |
| POST | `/api/rentals` | Create new rental request |

### Retrofit Integration
- **HTTP Client**: Retrofit 2.9.0 with OkHttp3
- **JSON Serialization**: Gson converter
- **Interceptor**: Logging interceptor for debugging
- **Coroutines**: Async API calls with kotlinx-coroutines-android

### Request/Response Flow
```
User Action → ViewModel → ViewModel State Management
   ↓
Retrofit API Call (Coroutine)
   ↓
Backend Processing
   ↓
JSON Response → Gson Deserialization
   ↓
ViewModel Updates State
   ↓
UI Observes State Change & Updates
```

---

## 5. Database

### Database Configuration
- **Type**: PostgreSQL (Supabase)
- **Schema**: Automatically managed by Hibernate ORM
- **DDL Mode**: Update (auto-creates/updates tables)

### Key Tables
- **users** - User accounts with email, name, hashed passwords
- **rentals** - Equipment rental records
- **equipment** - Available equipment catalog
- **transactions** - Payment and activity history

---

## 6. Mobile App Architecture

### Tech Stack
- **Language**: Kotlin
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Material Design 3 Components
- **Layout System**: ConstraintLayout with CardView

### Project Structure
```
mobile/app/src/main/
├── java/com/example/techloan/
│   ├── SplashActivity.kt
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   ├── DashboardActivity.kt
│   ├── api/
│   │   ├── RetrofitClient.kt
│   │   └── TechLoanApi.kt
│   ├── model/
│   │   └── Models.kt (DTOs)
│   └── viewmodel/
│       └── AuthViewModel.kt
└── res/
    ├── layout/ (XML layouts)
    ├── values/ (colors, strings, themes)
    └── menu/ (navigation)
```

### Design System
- **Primary Color**: #8B0000 (Maroon)
- **Primary Light**: #A52A2A
- **Primary Dark**: #5C0000
- **Text Colors**: Gray scale (#111827 to #F9FAFB)
- **Accent Colors**: Blue, Green, Amber for status indicators

### Material Design Components Used
- MaterialToolbar - App bar
- TextInputLayout - Form inputs with Material styling
- MaterialButton - Primary and outlined buttons
- CardView - Elevation and visual hierarchy
- BottomNavigationView - Tab-based navigation
- ConstraintLayout - Responsive layouts for various screen sizes

---

## 7. Testing & Optimization

### Device Testing
- **Target Device**: Medium phones (API 36)
- **Screen Size**: ~480x800 pixels
- **Optimization**: Responsive layouts with optimized spacing (12-18dp)
- **Touch Targets**: Minimum 44dp height for buttons

### Build Status
- Clean build architecture
- Gradle 8.11.1 with Kotlin support
- Material Design 3 library (v1.11.0)
- AndroidX compatibility (appcompat 1.6.1)

---

## 8. Screenshots Required

Please capture and attach:
1. **Registration Screen** - Showing the form with header card
2. **Successful Registration** - Success message displayed
3. **Login Screen** - Showing the login form layout
4. **Successful Login** - Success indicator before redirect
5. **Dashboard Screen** - After login, showing summary and recent activity
6. **Database Record** - Admin view of created user in Supabase PostgreSQL

---

## Conclusion

The mobile development phase successfully delivers a fully functional Android application with:
- ✅ Secure user authentication (Registration & Login)
- ✅ Material Design UI with enhanced visual hierarchy
- ✅ Robust API integration with error handling
- ✅ Responsive layouts optimized for mobile devices
- ✅ Clean MVVM architecture for maintainability
- ✅ PostgreSQL backend integration with Supabase

The application is ready for testing on Android emulators and deployment to production.
