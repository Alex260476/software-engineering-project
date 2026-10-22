# Cinema E-Booking System (CinemaFlex) - Complete Setup & Execution Guide

A full-stack, enterprise-grade online cinema e-booking platform built for Sprint 1 & Sprint 2 deliverables. The application allows users to browse currently running and upcoming movies, view embedded YouTube trailers, filter by date and genre, register accounts with email verification, manage profiles and encrypted payment cards (up to 3 cards), maintain favorite movies, reset forgotten passwords, and access an administrative management portal.

---

## Table of Contents
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Quick Start (One-Command Automated Run)](#quick-start-one-command-automated-run)
- [Manual Step-by-Step Run](#manual-step-by-step-run)
- [Pre-Seeded Demo Accounts](#pre-seeded-demo-accounts)
- [Interactive Demo Companion](#interactive-demo-companion)
- [Running Automated Tests](#running-automated-tests)
- [Configuration & Environment Variables](#configuration--environment-variables)
- [Complete REST API Reference](#complete-rest-api-reference)
- [Troubleshooting & FAQs](#troubleshooting--faqs)

---

## Tech Stack

### Frontend Tier
- **Framework & Library**: [React 18](https://react.dev/)
- **Programming Language**: [TypeScript](https://www.typescriptlang.org/) (Strict type checking)
- **Build Tool & Bundler**: [Vite 5](https://vitejs.dev/) (Lightning-fast Hot Module Replacement and production bundle optimization)
- **Styling**: [Tailwind CSS](https://tailwindcss.com/) & Vanilla CSS
- **Client-Side Routing**: [React Router DOM v6](https://reactrouter.com/)
- **UI Icons**: [Lucide React](https://lucide.dev/) (Modern vector icon suite)
- **State Management**: React Context API (`AuthContext`)
- **API Client**: Native Fetch API with custom typed wrapper (`frontend/src/services/api.ts`)

### Backend Tier
- **Programming Language**: Java 17 / 21
- **Framework**: [Spring Boot 3.2.x](https://spring.io/projects/spring-boot)
- **Web & Routing**: Spring MVC / Spring Web (`@RestController`, `@RequestMapping`)
- **Persistence & ODM**: Spring Data MongoDB (`@Document`, `MongoRepository`)
- **Security & Cryptography**:
  - **Password Hashing**: BCrypt (Work Factor 12) via `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`
  - **Payment Card Encryption**: AES-256-GCM authenticated encryption via Java Cryptography Architecture (`javax.crypto.Cipher`)
- **Validation**: Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Pattern`)
- **API Documentation**: Springdoc OpenAPI 2.3.0 / Swagger UI
- **Build & Dependency Tool**: Apache Maven (Bundled via Maven Wrapper `./mvnw`)

### Database Tier
- **Database**: [MongoDB 6.0+](https://www.mongodb.com/) (NoSQL Document Store, port `27017`)
- **Database Name**: `cinema_ebooking`
- **Collections**: `movies`, `users`

### Email & Verification Infrastructure
- **Virtual In-Memory Mailbox**: Built-in mock email service (`EmailService`) simulating transactional emails for account activation and password resets.
- **Developer Mailbox API**: Accessible via `GET /api/v1/dev/emails` and integrated into the frontend Demo Companion modal for instant 1-click token validation without requiring external SMTP credentials.

### Orchestration & Developer Tooling
- **One-Command Orchestration**: Node.js ES Modules script (`scripts/start.mjs`) managing MongoDB, backend, and frontend concurrently.
- **One-Time Setup Scripts**: `setup.sh` (macOS/Linux) and `setup.cmd` (Windows).
- **Domain Modeling**: PlantUML specification (`cinema_domain_model.puml`) & Python rendering script (`generate_diagram.py`).

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Browser                         │
│             React 18 + TypeScript + Vite (Port 5173)         │
└──────────────────────────────▲──────────────────────────────┘
                               │ HTTP / JSON REST APIs
                               │ (CORS Enabled)
┌──────────────────────────────▼──────────────────────────────┐
│                    Spring Boot Backend                      │
│                  REST API Server (Port 8080)                │
│                                                             │
│  Controllers:                                               │
│   • MovieController          • AuthenticationController     │
│   • UserController           • DevController                │
│                                                             │
│  Security Layer:                                            │
│   • BCrypt Password Hashing  • AES-256 Payment Encryption   │
│   • Secure Token Service     • Global Exception Handler     │
└──────────────────────────────▲──────────────────────────────┘
                               │ Spring Data MongoDB Driver
┌──────────────────────────────▼──────────────────────────────┐
│                       MongoDB Engine                        │
│                Local Daemon / Atlas (Port 27017)             │
│                Collections: `movies`, `users`                │
└─────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

Before running the application, make sure you have the following installed on your machine:
- **Node.js**: v18.0.0 or higher ([Download](https://nodejs.org/))
- **Java Development Kit (JDK)**: JDK 17 or JDK 21 ([Eclipse Temurin](https://adoptium.net/) or OpenJDK)
- **MongoDB**: Community Edition 6.0+ ([Download](https://www.mongodb.com/try/download/community))
- **Git**: For source version control ([Download](https://git-scm.com/))

> **Note**: Maven is **not** required separately; the project bundles the official Maven Wrapper (`mvnw` / `mvnw.cmd`).

---

## Quick Start (One-Command Automated Run)

The easiest and recommended way to start the entire system (MongoDB + Spring Boot Backend + React Frontend) is using the automated root runner.

### Step 1: Clone and Checkout Branch

```bash
git clone https://github.com/Alex260476/software-engineering-project.git
cd software-engineering-project
git checkout t1_d5
```

### Step 2: One-Time System Setup

Run the setup script for your operating system. It verifies prerequisites, installs missing tools, and downloads backend and frontend dependencies:

- **macOS / Linux**:
  ```bash
  bash setup.sh
  ```
  *(To automatically answer yes to prompts, use `bash setup.sh --yes`)*

- **Windows**:
  ```bat
  setup.cmd
  ```

### Step 3: Start the Application

```bash
npm start
```

### What `npm start` Does Automatically:
1. Detects if MongoDB is running on port `27017`. If not, starts an embedded instance storing data in `.data/mongo/`.
2. Compiles and launches the Spring Boot backend on `http://localhost:8080`.
3. Seeds initial movies and test accounts if the database is clean.
4. Starts the Vite frontend dev server on `http://localhost:5173`.
5. Opens your default web browser to `http://localhost:5173`.
6. Gracefully shuts down all processes when you press **Ctrl + C**.

#### Available Startup Flags:
- Live Logs: `npm start -- --verbose` (streams backend and frontend logs directly to terminal)
- Headless / No Browser: `npm start -- --no-open`
- Fresh Database Re-seed: `SEED_RESET=true npm start`

---

## Manual Step-by-Step Run

If you prefer to run each service individually in separate terminals:

### 1. Start MongoDB (Port 27017)
- **macOS (Homebrew)**:
  ```bash
  brew services start mongodb-community
  ```
- **Linux (systemd)**:
  ```bash
  sudo systemctl start mongod
  ```
- **Windows (Service)**:
  ```bat
  net start MongoDB
  ```
- **Docker Alternative**:
  ```bash
  docker run -d -p 27017:27017 --name cinema-mongo mongo:latest
  ```

### 2. Start the Backend API (Port 8080)
Open a new terminal window:
```bash
cd cinema-ebooking-backend

# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```
*Backend verification:*
- API Health: [http://localhost:8080/api/v1/movies/genres](http://localhost:8080/api/v1/movies/genres)
- Swagger UI Documentation: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 3. Start the Frontend Web App (Port 5173)
Open another terminal window:
```bash
cd frontend
npm install
npm run dev
```
*Frontend URL:* [http://localhost:5173](http://localhost:5173)

---

## Pre-Seeded Demo Accounts

The backend automatically seeds verified accounts, unverified accounts, administrators, and payment card fixtures on startup (`DataSeeder.java`):

| Account Type | Email | Password | Pre-Configured State / Purpose |
|--------------|-------|----------|---------------------------------|
| **Admin** | `admin@cinema.com` | `AdminPass!23` | Role: `ADMIN`. Full access to `/admin` management portal. |
| **Verified User (Favorites)** | `john@example.com` | `Pass123!user` | Status: `ACTIVE`. Has favorite movie pre-saved (*Dune: Part Two*). |
| **Verified User (3 Cards)** | `jane@example.com` | `Pass123!jane` | Status: `ACTIVE`. Has 3 encrypted payment cards stored (Demonstrates 3-card limit). |
| **Unverified User** | `unverified@example.com` | `Pass123!unv` | Status: `INACTIVE`. Tests unverified login rejection. |
| **Suspended User** | `suspended@example.com` | `Pass123!susp` | Status: `SUSPENDED`. Tests account suspension rejection. |

---

## Interactive Demo Companion

To streamline grading, sprint demonstrations, and grading rubric execution, a floating **Demo Helper** button is embedded at the bottom-right corner of the web interface:

1. **One-Click Account Switcher**: Instantly log in as Admin, John Doe, Jane Smith, or an Unverified User with a single click.
2. **Auto-Fill Registration (TC1)**: Pre-fills registration forms with test data (`Eman Saleh`, `eman@gmail.my`, `MyPass!20`).
3. **Virtual Dev Mailbox Viewer**: Real-time access to the mock email system (`/api/v1/dev/emails`). When a user registers or requests a password reset, click the generated link directly inside the app to activate the account or reset the password without external email setup.
4. **Card Limit Tester (TC5)**: Quickly attempts adding a 4th card to Jane Smith to demonstrate enforcement of the 3-card maximum constraint.
5. **Favorites Tooltip**: Hover over any movie heart icon to see custom tooltips for favorited vs non-favorited states.

---

## Running Automated Tests

### Backend Unit & Security Tests
To execute all backend unit tests (including user registration, duplicate email rejection, 3-card limit enforcement, BCrypt password hashing, and AES-256-GCM card encryption/decryption):

```bash
cd cinema-ebooking-backend
./mvnw test       # Windows: mvnw.cmd test
```

Test Suites:
- `com.cinema.ebook.service.UserServiceTest`: Validates registration flow, duplicate email checks, authentication token lifecycle, card additions/deletions, 3-card limit constraint, and favorite toggles.
- `com.cinema.ebook.util.PasswordHashingAndEncryptionTest`: Validates BCrypt salt generation, password matching, AES-256 encryption uniqueness (IV randomness), and round-trip decryption.

### Frontend Type-Checking & Production Build
To validate TypeScript types and generate the production web bundle:

```bash
cd frontend
npm run build
```

---

## Configuration & Environment Variables

All default settings are located in `cinema-ebooking-backend/src/main/resources/application.properties`. You can override any configuration using environment variables:

| Environment Variable | Default Value | Description |
|----------------------|---------------|-------------|
| `PORT` / `server.port` | `8080` | Port for the Spring Boot backend REST API |
| `MONGODB_URI` | `mongodb://localhost:27017/cinema_ebooking` | MongoDB connection URI (supports MongoDB Atlas) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Allowed origins for cross-origin API calls |
| `SEED_RESET` | `false` | When set to `true`, drops existing collections and cleanly re-seeds all data on startup |
| `ENCRYPTION_KEY` | *(Built-in 256-bit AES Base64 key)* | AES-256 symmetric key used for encrypting payment cards |

---

## Complete REST API Reference

All responses follow the unified structure:
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully"
}
```

### Authentication Endpoints (`/api/v1/auth`)
| Method | Endpoint | Request Body | Description |
|--------|----------|--------------|-------------|
| `POST` | `/api/v1/auth/register` | `RegistrationRequestDTO` | Register new user. Sets status `INACTIVE` and generates activation token. |
| `GET` | `/api/v1/auth/verify-email?token=` | *None* | Verifies email token and activates user account (`ACTIVE`). |
| `POST` | `/api/v1/auth/login` | `LoginRequestDTO` | Authenticates email & password. Verifies status is `ACTIVE`. |
| `POST` | `/api/v1/auth/forgot-password` | `PasswordResetRequestDTO` | Generates reset token and sends recovery link. |
| `POST` | `/api/v1/auth/reset-password` | `{ token, newPassword }` | Resets password using verification token. |

### User Profile Endpoints (`/api/v1/users`)
| Method | Endpoint | Request Body | Description |
|--------|----------|--------------|-------------|
| `GET` | `/api/v1/users/me` | *None* (Header: `X-User-Id` / Token) | Get profile of logged-in user. |
| `PUT` | `/api/v1/users/profile` | `UpdateProfileRequestDTO` | Update name, phone, address, and promo subscription. |
| `PUT` | `/api/v1/users/password` | `ChangePasswordRequestDTO` | Change account password (requires old password). |
| `GET` | `/api/v1/users/cards` | *None* | Retrieve payment cards (numbers masked, e.g. `**** **** **** 1111`). |
| `POST` | `/api/v1/users/cards` | `PaymentCardRequestDTO` | Add payment card (AES-256 encrypted, max 3 cards). |
| `DELETE` | `/api/v1/users/cards/{cardId}`| *None* | Delete stored payment card. |
| `POST` | `/api/v1/users/favorites/{movieId}`| *None* | Add movie to user's favorites. |
| `DELETE` | `/api/v1/users/favorites/{movieId}`| *None* | Remove movie from user's favorites. |

### Movie Catalog Endpoints (`/api/v1/movies`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/movies` | Get all movies (Optional query: `?status=CURRENTLY_RUNNING` or `COMING_SOON`). |
| `GET` | `/api/v1/movies/{id}` | Get detailed movie metadata, showtimes, cast, director, and YouTube trailer. |
| `GET` | `/api/v1/movies/search?title=` | Case-insensitive movie title search. |
| `GET` | `/api/v1/movies/filter` | Filter by `genre`, `rating`, `status`, and `showDate` (e.g. `YYYY-MM-DD`). |
| `GET` | `/api/v1/movies/genres` | Get list of all distinct movie genres. |

### Developer & Testing Endpoints (`/api/v1/dev`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/dev/emails` | Inspect sent verification emails and password reset links. |
| `DELETE` | `/api/v1/dev/emails` | Clear virtual mailbox. |

---

## Troubleshooting & FAQs

### 1. "Port 8080 or 5173 is already in use"
Another instance of the backend or frontend is still running.
- **macOS / Linux**:
  ```bash
  lsof -ti :8080 | xargs kill -9 2>/dev/null || true
  lsof -ti :5173 | xargs kill -9 2>/dev/null || true
  ```
- **Windows**:
  ```bat
  netstat -ano | findstr :8080
  taskkill /PID <PID_NUMBER> /F
  ```

### 2. "MongoDB connection refused"
Ensure MongoDB is running locally on port 27017, or run `npm start` which will automatically launch a dedicated MongoDB instance under `.data/mongo/`.

### 3. "JAVA_HOME is not set or Maven error"
Ensure JDK 17 or JDK 21 is installed. On macOS with Homebrew:
```bash
export JAVA_HOME=$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home
```

### 4. How to reset the database and reseed from scratch
If you modified seed data or want a fresh start:
```bash
SEED_RESET=true npm start
```
Or with manual run:
```bash
cd cinema-ebooking-backend
SEED_RESET=true ./mvnw spring-boot:run
```

---

## Authors & Course Information
- **Course**: Software Engineering Project (CSCI 4050 / CSCI 6050)
- **Branch**: `t1_d5`
- **Deliverables**: Sprint 1 (Movie Browsing, Trailers, Search & Filter) & Sprint 2 (Authentication, Profile, Encryption, Admin Portal, Data Seeds)
