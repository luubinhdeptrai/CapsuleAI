**SoLi Food Delivery Platform**

Architecturally Significant Requirements

**ASR**

**Version 2.6**

Architecture Analysis Report

Academic Submission — Software Architecture Design

Architecture Style: Modular Monolith

Scope: NestJS Backend · Web Client · Mobile Client

Date: 2026-05-18

# Revision History

| **Version** | **Date** | **Author(s)** | **Summary** |
| --- | --- | --- | --- |
| 2.6 | 2026-05-18 | Lưu Bình | Audited — rechecked against backend, web, and mobile implementation; weak trace mappings, driver mismatches, client-fallback overclaims, and Appendix count ambiguity removed |

# Table of Contents

Revision History 2

Table of Contents 3

1. Introduction 4

1.1 Purpose 4

1.2 Implementation 4

2. Architectural Drivers 5

3. Architecturally Significant Functional Areas 7

3.1 Authentication & Account Management (UC-1) 7

3.2 Foundation & Customer Ordering Core (UC-2 – UC-9) 10

3.3 Restaurant & Delivery Operations (UC-11 – UC-19) 15

3.4 Customer Interaction, Promotion & Notification (UC-20 – UC-26) 21

3.5 Administration & Governance (UC-27 – UC-35) 26

4. Architectural Constraints 30

5. Cross-Cutting Concerns 31

5.1 Bounded Context Integration 31

5.2 Redis Usage 31

5.3 CQRS Usage 31

5.4 ACL Snapshot Tables 31

5.5 Error Strategy 32

5.6 Time and Geo 32

5.7 Authorization Layering 32

5.8 Background Jobs 32

6. Appendices 33

Appendix A — Out-of-Scope Items 33

# 1. Introduction

## 1.1 Purpose

This document captures the **Architecturally Significant Requirements (ASRs)** of the SoLi Food Delivery Platform. ASRs are the subset of requirements — both functional and non-functional — that exert direct, measurable influence on architectural decisions. They drive choices in module structure, runtime topology, data ownership, integration style, and quality-attribute tactics.

Unlike the SRS (which exhaustively enumerates functional requirements), the BRD (which describes business intent), or the ADD (which owns quality-attribute scenario bodies and architecture views), this document focuses on **architectural drivers, architecturally significant functional areas, architectural constraints, cross-cutting concerns, and traceability**.

## 1.2 Implementation

The implemented architecture is a **Modular Monolith** with the following characteristics:

* Bounded contexts: restaurant-catalog, ordering, payment, promotion, notification, image, auth
* Selective CQRS (@nestjs/cqrs): used for order placement (PlaceOrderCommand), order lifecycle transitions (TransitionOrderCommand), and payment IPN handling (ProcessIpnCommand); standard service/repository layering elsewhere
* In-process synchronous EventBus for cross-BC integration (no external message broker)
* Anti-Corruption Layer (ACL) snapshot projections maintained by event handlers
* Dependency-Inversion ports (PAYMENT\_INITIATION\_PORT, PROMOTION\_APPLICATION\_PORT) between Ordering and Payment / Promotion
* Single PostgreSQL database (Drizzle ORM) with module-scoped table groups
* Redis for cart state, idempotency keys, distributed locks, and WebSocket presence
* Socket.IO gateway for real-time notifications; VNPay payment gateway (HMAC-SHA512); Cloudinary image CDN; FCM push; Nodemailer email
* **Out of Scope (explicitly NOT in current architecture):** Microservices, service mesh, gRPC; Distributed tracing / OpenTelemetry; Message brokers (Kafka / RabbitMQ / SQS); Multi-region active-active deployment; API rate limiting via @nestjs/throttler (not currently registered)

# 2. Architectural Drivers

The following drivers shape the architecture and are referenced by ASRs throughout this document.

| **ID** | **Driver** | **Source** | **Architectural Impact** |
| --- | --- | --- | --- |
| **AD-1** | Exactly-once order creation under retry / network loss | UC-8 | Dual-layer idempotency (Redis key + DB UNIQUE(cart\_id)); ports & adapters; CQRS command for placement |
| **AD-2** | Integrity & non-repudiation of online payments | UC-9 | HMAC-SHA512 IPN verification before any state change; optimistic locking (version) on payment\_transactions; constant-time signature comparison |
| **AD-3** | Decoupling of bounded contexts under one deployable | Strategies §Modular Monolith Blueprint, ordering.module.ts | Synchronous in-process EventBus; ACL snapshot tables (ordering\_\*\_snapshots); DIP ports for outbound calls |
| **AD-4** | Real-time order-status visibility (≤ 5 s) | UC-20, UC-26 | Socket.IO gateway; Redis presence reference-counting; per-user rooms |
| **AD-5** | State-machine integrity of order lifecycle | UC-8, UC-14, UC-15, UC-18, UC-19,UC-21,.. | Hand-crafted TRANSITIONS map in constants/transitions.ts; enforcement + optimistic lock in TransitionOrderHandler; OrderLifecycleService handles ownership-only checks; order\_status\_logs audit trail |
| **AD-6** | Single-restaurant cart constraint | UC-4, UC-5 | Enforced in cart service before append; Redis-only cart store (no DB schema for carts) |
| **AD-7** | Delivery radius constraint | UC-7, UC-8 | Haversine in GeoService; ACL snapshot of delivery\_zones; validated synchronously in PlaceOrderHandler |
| **AD-8** | Partner approval integrity | UC-27, UC-28 | Partner approval — covering both Restaurant and Shipper onboarding — must be modelled as an **explicit state transition** (PENDING → APPROVED / REJECTED) owned exclusively by the Admin/Governance context. Each transition must record actorId, decidedAt, and an optional reason, enforced at the service boundary so no other context can elevate partner status directly. The current boolean isApproved field is a simplification that must be migrated to this model before Shipper onboarding is introduced. |
| **AD-9** | Graceful degradation of optional notification services | Vision & Scope §QA, notification module factories | Notification side-effect handlers log failures without blocking committed core flows |
| **AD-10** | Auditability of privileged actions | Quality Attribute (Supportability), use-case logging requirements | Structured logger usage; order\_status\_logs, payment\_transactions, notification\_delivery\_logs |
| **AD-11** | Public endpoint abuse control | QA-S-06, security requirements | Planned edge or Nest throttling for login, registration, and public search endpoints; current apps/api has no @nestjs/throttler registration |
| **AD-12** | Post-commit compensation reliability | QA-R-08; refund and promotion compensation flows | Post-commit module side effects for refund handling and promotion rollback must remain idempotent, failure-isolated, and operationally recoverable without implying distributed consistency infrastructure |

# 3. Architecturally Significant Functional Areas

The tables below capture **architecturally significant** Use Cases only — those whose requirements impose concrete decisions on module structure, runtime behavior, cross-BC integration, data ownership, or quality-attribute tactics. Architecturally routine Use Cases (UC-6, UC-10, UC-17, UC-31, UC-34) are intentionally excluded; they are governed by the general constraints in §4 and cross-cutting concerns in §5.

QA labels follow the course 14 Quality Attribute taxonomy exactly: **Performance, Availability, Reliability, Security, Scalability, Maintainability, Flexibility, Reusability, Interoperability, Conceptual Integrity, Usability, Testability, Supportability, Manageability**.

## 3.1 Authentication & Account Management (UC-1)

| **No.** | **Domain** | **Function** | **Description** | **Architectural Requirements** |
| --- | --- | --- | --- | --- |
| 1 | Authentication & Account Management | **Sign Up (UC-1)** | Customers, restaurant owners, and delivery personnel create platform accounts through Better Auth. New users default to the user role; restaurant ownership and shipper eligibility are handled by separate partner flows rather than by self-service role escalation. | **Security:**   * Password hashing via scrypt (Better Auth defaults); BETTER\_AUTH\_SECRET ≥ 32 chars enforced at startup by Zod env.schema.ts — app refuses to start on violation * Global ValidationPipe + class-validator DTOs; Drizzle parameterized queries prevent SQL injection * HTTPS enforced at reverse-proxy layer in production   **Reliability:**   * UNIQUE(email) on users table prevents duplicate accounts   **Performance:**   * Account creation API response p95 ≤ 2 s   **Interoperability:**   * expo() plugin supports Expo deep-link OAuth callbacks; phoneNumber() plugin provides the integration point for SMS-based OTP delivery   **Auditability:**   * Registration attempt, outcome, and timestamp captured in centralized audit logs |
| 2 |  | **Sign In (UC-1)** | Users provide email and password to authenticate and receive a bearer session token authorising all subsequent API requests, validated server-side on every call. | **Security:**   * Constant-time credential comparison (Better Auth internals); session tokens ≥ 128-bit entropy persisted in session table * Bearer plugin validates Authorization: Bearer <token> on every request * Brute-force protection via edge-proxy throttling or module-level throttling controls (QA-S-06)   **Reliability:**   * Session validation is stateless per-request; expired or revoked token rejected immediately without cache delay   **Performance:**   * Login API response p95 ≤ 1 s   **Auditability:**   * Failed login attempts observable via structured access logs |
| 3 |  | **Forgot / Reset Password (UC-1)** | Users who cannot sign in supply a registered email or phone; the system delivers a time-limited OTP. After successful verification the user sets a new password and all prior sessions are invalidated. | **Security:**   * OTP is time-limited, single-use, and invalidated after first successful verification * Reset submission over HTTPS only; phone OTP delivery uses the phoneNumber() plugin interface to integrate SMS providers * Reset endpoint does not disclose account existence (prevents account enumeration)   **Availability:**   * Email provider has Noop fallback when SMTP is unreachable — core platform flows unaffected   **Performance:**   * Password update API response p95 ≤ 2 s; OTP email delivered within 30–60 s   **Auditability:**   * Reset request, OTP delivery status, and outcome (success / failure) logged with timestamp |
| 4 |  | **Role-Based Access Control** | Protected endpoints authorize by the Better Auth role vocabulary (admin, restaurant, shipper, user). Lifecycle audit logs use customer as the order-actor label for the default consumer role. A user may hold multiple roles simultaneously (e.g., user,restaurant). | **Security:**   * user.role stores a comma-separated multi-role value; hasRole() in role.util.ts applies OR-logic — caller passes if any assigned role matches * Better Auth admin() plugin scopes admin-only endpoints; protected handlers deny unauthorized requests before service-layer mutation   **Conceptual Integrity:**   * APP\_ROLES = ['admin','restaurant','shipper','user'] constant in lib/auth.ts is the single Better Auth role vocabulary; order\_status\_logs.triggeredByRole separately records order actors (customer, restaurant, shipper, admin, system)   **Auditability:**   * Unauthorized attempts surface as 401 (no session) / 403 (insufficient role) in structured access logs |

## 3.2 Foundation & Customer Ordering Core (UC-2 – UC-9)

| **No.** | **Domain** | **Function** | **Description** | **Architectural Requirements** |
| --- | --- | --- | --- | --- |
| 1 | Foundation & Customer Ordering Core | **Discover Restaurants & Food (UC-2)** | Customers search for restaurants and menu items by keyword, location, or category. The system returns a paginated list of approved restaurants that are open for ordering. | **Performance:**   * p95 ≤ 2 s for first results page; paginated ≤ 20 items by default; public search filters restaurants.isApproved = true and restaurants.isOpen = true in restaurant-catalog/search   **Scalability:**   * Read-heavy path served by stateless API instances; Redis Cache-Aside supports hot-query acceleration at peak load   **Security:**   * Drizzle parameterized queries prevent SQL injection; ValidationPipe sanitizes all input; no ownerId or approval internals exposed in public results   **Interoperability:**   * Public browse/search reads Restaurant Catalog source tables directly; Ordering ACL snapshots are used by checkout and lifecycle ownership checks, not by the public search repository |
| 2 |  | **View Restaurant Details (UC-3)** | Customers view a restaurant's profile: menu categories, items, modifier groups, pricing, operating hours/open state, delivery zone coverage, and rating/review information. | **Performance:**   * Detail page API response p95 ≤ 2 s   **Interoperability:**   * Restaurant detail and menu reads are owned by Restaurant Catalog; separate events maintain Ordering ACL snapshots for checkout validation, not for public detail rendering   **Conceptual Integrity:**   * Rating and review information is incorporated into the restaurant detail model without introducing a parallel catalog representation |
| 3 |  | **Add Item to Cart (UC-4)** | Customers add a menu item with modifier choices to their cart. The system validates item availability and enforces the single-restaurant constraint (BR-2). Mixing items from different restaurants is rejected. | **Security:**   * Cart key scoped per authenticated customerId (cart:{customerId}); unauthenticated access rejected   **Performance:**   * Cart write p95 ≤ 50 ms (Redis O(1) per-customer-key operation)   **Reliability:**   * BR-2 single-restaurant constraint enforced in CartService before Redis write — returns CART\_RESTAURANT\_CONFLICT on mismatch; existing cart unchanged * Cart data is persisted with a sliding Redis TTL from CART\_ABANDONED\_TTL\_SECONDS (default 86 400 s); abandoned carts are evicted by Redis TTL rather than a background sweeper |
| 4 |  | **Manage Shopping Cart (UC-5)** | Customers view cart contents, update item quantities, remove individual items, or clear the cart before proceeding to checkout. | **Reliability:**   * Cart mutations are atomic per Redis key; checkout lock (cart:{customerId}:lock, SET NX EX 30) prevents concurrent order submissions for the same cart   **Usability:**   * Full cart payload returned on every mutation response — no separate refresh call required   **Security:**   * All cart operations require authenticated session; operations scoped to the caller's own cart key only |
| 5 |  | **Manage Delivery Zones (UC-7)** | Restaurant owners define geographic delivery coverage: radius, base fee, per-km rate, preparation time, and delivery buffer. Administrators may manage zones for any restaurant. | **Interoperability:**   * Every zone change publishes DeliveryZoneSnapshotUpdatedEvent; Ordering ACL projector upserts ordering\_delivery\_zone\_snapshots — UC-8 reads exclusively from this snapshot for fee computation and ETA, never crossing BC boundaries directly   **Reliability:**   * Zone snapshot upsert is idempotent (ON CONFLICT DO UPDATE); UC-8 always reads a consistent view even on event replay   **Performance:**   * Zone change propagated to checkout within ≤ 10 s (synchronous in-process event)   **Security:**   * Restricted to restaurant role (own restaurant only) or admin role; ownership verified in service layer   **Conceptual Integrity:**   * Single GeoService (Haversine) used for all distance computations across the system — no duplicated geo logic |
| 6 |  | **Place Order (UC-8)** | Customers submit their cart as a confirmed order, providing delivery address, optional notes, and payment method (COD or VNPay). The system validates delivery radius, applies any active promotion, captures a frozen pricing snapshot, and persists the order atomically. For VNPay, a payment redirect URL is returned immediately. | **Reliability:**   * Single Drizzle ACID transaction over orders, order\_items, order\_status\_logs; OrderPlacedEvent emitted after commit only — no phantom events on rollback * Dual-layer idempotency: Redis key idempotency:order:{key} (TTL = ORDER\_IDEMPOTENCY\_TTL\_SECONDS) as fast-path short-circuit plus DB UNIQUE(cart\_id) as backstop — zero duplicate orders on any client retry * Checkout lock (cart:{customerId}:lock, SET NX EX 30) prevents concurrent submissions for the same cart * Each order\_items row captures unit\_price, modifiers\_price, item name, and subtotal from ACL snapshot at order time — immune to subsequent catalog edits   **Performance:**   * End-to-end p95 ≤ 3 s including all ACL reads, Haversine validation, promotion reservation, and DB commit   **Security:**   * X-Idempotency-Key header validated and scoped to the authenticated customerId session token   **Auditability:**   * Initial order\_status\_logs entry created at placement with fromStatus = NULL (origin entry) |
| 7 |  | **Make Online Payment — VNPay (UC-9)** | Customers are redirected to VNPay's hosted payment page to complete payment. VNPay notifies the backend via a server-to-server IPN callback, driving the order to paid on success or triggering cancellation on failure or timeout. | **Security:**   * Redirect URL signed with HMAC-SHA512 over canonically ordered vnp\_\* params; VNPAY\_HASH\_SECRET never logged or surfaced in API responses * IPN signature verified with crypto.timingSafeEqual (constant-time comparison) before any state mutation   **Reliability:**   * IPN handler short-circuits on terminal-state detection — duplicate VNPay retries produce no state change * Optimistic lock (payment\_transactions.version) prevents concurrent mutations; UNIQUE(provider\_txn\_id) is the DB backstop * Payment amount validated against the stored transaction amount (BR-P4) before confirming * Pending transactions auto-expired after PAYMENT\_SESSION\_TIMEOUT\_SECONDS (env var, default 1 800 s) by PaymentTimeoutTask (@Cron(EVERY\_MINUTE)); emits PaymentFailedEvent → drives order cancellation   **Interoperability:**   * Strict conformance to VNPay specification: canonical parameter ordering, percent-encoding, VND-only currency, sandbox / live base-URL switch via env var   **Auditability:**   * payment\_transactions records status, amount, providerTxnId, expiresAt, and version for every lifecycle change |

## 3.3 Restaurant & Delivery Operations (UC-11 – UC-19)

| **No.** | **Domain** | **Function** | **Description** | **Architectural Requirements** |
| --- | --- | --- | --- | --- |
| 1 | Restaurant & Delivery Operations | **Restaurant Registration & Profile Management (UC-11)** | Restaurant owners register their business and manage their profile. New restaurants default to isApproved = false and isOpen = false; administrators can approve or unapprove the restaurant. Approved profile updates propagate to dependent bounded contexts. | **Security:**   * Restaurant role required for profile management; admin role required for approve / unapprove endpoints   **Reliability:**   * Approval uses isApproved and isOpen controls so only approved restaurants participate in public discovery and checkout   **Interoperability:**   * Create / update / approve / unapprove publishes RestaurantUpdatedEvent; Ordering ACL projector refreshes ordering\_restaurant\_snapshots; Notification ACL projector refreshes notification\_restaurant\_snapshots in-process   **Manageability:**   * Approval takes effect immediately in the source DB and propagates synchronously to local ACL tables in the same process   **Auditability:**   * Restaurant approval decisions persist admin actor, decision reason, and old/new approval state in auditable logs |
| 2 |  | **Manage Menu Catalog (UC-12)** | Restaurant owners create, update, and remove menu categories, items, modifier groups, and modifier options. Changes feed the checkout validation pipeline at the next order. | **Interoperability:**   * MenuItemUpdatedEvent published on create / update / sold-out changes; Ordering ACL projector upserts ordering\_menu\_item\_snapshots so UC-8 validates against a local Ordering read model * Images uploaded via Cloudinary signed upload; URL persisted in images table — image bytes never stored on backend   **Reliability:**   * ACL projectors are idempotent upserts (ON CONFLICT DO UPDATE); projection failures surface through operational logging and recovery handling * Snapshot propagation target ≤ 10 s through same-process event dispatch   **Security:**   * Restaurant owner verified against restaurantId ownership in service layer — a restaurant can only manage its own catalog |
| 3 |  | **Toggle Item & Restaurant Availability (UC-13)** | Restaurant owners mark menu items as sold out or available, and open or close their restaurant for orders. Availability changes take effect at checkout within seconds. | **Interoperability:**   * MenuItemUpdatedEvent / RestaurantUpdatedEvent published synchronously on every toggle; Ordering ACL snapshots updated in-process — UC-4 rejects out\_of\_stock items; UC-8 rejects closed restaurants at checkout   **Performance:**   * Availability change visible to customers ≤ 10 s under peak load (synchronous in-process pipeline)   **Conceptual Integrity:**   * isOpen is the single authoritative flag for restaurant order-acceptance; available / out\_of\_stock is the canonical item-level flag — no parallel availability signals anywhere in the system   **Security:**   * Toggle restricted to authenticated restaurant owner; restaurantId ownership verified in service layer |
| 4 |  | **Accept or Reject Order (UC-14)** | Restaurant operators accept or reject incoming orders within the configured window (default 600 s from RESTAURANT\_ACCEPT\_TIMEOUT\_SECONDS). Rejection requires a reason note. Post-VNPay-payment rejection triggers the refund pipeline. | **Reliability:**   * Closed TRANSITIONS map in constants/transitions.ts enforces T-01 (pending → confirmed), T-03 (pending → cancelled), T-04 (paid → confirmed), T-05 (paid → cancelled) — invalid transitions rejected with HTTP 422 * Optimistic lock (orders.version) prevents concurrent double-accept or race conditions * Auto-cancellation by OrderTimeoutTask (@Cron(EVERY\_MINUTE)) if restaurant does not respond within RESTAURANT\_ACCEPT\_TIMEOUT\_SECONDS; dispatched via TransitionOrderCommand through the same CQRS path   **Security:**   * Restaurant role with restaurantId ownership verification required — operator cannot act on another restaurant's orders   **Auditability:**   * Transition recorded in order\_status\_logs with triggeredBy UUID, triggeredByRole, note, and createdAt |
| 5 |  | **Prepare Order for Pickup (UC-15)** | Restaurant staff mark an accepted order as preparing and then ready for pickup. The system records the lifecycle transition and emits pickup-ready/status events for downstream consumers. | **Reliability:**   * T-06 (confirmed → preparing) and T-08 (preparing → ready\_for\_pickup) both routed through TransitionOrderCommand; idempotent via optimistic lock — duplicate submissions produce a conflict response, not a duplicate event * T-08 publishes OrderReadyForPickupEvent after commit and OrderStatusChangedEvent, supporting customer pickup-ready notifications and shipper dispatch workflows   **Performance:**   * Customer status notification delivered ≤ 5 s p95 via WebSocket / FCM   **Security:**   * Restaurant role with restaurantId ownership check required for this transition   **Auditability:**   * Transition recorded in order\_status\_logs with actor, role, and timestamp |
| 6 |  | **Shipper Registration (UC-16)** | Delivery personnel complete a shipper onboarding workflow under BR-1 and receive shipper eligibility after approval so delivery endpoints operate under an authenticated shipper role. | **Security:**   * Shipper onboarding uses an application and approval workflow so role assignment occurs only after approved eligibility review   **Reliability:**   * Application persistence, approval state machine, document review, and approval event coordinate onboarding and activation of delivery capabilities   **Auditability:**   * Approval decisions preserve audit history including actor, timestamp, submitted documents, and decision metadata |
| 7 |  | **Accept Delivery Assignment (UC-18)** | Shippers view ready\_for\_pickup orders and claim one by advancing T-09 (ready\_for\_pickup → picked\_up), with dispatch offers filtered by online availability and proximity. | **Reliability:**   * At-most-one assignment enforced by the same optimistic-lock status update that sets orders.shipperId during T-09; concurrent losers receive a conflict response   **Performance:**   * OrderReadyForPickupEvent and notification fan-out provide the pickup-ready signal; dispatch selection incorporates proximity and availability criteria   **Security:**   * Shipper role required; state machine ensures only ready\_for\_pickup orders can be claimed |
| 8 |  | **Deliver Order (UC-19)** | Shippers advance a claimed order through pickup, en-route, and delivered states. Upon delivery, the flow finalizes and notifies the customer. | **Reliability:**   * T-10 (picked\_up → delivering) and T-11 (delivering → delivered) both routed through TransitionOrderCommand; idempotent via optimistic lock — duplicate submissions produce a conflict, not a second event   **Performance:**   * Delivered status visible to customer ≤ 5 s p95 via WebSocket / FCM   **Security:**   * Only the shipper whose UUID matches orders.shipperId may execute T-10/T-11; unauthorized attempts return HTTP 403   **Auditability:**   * Delivery timestamp, shipper actor UUID, and role recorded in order\_status\_logs |

## 3.4 Customer Interaction, Promotion & Notification (UC-20 – UC-26)

| **No.** | **Domain** | **Function** | **Description** | **Architectural Requirements** |
| --- | --- | --- | --- | --- |
| 1 | Customer Interaction, Promotion & Notification | **Track Order Status (UC-20)** | Customers monitor the progression of their active order through pushed status updates, durable notification history, and order-detail refresh paths. | **Performance:**   * Status update delivered ≤ 5 s p95 target: TransitionOrderCommand commit → OrderStatusChangedEvent dispatch → persisted notification → WebSocket emit to room:user:{userId}; client channels refresh visible order state from notification-driven updates   **Availability:**   * Backend supports recovery through REST notification inbox reads, reconnecting notification sockets, unread-count refresh, and order-detail refresh flows across clients after disconnect   **Reliability:**   * OrderStatusChangedEvent emitted only after successful DB commit — no phantom events on transaction rollback * Redis presence ref-count (ws:connections:{userId}) + per-socket expiry timer cleared in handleDisconnect — prevents WebSocket connection resource leaks   **Security:**   * Socket.IO connection authenticated server-side via bearer token (userId resolved at connect); per-user rooms (room:user:{userId}) prevent cross-user notification observation * Customer-scoped order detail and timeline reads enforce ownership before returning data |
| 2 |  | **Cancel Order (UC-21)** | Customers cancel an active order before pickup. Pre-payment (COD) cancellations transition directly to cancelled; post-VNPay-payment cancellations additionally trigger the refund pipeline. | **Reliability:**   * T-03 (pending → cancelled) for COD; T-05 (paid → cancelled) for VNPay — both routed through TransitionOrderCommand — same closed state machine applies to all actors * Post-payment cancellation publishes OrderCancelledAfterPaymentEvent; refund handler failure (UC-25) is isolated and never rolls back the cancellation   **Security:**   * orders.customerId ownership enforced at service layer; HTTP 404 returned for non-owned orders (prevents order-existence enumeration)   **Auditability:**   * Recorded in order\_status\_logs with triggeredByRole = 'customer', note, and createdAt |
| 3 |  | **Submit Rating & Review (UC-22)** | Customers submit ratings and reviews for delivered orders. Review persistence, moderation, and rating-propagation events feed restaurant detail and feedback workflows. | **Reliability:**   * One review is allowed per delivered order per customer   **Security:**   * An authenticated customer may review only their own delivered order   **Conceptual Integrity:**   * Reviews reference orders by UUID and use the existing orderStatusEnum delivered state as the eligibility gate   **Supportability:**   * Moderation preserves review history rather than hard-deleting records |
| 4 |  | **Manage Restaurant Promotions (UC-23)** | Restaurant owners create, configure, activate, pause, and deactivate promotions (percentage / flat discounts, optional coupon codes, usage caps, validity windows) scoped to their restaurant. | **Reliability:**   * 4-phase reservation at checkout: preview (read-only eligibility) → computeAndReserve (atomic counter increment + reservation row) → confirm (on order success) → rollback (compensating write on failure) — discount never applied to a failed order   **Flexibility:**   * IPromotionApplicationPort (DIP token PROMOTION\_APPLICATION\_PORT) decouples Ordering BC from all Promotion BC internals — zero concrete Promotion imports in module/ordering   **Security:**   * Restaurant owner scoped to own restaurantId; ownership enforced in service layer   **Conceptual Integrity:**   * Promotion state machine (draft → active → paused → expired) enforced at service layer; disallowed transitions return HTTP 422 |
| 5 |  | **Manage Platform Promotions (UC-24)** | Platform administrators create and manage platform-wide promotions and generate coupon-code batches, targeting all restaurants or a specific one. | **Security:**   * Admin role required for all platform-scope operations; platform vs restaurant scope constraints validated in service code   **Reliability:**   * UNIQUE(code) at DB level; duplicate code raises ConflictException immediately — no silent retry or skip   **Conceptual Integrity:**   * Same Promotion schema and status rules as UC-23 — no separate admin-only promotion aggregate   **Supportability:**   * Promotion administration records persistent actor UUID audit trails for platform-scope operations |
| 6 |  | **Process Payment Refund (UC-25)** | When a VNPay-paid order is cancelled, Payment BC handles refund compensation asynchronously without blocking the committed order cancellation, including the external refund interaction with VNPay. | **Reliability:**   * OrderCancelledAfterPaymentHandler in Payment BC transitions payment\_transactions from completed to refund\_pending to refunded using optimistic locking; handler exception swallowed and logged — cancellation is never rolled back due to refund failure   **Conceptual Integrity:**   * Payment BC is the sole component responsible for VNPay financial state; Ordering BC only publishes the domain event — no direct VNPay API calls from module/ordering   **Interoperability:**   * VNPay refund API interaction and retry handling are encapsulated in Payment BC and emit customer order\_cancelled / refund\_initiated notifications through Notification BC   **Auditability:**   * Refund state transitions recorded in payment\_transactions; customer notifications persisted in Notification BC |
| 7 |  | **Manage Real-Time Notifications (UC-26)** | Users receive in-app, FCM push, and email notifications for order and payment events. Users view their inbox, mark messages as read, and manage device tokens for push delivery. | **Interoperability:**   * Multi-channel dispatch via ChannelDispatcherService; provider abstractions (EmailProvider, PushProvider) with Noop / Stub fallback — order and payment flows never blocked by notification failures   **Availability:**   * Provider failure isolated per channel; core flows (order placement, payment IPN) entirely unaffected by notification errors   **Performance:**   * In-app notification via WebSocket ≤ 5 s p95 target; FCM and email dispatched asynchronously   **Reliability:**   * notifications table provides durable inbox; survives WebSocket disconnection; in-app rows are assigned a 90-day expiresAt * Push device tokens cleaned up by DeviceTokenCleanupTask on stale registrations   **Security:**   * Socket.IO connection authenticated at connect via bearer token; push tokens registered per user-device pair   **Supportability:**   * Every dispatch attempt logged in notification\_delivery\_logs with channel, outcome, and error detail for failed attempts |

## 3.5 Administration & Governance (UC-27 – UC-35)

| **No.** | **Domain** | **Function** | **Description** | **Architectural Requirements** |
| --- | --- | --- | --- | --- |
| 1 | Administration & Governance | **Approve or Reject Restaurant Applications (UC-27)** | Administrators approve or unapprove restaurant registrations through boolean isApproved. Approved and open restaurants become visible in public catalog; ACL snapshots are refreshed in dependent BCs. | **Security:**   * Admin role required; approval restricted to authenticated admin session   **Interoperability:**   * Approval/unapproval publishes RestaurantUpdatedEvent; Ordering and Notification ACL snapshots refresh through the shared in-process integration pipeline   **Manageability:**   * Decision takes effect immediately in the source table and the administrative approval workflow   **Auditability:**   * Decision reason, admin actor UUID, and old/new approval state persist in approval audit records |
| 2 |  | **Approve or Reject Shipper Applications (UC-28)** | Administrators review shipper applications through an approval queue that drives role elevation, onboarding, and decision audit. | **Security:**   * shipper role elevation is admin-only and non-self-service   **Reliability:**   * An application state machine prevents re-deciding approved or rejected submissions   **Auditability:**   * Decision history records admin actor, target applicant, reason, and timestamp |
| 3 |  | **Suspend or Reactivate Partner Accounts (UC-29)** | Administrators suspend or reactivate partner accounts. Suspension coordinates Better Auth user status, restaurant approval, shipper eligibility, and operational access. | **Security:**   * Suspension and reactivation are exposed only through admin-authorized controls   **Reliability:**   * Suspension rules define effects on restaurants.isApproved, shipper eligibility, and in-flight orders explicitly   **Auditability:**   * Suspension and reactivation history records admin actor, target account, reason, action, and effective timestamp |
| 4 |  | **Monitor Orders and Platform Health (UC-30)** | Administrators view a filtered, paginated list of all platform orders across all restaurants together with platform-health KPIs, anomaly flags, and stuck-order diagnostics. | **Performance:**   * Query p95 target ≤ 2 s; paginated query layer uses dynamic filters and aggregate subqueries to avoid N+1 list reads   **Security:**   * Admin role required; all restaurants' orders visible (unlike restaurant-scoped views)   **Manageability:**   * Filters: status, restaurantId, customerId, shipperId, paymentMethod, date range; sort: created\_at, updated\_at, total\_amount   **Scalability:**   * Read-replica or pre-aggregated monitoring views support high-volume monitoring |
| 5 |  | **Administrative Order Cancellation & Refund (UC-32)** | Administrators cancel permitted in-progress orders regardless of ownership and can trigger the post-delivery refund transition. VNPay refund compensation is handled by Payment BC through the shared refund event pipeline. | **Security:**   * Admin authority encoded in allowedRoles entries in the TRANSITIONS map — bypasses customer / restaurant ownership gate without a separate codepath   **Reliability:**   * Cancellation/refund routed through TransitionOrderCommand — same closed TRANSITIONS map as all actors; no state-machine bypass for admin * Post-payment cancellation publishes OrderCancelledAfterPaymentEvent for Payment BC compensation (UC-25)   **Conceptual Integrity:**   * No separate admin cancellation codepath — admin authority is a configuration in allowedRoles; single state machine applies to every actor   **Auditability:**   * Recorded in order\_status\_logs with triggeredByRole = 'admin'; mandatory reason note (requireNote: true in TRANSITIONS map entry) |
| 6 |  | **View and Export Operational Reports (UC-33)** | Administrators generate operational reports covering order volumes, GMV by restaurant, delivery performance, and promotion effectiveness, exportable as CSV / PDF. | **Performance:**   * Asynchronous generation for large date ranges; recent-period summaries p95 ≤ 5 s   **Security:**   * Admin role required; HTTPS transmission; PII minimized in exports to necessary identifiers   **Scalability:**   * Long-range reports leverage read-replica or pre-aggregated analytics snapshot tables   **Auditability:**   * Report access (actor, parameters, timestamp) logged |
| 7 |  | **Manage Admin Roles & Permissions (UC-35)** | Administrators assign or revoke privileged roles through dedicated role-management controls with a last-admin safeguard and persistent role-change audit. | **Security:**   * Role assignment and revocation are admin-only and prevent self-service privilege escalation * A last-admin safeguard prevents full system lockout   **Reliability:**   * Last-admin checks and role updates execute atomically   **Conceptual Integrity:**   * Role management reuses user.role plus hasRole() OR-logic as the authorization vocabulary   **Auditability:**   * Role-change audit records actor UUID, target UUID, old role value, new role value, and timestamp |

# 4. Architectural Constraints

The table below captures structural constraints that govern all bounded contexts and cross-cutting decisions throughout the platform.

| **ID** | **Constraint** | **Rationale** | **Implication** |
| --- | --- | --- | --- |
| **C-1** | **Modular Monolith — single deployable** | MVP scale; reduces operational complexity | All BCs live in one process; horizontal scaling = scale the whole app; cross-BC integration uses in-process EventBus + DIP ports |
| **C-2** | **PostgreSQL single primary** | Strong transactional semantics needed for order placement & payment | Cross-BC consistency through ACID transactions inside one BC + in-process events between BCs; no distributed transactions |
| **C-3** | **In-process synchronous EventBus (no broker)** | Operational simplicity for MVP | Replicated full-application instances are valid for scaling the modular monolith, but separating publishers and listeners into different deployables is not supported without first introducing an external broker — see QA-SC-01 |
| **C-4** | **NestJS + Drizzle + Better Auth** | Selected by team; community-supported; type-safe | All modules use NestJS DI; schemas declared via Drizzle; auth routes auto-managed by Better Auth |
| **C-5** | **Vietnamese market (VNPay only for MVP)** | Business requirement (BR-4) | VNPay-specific signature, currency in VND, integration adapter; no PCI scope (no card data stored) |
| **C-6** | **HTTPS everywhere in production** | OWASP, payment integration | TLS termination at reverse proxy; not enforced inside the Node process |
| **C-7** | **Single-region deployment** | Cost / scope for MVP | RTO / RPO and backup automation are not formalized; PostgreSQL-native backup is a deployment responsibility; multi-region failover is post-MVP |
| **C-8** | **Mobile and web clients are separate apps** | Distinct UX | Shared OpenAPI / Better Auth contract; backend agnostic to client kind |
| **C-9** | **TypeScript end-to-end** | Type-safety, monorepo turborepo | Shared types impossible across packages without explicit publication; current codebase keeps API types internal |

# 5. Cross-Cutting Concerns

## 5.1 Bounded Context Integration

* All inter-BC communication uses in-process NestJS EventBus (synchronous) or DIP ports.
* Ordering → Payment: PAYMENT\_INITIATION\_PORT (injects PaymentService)
* Ordering → Promotion: PROMOTION\_APPLICATION\_PORT (injects PromotionService)
* Catalog mutations → Ordering ACL: RestaurantUpdatedEvent, MenuItemUpdatedEvent, DeliveryZoneSnapshotUpdatedEvent
* Order/Payment events → Notification: OrderStatusChangedEvent, PaymentFailedEvent, OrderCancelledAfterPaymentEvent
* Restaurant/Catalog events → Notification ACL: RestaurantUpdatedEvent
* No cross-module Drizzle schema imports exist; each BC owns its table definitions.

## 5.2 Redis Usage

* Cart storage: cart:{customerId} (hash, sliding TTL = CART\_ABANDONED\_TTL\_SECONDS)
* Checkout lock: cart:{customerId}:lock (SET NX EX 30) — prevents concurrent order submissions
* Order idempotency key: idempotency:order:{key} (TTL = ORDER\_IDEMPOTENCY\_TTL\_SECONDS)
* WebSocket presence ref-count: ws:connections:{userId} (incremented on connect, decremented on disconnect; per-socket expiry timer cleared in handleDisconnect)

## 5.3 CQRS Usage

* Used selectively via @nestjs/cqrs — not applied globally.
* PlaceOrderCommand: Place Order handler (UC-8)
* TransitionOrderCommand: All lifecycle transitions (UC-14, UC-15, UC-18, UC-19, UC-21, UC-32) and timeout tasks
* ProcessIpnCommand: VNPay IPN handling (UC-9)
* Standard NestJS service/repository layering used everywhere else.

## 5.4 ACL Snapshot Tables

* ordering\_restaurant\_snapshots: populated by RestaurantUpdatedEvent projector; consumed by PlaceOrderHandler and OrderLifecycleService for ownership / open-state checks
* ordering\_menu\_item\_snapshots: populated by MenuItemUpdatedEvent projector; consumed by CartService (availability) and PlaceOrderHandler (pricing)
* ordering\_delivery\_zone\_snapshots: populated by DeliveryZoneSnapshotUpdatedEvent projector; consumed by PlaceOrderHandler (Haversine + fee)
* notification\_restaurant\_snapshots: populated by RestaurantUpdatedEvent projector in Notification BC; consumed by Notification handlers for restaurant name resolution

## 5.5 Error Strategy

* Core flow handlers (PlaceOrderHandler, TransitionOrderHandler, ProcessIpnCommand): propagate exceptions; HTTP layer maps to appropriate 4xx/5xx.
* Notification side-effect handlers: swallow and log exceptions — core flows are never blocked.
* ACL projectors: log and rethrow on snapshot write failure — projection failures are observable.
* Payment refund handler: swallows exceptions after logging — cancellation is never rolled back on refund failure (AD-12).

## 5.6 Time and Geo

* All timestamps stored in UTC via timestamp with time zone.
* Distances computed via Haversine on stored decimal lat / lng pairs in GeoService; planned PostGIS migration is not required at MVP scale.

## 5.7 Authorization Layering

* Better Auth issues session tokens; bearer plugin extracts on every request.
* NestJS route guards verify session presence; service / handler code calls hasRole() for fine-grained role checks.
* ACL snapshots in Notification BC supply restaurant ownership without coupling to the restaurants table.

## 5.8 Background Jobs

* Background jobs use @nestjs/schedule cron / interval triggers.
* Payment timeout sweeper (payment-timeout.task.ts): runs every minute; expires payment\_transactions past expiresAt (set from PAYMENT\_SESSION\_TIMEOUT\_SECONDS env var); publishes PaymentFailedEvent.
* Order timeout sweeper (order-timeout.task.ts): runs every minute; auto-cancels orders past expiresAt (set from RESTAURANT\_ACCEPT\_TIMEOUT\_SECONDS app\_setting, default 600 s); dispatches TransitionOrderCommand so T-03/T-05 run through the same CQRS path.
* Device-token cleanup (device-token-cleanup.task.ts).
* WebSocket connection metrics (@Interval in NotificationGateway) and client-driven heartbeat refresh of Redis presence TTL.
* State-changing timeout/cleanup tasks are designed to be idempotent; WebSocket metrics are observational only.

# 6. Appendices

## Appendix A — Out-of-Scope Items

The following are commonly listed in enterprise ASRs but are deliberately excluded from the SoLi MVP and must not be assumed present:

* Microservices, service mesh, Kubernetes operators
* Message brokers (Kafka, RabbitMQ, NATS, SQS)
* Distributed transaction coordination
* Cross-service eventual consistency infrastructure
* Service discovery
* Distributed tracing (OpenTelemetry / Jaeger / Zipkin / APM)
* Multi-region active-active or active-passive failover
* Saga orchestrator framework (Temporal, AWS Step Functions)
* Outbox pattern infrastructure (events are in-process synchronous)
* Formal SLOs / error budgets / chaos engineering practice
* API rate limiting in the NestJS app (relies on edge / reverse proxy when introduced)
* PCI DSS scope (no card data ever stored)
* MFA / FIDO2 (not in MVP — see BR-4 / SRS)
* Identity federation (OAuth / OIDC IdP) — Better Auth expo() plugin only for Expo deep-links

When the platform evolves past MVP, these items become candidate ASRs and should be re-introduced through a formal architecture review.