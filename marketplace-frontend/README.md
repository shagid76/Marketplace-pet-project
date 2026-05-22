# Marketplace — Frontend

React 19 + TypeScript single-page application for a peer-to-peer digital marketplace. Buyers browse, search, and purchase products; sellers list items and manage inventory; moderators handle reports and bans — all in one UI.

---

## Tech stack

| Layer | Choice |
|---|---|
| Framework | React 19 |
| Language | TypeScript (strict, no `any`) |
| State management | Redux Toolkit |
| Routing | React Router 7 |
| Forms & validation | React Hook Form + Zod |
| HTTP client | Axios with JWT interceptors |
| Real-time | WebSocket/STOMP (chat) + SSE (notifications) |
| Styling | SCSS / BEM |
| Payments | Stripe Checkout (redirect flow) |

---

## Pages

| Route | Description |
|---|---|
| `/` | Home — featured and new listings |
| `/search` | Full-text + category + price filter search (Elasticsearch) |
| `/product/:id` | Product detail with gallery, promo code, and buy panel |
| `/create-product` | Create a new listing with image upload |
| `/update-product` | Edit an existing listing |
| `/cart` | Shopping cart with promo code support |
| `/chat` / `/chat/:id` | Real-time direct messaging |
| `/me` | Authenticated user's own profile |
| `/profile-update` | Edit profile and avatar |
| `/user/:id` | Public seller profile with reviews |
| `/admin` | Moderation panel (reports, bans, promo codes) |
| `/login` | Sign in |
| `/register` | Create account |

---

## Key features

**Authentication** — JWT access token (30 min) + refresh token (5 days) stored in localStorage. Axios interceptor silently refreshes on 401 and retries the original request.

**Product search** — connects to the Elasticsearch-backed `/api/products/search` endpoint; supports free-text query, category filter, and min/max price range.

**Real-time chat** — WebSocket connection via STOMP. Messages stream in without polling; deleted messages are removed immediately across all open windows.

**Live notifications** — SSE subscription (`/api/events/subscribe`) delivers moderation events (ban, warning) to the user in real time without polling.

**Stripe payments** — `ProductBuyPanel` validates an optional promo code, calculates the discounted price client-side, and redirects to a Stripe Checkout session.

**Role-based UI** — Admin panel is only rendered for `ROLE_MODERATOR` / `ROLE_ADMIN` users. Seller actions (edit/delete listing) are shown only to the product owner.

---

## Project structure

```
src/
├── api/            # Axios instance + interceptors
├── app/            # Redux store + slices
├── components/     # Reusable UI components (BEM SCSS)
├── hooks/          # Custom React hooks
├── pages/          # Route-level page components
├── services/       # API call functions (typed, no Promise<any>)
├── types/          # TypeScript interfaces
├── utils/          # Formatters, helpers
├── validation/     # Zod schemas
└── ws/             # WebSocket / STOMP client
```

---

## Getting started

```bash
# Install dependencies
npm install

# Start dev server
npm start

# Run tests
npm test

# Production build
npm run build
```

The app expects the backend running at `http://localhost:8080`. Configure the base URL in `src/api/axiosInstance.ts`.

---

## Backend

See [`../marketplace-backend/README.md`](../marketplace-backend/README.md) for the full API reference, infrastructure setup, and environment variables.
