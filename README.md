# SamMart

Multi-seller marketplace for Anna University R2025 Semester 3 (Java Servlets · JDBC · Tomcat 9).

**Problem statement:** Sellers list products. Buyers browse, search, cart, and purchase with mock payment. Admin manages users, orders, and listings. Strength of the system is in the relational schema, transactional checkout, and the acceptance test plan — not the UI.

## Tech stack

| Component | Choice |
| --- | --- |
| JDK | 17 (LTS) — compile target |
| Container | Tomcat 9.0.x (`javax.servlet.*`) |
| Build | Maven |
| Database | H2 (file/server for app, in-memory for tests) |
| Pool | HikariCP via `DataSourceListener` |
| View | JSP + JSTL; JSON API under `/api/v1/` |
| Hashing | jBCrypt |
| Tests | JUnit 5 + Mockito against `jdbc:h2:mem:...` |

## Architecture

Browser (JSP / fetch) → Filters (encoding, auth, request id) → Front controller (`ApiServlet`) or page servlets → Service (validation, checkout transaction) → DAO (`PreparedStatement` only) → HikariCP → H2.

Mandatory diagrams: [D1 ER](docs/D1-ER.md) · [D2 Use case](docs/D2-use-case.md) · [D3 Place-order sequence](docs/D3-sequence.md)

Acceptance tests: [docs/ATP.md](docs/ATP.md)

## Database (what to show reviewers)

Beyond the spec minimum:

- Normalized `categories` (not a free-text product category)
- `payments` (one row per order, unique `transaction_ref`)
- `inventory_movements` (SALE/RESTOCK audit of stock)
- `audit_logs` for admin/seller status changes
- `DECIMAL(10,2)` money, **no FLOAT**
- Unique email, unique cart line, unique review per user+product
- Check constraints on role, status, rating 1–5, price ≥ 0.01, stock ≥ 0
- **Index on every foreign key** plus status/name search indexes
- Views `v_product_rating` and `v_seller_sales`
- Checkout is **one JDBC transaction**: `SELECT … FOR UPDATE` → insert order/items/payment/movements → decrement stock → clear cart → commit / rollback

Schema: `db/schema.sql` and `db/migrations/V1__init_schema.sql`. Seed: `db/seed.sql` plus bcrypt users created on first boot.

## Local setup

1. JDK 17+ and Maven 3.9.
2. Copy `src/main/resources/config.properties.example` to `config.properties` if needed (already present for local H2).
3. `mvn -B clean verify`
4. `mvn -B package`
5. Deploy `target/sammart.war` to **Tomcat 9** (not Tomcat 10 — that is Jakarta, this project is `javax.servlet`).
6. Open `http://localhost:8080/sammart/`

Demo accounts (bcrypt, seeded once):

- `admin@sammart.local` / `Admin@123`
- `seller@sammart.local` / `Seller@123`
- `buyer@sammart.local` / `Buyer@123`

Health: `GET /sammart/api/v1/health` → `{ "status": "UP", "db": "UP" }`

## Design patterns (report talking points)

| Pattern | Where |
| --- | --- |
| DAO | `dao` interfaces + JDBC impls |
| Front controller | `ApiServlet` `/api/v1/*` |
| Singleton | Hikari pool in `DataSourceListener` |
| Factory | `DaoFactory` |
| Strategy | `PaymentChannel` / `MockPaymentChannel` |
| Builder | `CheckoutRequest.Builder` |

## Deployed link

Live demo :

- App: https://sammart-r2ze.onrender.com/sammart/


Restart with `powershell -File scripts/start-public.ps1` if Tomcat or the tunnel stops. The trycloudflare host changes each restart.

## Features F1–F8

Registration/login (Buyer/Seller; Admin seed only), seller CRUD listings, browse/search/filter, cart, mock checkout, buyer/seller/admin order views, admin listing removal, reviews on **delivered** orders.
