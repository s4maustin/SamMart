# Acceptance Test Plan (ATP) — SamMart

Review artifact for Sep 21 full-build. Manual cases + automated suite (`mvn -B test`).

## 1. Scope

| ID | Feature | Automated | Manual |
| --- | --- | --- | --- |
| F1 | Register / login / session | AuthServiceTest, bcrypt test | TC-AUTH-* |
| F2 | Seller listings | Product search DAO | TC-SEL-* |
| F3 | Browse / keyword / category | `productSearchByKeyword` | TC-CAT-* |
| F4 | Cart add/update/remove | checkout tests use cart | TC-CART-* |
| F5 | Checkout + mock payment | `checkoutDecrementsStockAndClearsCart` | TC-ORD-* |
| F6 | Order history (buyer/seller) | order DAO after checkout | TC-ORD-* |
| F7 | Admin users/orders/remove listing | — | TC-ADM-* |
| F8 | Reviews on delivered orders | — | TC-REV-* |

## 2. Environment

- JDK 17+, Maven, Tomcat 9, H2 file DB `./data/sammart`
- Test DB: `jdbc:h2:mem:sammarttest;DB_CLOSE_DELAY=-1` loaded from `schema.sql` each run

## 3. Automated cases (CI)

| Test | Expected |
| --- | --- |
| Unique email | Second insert of same email fails at DB |
| Keyword search | “hub” returns 1; “banana” returns 0 |
| Checkout happy path | stock 2→0, cart empty, 1 order, payment SUCCESS |
| Oversell | stock forced to 1 after cart qty 2 → `ConflictException`, stock remains 1 (rollback) |
| Payment not confirmed | `ValidationException`, no order row |
| bcrypt | hash verifies; ADMIN self-register rejected |
| Auth mock | duplicate email → 409 conflict |

## 4. Manual test cases

### TC-AUTH-01 Register buyer
Steps: `/register` name, unique email, password ≥ 8, role BUYER.  
Expected: redirect login; `users.role='BUYER'`; `password_hash` starts with `$2a$` (not plaintext).

### TC-AUTH-02 No admin signup
Steps: POST role=ADMIN via form tamper or API.  
Expected: HTTP 400 VALIDATION_ERROR.

### TC-AUTH-03 Session regenerate
Steps: login.  
Expected: new `JSESSIONID`; timeout 30 min; protected `/cart` redirects to login when logged out.

### TC-CAT-01 Filter
Steps: home, pick category Electronics, keyword “USB”.  
Expected: only matching active rows; XSS string in name rendered escaped (`<c:out>`).

### TC-CART-01 Qty vs stock
Steps: add qty > stock.  
Expected: validation error, no `cart_items` row.

### TC-ORD-01 Place order transaction
Steps: buyer cart → checkout → confirm mock payment.  
Expected: `orders` PENDING; `order_items.unit_price` snapshot; `payments.status=SUCCESS`; `inventory_movements.reason=SALE` negative delta; `products.stock_qty` reduced; cart empty. If you kill DB mid-way, no partial order (repeat with forced exception in lab).

### TC-ORD-02 Seller incoming
Steps: login seller.  
Expected: order appears; status CONFIRMED → SHIPPED → DELIVERED writes `audit_logs`.

### TC-REV-01 Delivered only
Steps: review before DELIVERED.  
Expected: 403. After DELIVERED, one review; second review 409. Rating 6 rejected by CHECK + service.

### TC-ADM-01 Moderate
Steps: admin removes listing.  
Expected: `products.is_active=false`; product disappears from browse.

### TC-SEC-01 SQL injection
Steps: search `q=' OR 1=1 --`.  
Expected: empty or literal match; **PreparedStatement** only (`grep Statement)` → `PreparedStatement`).

### TC-SEC-02 XSS
Steps: register name `<script>alert(1)</script>`.  
Expected: header shows escaped text, no alert.

### TC-SEC-03 Authz
Steps: buyer GET `/seller/` or `/admin/`.  
Expected: 403 error page (no stack trace).

### TC-API-01 Envelope
Steps: GET `/api/v1/health`.  
Expected: HTTP 200 `{success:true,data:{status:UP,db:UP},error:null}`. Failed login HTTP 401 not 200.

## 5. Traceability

Every F-requirement has at least one TC. Checkout + stock is the **priority demo** for database/ATP-focused reviewers.

## 6. Load (optional before live URL)

`ab -n 200 -c 10 http://localhost:8080/sammart/api/v1/health` — 10 concurrent, no 5xx.

## 7. Sign-off

| Date | Build | Result |
| --- | --- | --- |
| 2026-09-19 | local `mvn test` — 10/10 pass | PASS |
