# 🛒 Marketplace

A full-stack peer-to-peer marketplace where users can buy and sell items — with real-time chat, Stripe payments, full-text search, and a complete moderation system.

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=flat&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React_19-20232A?style=flat&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat&logo=typescript&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat&logo=mongodb&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=flat&logo=elasticsearch&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![CI](https://github.com/shagid76/Marketplace-pet-project/actions/workflows/backend.yml/badge.svg)

---

## Features

- **Auth** — Registration, login, JWT access + refresh token rotation (30 min / 5 days)
- **Product listings** — Create, update, delete with multi-image upload (up to 6 photos), stored in MinIO
- **Search** — Full-text search via Elasticsearch with category and price range filters
- **Shopping cart** — Add/remove items, apply promo codes, proceed to checkout
- **Stripe payments** — Checkout sessions with optimistic locking to prevent double-purchases; expired payment auto-release via scheduler
- **Real-time chat** — WebSocket-based messaging between buyers and sellers (SockJS + STOMP)
- **Admin panel** — Ban/block users and products, issue warnings, revoke and extend actions
- **Moderation** — Report system for users and products; SSE-based instant ban notifications with polling fallback
- **Reviews** — Leave reviews on completed purchases; moderation-aware review display

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4 · Java 21 |
| Database | MongoDB 7 (replica set) |
| Search | Elasticsearch 8.11 |
| File storage | MinIO (S3-compatible) |
| Authentication | JWT (JJWT) · Spring Security 6 |
| Payments | Stripe Checkout API |
| Real-time | WebSocket (SockJS/STOMP) · SSE |
| API docs | OpenAPI 3 / Swagger UI |
| Testing | JUnit 5 · Mockito · 228 tests |
| Build | Gradle · Docker |

### Frontend
| Layer | Technology |
|---|---|
| Framework | React 19 · TypeScript |
| State | Redux Toolkit · TanStack Query |
| Forms | React Hook Form · Zod |
| HTTP | Axios (interceptors, silent token refresh) |
| Real-time | SockJS · STOMP |
| Testing | Jest · React Testing Library · Playwright (E2E) |
| Build | Create React App (Webpack) |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│        Redux Toolkit · React Query · Zod · Axios        │
└────────────────────────┬────────────────────────────────┘
                         │ REST + WebSocket + SSE
┌────────────────────────▼────────────────────────────────┐
│                  Spring Boot API                         │
│   Controllers → Services → Repositories → Mappers       │
│              JWT Filter · Spring Security                │
└──────┬──────────┬───────────┬──────────────┬────────────┘
       │          │           │              │
  ┌────▼───┐ ┌───▼──────┐ ┌──▼───┐   ┌─────▼──────┐
  │MongoDB │ │Elastic-  │ │MinIO │   │  Stripe    │
  │Replica │ │search    │ │      │   │  Checkout  │
  │Set     │ │(search)  │ │(img) │   │  API       │
  └────────┘ └──────────┘ └──────┘   └────────────┘
```

**Key design decisions:**
- **Optimistic locking** (`@Version` on `Product`) prevents two buyers from purchasing the same item simultaneously
- **Dual-write** — every product save is synced to both MongoDB and Elasticsearch atomically
- **SSE + polling fallback** — ban notifications arrive instantly via SSE; a background poll catches any dropped connections
- **JWT type validation** — refresh tokens are explicitly rejected on endpoints expecting access tokens

---

## Getting Started (Local)

### Prerequisites
- Java 21
- Docker & Docker Compose
- Node.js 20+

### Backend

```bash
cd marketplace-backend

# Copy and fill in environment variables
cp .env.example .env

# Start all infrastructure (MongoDB, Elasticsearch, MinIO)
# then build and run the backend
docker compose up -d --build

# API is available at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui/index.html
```

### Frontend

```bash
cd marketplace-frontend

cp .env.example .env
# Set REACT_APP_API_URL=http://localhost:8080/api

npm install
npm start
# App is available at http://localhost:3000
```

---

## Environment Variables

See [`marketplace-backend/.env.example`](marketplace-backend/.env.example) and [`marketplace-frontend/.env.example`](marketplace-frontend/.env.example) for all required variables with descriptions.

The backend requires: MongoDB credentials, JWT secret, MinIO credentials, Stripe API key + webhook secret, and the frontend origin URL.

Generate a secure JWT secret with:
```bash
openssl rand -hex 32
```

---

## Testing

```bash
# Backend — runs all 228 tests + generates JaCoCo coverage report
cd marketplace-backend
./gradlew test

# Coverage report: build/reports/jacoco/test/html/index.html

# Frontend — unit tests
cd marketplace-frontend
npm test

# Frontend — end-to-end (Playwright)
npx playwright test
```

---

## CI/CD

GitHub Actions runs on every push:

- **Backend pipeline** — compiles, runs 228 tests, uploads JaCoCo coverage report as artifact, builds JAR
- **Frontend pipeline** — TypeScript type-check, ESLint, unit tests with coverage, production build

Pipelines are path-scoped — pushing to `marketplace-backend/` only triggers the backend workflow and vice versa.

---

## Project Structure

```
marketplace/
├── .github/
│   └── workflows/
│       ├── backend.yml       # Java CI pipeline
│       └── frontend.yml      # Node CI pipeline
├── marketplace-backend/
│   ├── src/
│   │   ├── main/java/com/marketplace/backend/
│   │   │   ├── configuration/  # Security, CORS, MinIO, WebSocket, OpenAPI
│   │   │   ├── controller/     # 12 REST controllers
│   │   │   ├── dto/            # API contracts (12 DTOs)
│   │   │   ├── exception/      # GlobalExceptionHandler + 8 custom exceptions
│   │   │   ├── mapper/         # Entity ↔ DTO conversion (11 mappers)
│   │   │   ├── model/          # Domain models grouped by feature
│   │   │   ├── repository/     # 13 Spring Data repositories
│   │   │   ├── security/       # JWT filter, UserDetails, SecurityUtils
│   │   │   └── service/        # 13 business logic services
│   │   └── test/               # 228 tests across 26 test classes
│   ├── docker-compose.yaml
│   └── .env.example
└── marketplace-frontend/
    ├── src/
    │   ├── api/                # Axios instance with token refresh interceptor
    │   ├── components/         # Feature-grouped React components
    │   ├── hooks/              # 11 custom hooks
    │   ├── pages/              # 14 page components
    │   ├── services/           # 11 API service modules
    │   ├── validation/         # Zod schemas
    │   └── ws/                 # WebSocket client (SockJS + STOMP)
    ├── e2e/                    # 6 Playwright test files
    └── .env.example
```
