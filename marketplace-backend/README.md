# Marketplace Backend

A full-featured REST API for a peer-to-peer marketplace — built with Spring Boot 3, Java 21, and a modern infrastructure stack. Users can list products, buy via Stripe, chat in real time, and receive live moderation notifications.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4, Java 21 |
| Database | MongoDB |
| Search | Elasticsearch |
| File Storage | MinIO (S3-compatible) |
| Payments | Stripe Checkout |
| Auth | JWT (access + refresh tokens) |
| Real-time | WebSocket (chat) + SSE (notifications) |
| Build | Gradle |

---

## Features

- **Auth** — Registration, login, JWT refresh token rotation (access token 30 min / refresh token 5 days)
- **Products** — Create, update, delete listings with multi-image upload; full-text search via Elasticsearch with category and price filters
- **Cart** — Per-user cart with stock and ban validation
- **Wishlist** — Save and remove products on your profile
- **Payments** — Stripe Checkout sessions with optimistic locking to prevent double-purchases; expired payment auto-release via scheduler
- **Promo Codes** — Create Stripe-backed discount coupons (percentage or fixed), validate eligibility per user, category, and cart total
- **Reviews** — One review per user per seller; average rating aggregation
- **Chat** — Real-time WebSocket messaging between users; full chat history
- **Notifications** — SSE push events for account ban/block actions
- **Moderation** — Admin actions: ban/block users, ban products and reviews, revoke and extend actions
- **Reports** — Users can report content; moderators archive resolved reports

---

## Getting Started

### Prerequisites

- Java 21
- Docker (for MongoDB, Elasticsearch, MinIO)

### Environment Variables

Create a `.env` file in the project root (see `.env.example`):

```env
DB_USERNAME=
DB_PASSWORD=
DB_HOST=localhost
DB_PORT=27017

URIS=http://localhost:9200

JWT_SECRET=          # hex-encoded secret, min 64 chars

S3_ENDPOINT=http://localhost:9000
BUCKET_NAME=marketplace
ACCESS_KEY=
SECRET_KEY=
DEFAULT_AVATAR_PATH=

STRIPE_WEBHOOK_SECRET=
API_KEY=             # Stripe secret key

FRONTEND_ORIGIN=http://localhost:5173
```

### Run

```bash
./gradlew bootRun
```

---

## API Overview

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/registration` | Register a new user |
| POST | `/api/auth/sign-in` | Login, returns token pair |
| POST | `/api/auth/refresh` | Refresh access token |

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/{id}` | Get public user profile |
| GET | `/api/users/me` | Get own profile |
| PUT | `/api/users/update` | Update username / avatar |
| DELETE | `/api/users/delete` | Delete own account |
| POST | `/api/users/wishlist/{productId}` | Add to wishlist |
| DELETE | `/api/users/wishlist/{productId}` | Remove from wishlist |

### Products
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | Paginated product list |
| GET | `/api/products/{id}` | Product detail |
| GET | `/api/products/search` | Search (query, category, price range) |
| GET | `/api/products/new-items` | Latest listings |
| GET | `/api/products/category/{category}` | Filter by category |
| POST | `/api/products/create` | Create listing (multipart) |
| PUT | `/api/products/update/{id}` | Update listing |
| DELETE | `/api/products/delete/{id}` | Delete listing |

### Cart
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | Get cart |
| POST | `/api/cart/create` | Create cart |
| POST | `/api/cart/add/{productId}` | Add product |
| DELETE | `/api/cart/remove/{productId}` | Remove product |
| DELETE | `/api/cart/clear` | Clear cart |

### Payments
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/payments/create` | Create Stripe Checkout session |
| POST | `/api/payments/webhook` | Stripe webhook handler |

### Promo Codes
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/promo-codes` | List all (admin) |
| GET | `/api/promo-codes/{code}` | Get by code |
| POST | `/api/promo-codes/create` | Create (admin) |
| POST | `/api/promo-codes/check` | Validate for cart |
| PATCH | `/api/promo-codes/deactivate/{id}` | Deactivate |

### Reviews
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reviews/{targetId}` | Reviews for a user |
| GET | `/api/reviews/average/{targetId}` | Average rating |
| POST | `/api/reviews/create` | Create or update review |
| DELETE | `/api/reviews/delete/{id}` | Delete own review |

### Chat & Messages
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/chats` | My chats (with last message) |
| GET | `/api/chats/{id}` | Chat with full history |
| POST | `/api/chats/create` | Start a chat |
| DELETE | `/api/chats/delete/{id}` | Delete chat |
| POST | `/api/messages/create` | Send message |
| DELETE | `/api/messages/delete/{id}` | Delete message |

### Moderation
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/admin-actions` | Create ban/block action |
| GET | `/api/admin-actions/active` | Get active action for target |
| DELETE | `/api/admin-actions/revoke/{id}` | Revoke action |
| PATCH | `/api/admin-actions/extend/{id}` | Extend expiry |

### Real-time
| Type | Endpoint | Description |
|---|---|---|
| SSE | `/api/events/subscribe` | Account notifications (ban/block) |
| WebSocket | `/ws` | Chat messaging |

---

## Architecture Notes

- **Optimistic locking** on product purchase — concurrent checkout attempts are caught via `OptimisticLockingFailureException` and handled gracefully
- **Dual write** — every product write goes to both MongoDB (source of truth) and Elasticsearch (search index)
- **Payment expiry scheduler** — runs periodically to unlock products from abandoned Stripe sessions
- **SSE fan-out** — multiple browser tabs per user are supported via `CopyOnWriteArrayList` per userId

---

## Tests

228 tests across service and controller layers, using Mockito and Spring's `@WebMvcTest` slice.

```bash
./gradlew test
./gradlew test jacocoTestReport   # coverage report → build/reports/jacoco/
```