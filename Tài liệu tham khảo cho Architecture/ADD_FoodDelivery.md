**ARCHITECTURE DESIGN**

**ATTRIBUTE-DRIVEN DESIGN DOCUMENT**

**ADD**

SoLi Food Delivery Platform

Architecture: Modular Monolith

|  |  |
| --- | --- |
| **Prepared by** | Lưu Bình |
| **Version** | 1.0 |
| **Document Category** | Architecture Design Report |
| **Submission** | Software Architecture Design — Academic Submission |

# Table of Contents

Table of Contents 2

1. Design Constraints 4

2. Quality Attribute Requirements 10

2.1. Performance 10

QA-P-01 — Restaurant Search Response Time 10

QA-P-02 — Order Status Propagation to Customer 10

QA-P-03 — Checkout End-to-End Latency 11

QA-P-04 — Menu / Availability Update Propagation 11

2.2. Availability 12

QA-A-01 — Authentication Endpoint Availability 12

QA-A-02 — Real-Time Channel Graceful Degradation 12

QA-A-03 — Optional Notification-Channel Degradation 13

2.3. Reliability 13

QA-R-01 — Order Placement Idempotency 13

QA-R-02 — Payment IPN Webhook Idempotency 14

QA-R-03 — Order State-Machine Integrity 14

QA-R-04 — Single-Restaurant Cart Invariant 15

QA-R-05 — Atomic Shipper Assignment 15

QA-R-06 — Payment Timeout Recovery 16

QA-R-07 — Restaurant Acceptance Timeout 17

QA-R-08 — Refund and Promotion Compensation Reliability 17

2.4. Security 18

QA-S-01 — VNPay Callback Integrity 18

QA-S-02 — Authentication & Session Management 19

QA-S-03 — Role-Based Authorization 19

QA-S-05 — Input Validation & Injection Resistance 20

QA-S-06 — Rate Limiting on Public Endpoints 20

2.5. Scalability 21

QA-SC-01 — Horizontal Scaling of API Instances 21

QA-SC-02 — Cart and Idempotency Storage Scaling 21

2.6. Flexibility 22

QA-FL-01 — Generalizing Payment Provider Integration 22

QA-FL-02 — Adding a New Order Status 22

QA-FL-03 — Replacing a Notification Channel Provider 23

2.7. Interoperability 23

QA-I-01 — VNPay Gateway Integration 23

QA-I-02 — Push Notification Multi-Channel Dispatch 24

QA-I-03 — Image Upload via Cloudinary 24

2.8. Supportability 24

QA-SUP-01 — Audit Trail for Order Lifecycle 24

QA-SUP-02 — Structured Logging on Cross-BC Events 25

QA-SUP-03 — Stuck-Order Diagnostics 25

2.9. Maintainability 26

QA-MA-01 — Bounded-Context Boundary Enforcement 26

QA-MA-02 — Schema Evolution via Drizzle Migrations 26

2.10. Testability 27

QA-T-01 — Deterministic Order Placement Tests 27

2.11. Usability 27

QA-U-01 — Sub-2-Minute Registration Flow 27

QA-U-02 — Predictable Restaurant Discovery 28

2.12. Conceptual Integrity 28

QA-CI-01 — Single Order-Status Vocabulary 28

QA-CI-02 — Event Envelope Consistency 29

3. Architectural Representation 30

3.1. Logical View 31

3.2. Implementation View 34

3.3. Deployment View 35

3.4. Data View 38

# 1. Design Constraints

In this ADD, Design Constraints are expressed as quality drivers: the quality concerns that constrain the architecture and force specific design responsibilities. Technology choices are consequences of these drivers, not the primary constraints.

| **Quality Driver** | **Constraint** | **Architectural Implication** | **Affected Modules** | **Design Pressure** |
| --- | --- | --- | --- | --- |
| **Performance** | Search, checkout, cart mutation, order-status propagation, and notification delivery must stay responsive during normal mobile/web use. | Reads are paginated, cart and idempotency state are kept in Redis, checkout is concentrated in one command handler, and non-critical side effects are emitted after commit. | Restaurant Catalog, Ordering, Cart, Payment, Notification, Redis, PostgreSQL. | Avoid chatty cross-context calls, prevent N+1 list reads, keep event handlers lightweight, and preserve p95 targets for the user-visible purchase path. |
| **Availability** | Authentication, ordering, payment confirmation, and notification recovery must remain usable when optional providers or client connections degrade. | Core business state is persisted in PostgreSQL, sessions are database-backed, notification inbox rows are durable, and SMTP/FCM providers degrade through Noop/Stub adapters. | Auth, Ordering, Payment, Notification, Redis, PostgreSQL, external provider adapters. | Keep optional channels from blocking committed order/payment flows and define fallback reads for disconnected clients. |
| **Reliability** | The platform must prevent duplicate orders, invalid order transitions, duplicate payment updates, and inconsistent promotion/refund side effects. | Ordering uses Redis idempotency plus a database uniqueness backstop, lifecycle transitions use a closed state machine and optimistic locking, and payment IPN processing verifies terminal states before mutation. | Ordering, Order Lifecycle, Payment, Promotion, Notification, Redis, PostgreSQL. | Put invariants in backend handlers and database constraints, emit events only after commit, and make compensation handlers idempotent. |
| **Security** | Identity, role-based access, payment callback integrity, and public endpoint abuse protection must protect money, orders, and administrative actions. | Better Auth owns credential/session handling, route guards and service checks enforce roles and ownership, VNPay callbacks are HMAC verified before lookup/mutation, and rate limiting is reserved for the edge or Nest throttling layer. | Auth, Payment, Ordering, Restaurant Catalog, Promotion, Notification, Image, Admin/Governance surfaces. | Keep secrets in validated environment variables, avoid custom cryptography, deny unauthorized access before mutation, and avoid leaking order existence across ownership boundaries. |
| **Scalability** | Browse/search and cart/order traffic must grow beyond one process while respecting the modular-monolith boundary. | HTTP state is externalized to PostgreSQL and Redis; scaling means replicating the whole API instance, while WebSocket fan-out requires sticky sessions or a Socket.IO Redis adapter before multi-instance correctness. | API runtime, Restaurant Catalog, Cart, Ordering, Notification Gateway, Redis, PostgreSQL. | Keep modules stateless at the process level, isolate hot volatile keys in Redis, and document the EventBus limitation before extracting services. |
| **Flexibility** | Payment providers, notification providers, order statuses, and promotion rules must be changeable without rewriting checkout or lifecycle ownership. | Ordering depends on Payment and Promotion through ports, notification channels are provider interfaces, and lifecycle states are centralized in one enum/transition matrix. | Ordering, Payment, Promotion, Notification, shared ports, shared events. | Avoid concrete cross-BC imports, keep provider details behind adapters, and update contract tests whenever core vocabularies evolve. |
| **Maintainability** | The backend must remain understandable as bounded contexts with clear data ownership even while deployed as one application. | Source code is organized by BC modules under apps/api/src/module, shared contracts live under src/shared, infrastructure helpers stay under src/lib, and Drizzle schemas group tables by owner. | All backend BCs, shared events, shared ports, Drizzle schema/migrations. | Prevent boundary erosion, keep routine CRUD inside its owning module, and treat cross-context reads as snapshots or public contracts. |
| **Testability** | Critical order, payment, promotion, notification, and lifecycle rules must be verifiable deterministically in CI. | Command handlers and services use dependency injection, providers can be stubbed, and e2e tests run against controlled PostgreSQL/Redis services. | Ordering, Payment, Promotion, Notification, Cart, ACL projections, test setup. | Keep time, provider calls, and Redis/DB dependencies controllable; preserve small handler contracts that can be tested without full client stacks. |
| **Usability** | Users must receive predictable feedback for discovery, cart conflicts, payment failure, delivery updates, and notification recovery. | Backend responses include structured error reasons, cart mutations return the full cart payload, order/payment states are durable, and notification inbox reads support recovery after realtime disconnects. | Restaurant Catalog, Cart, Ordering, Payment, Notification, Auth. | Make backend state transitions and error codes explicit so web/mobile clients can present clear next actions without duplicating business logic. |
| **Interoperability** | The platform must integrate predictably with VNPay, Cloudinary, FCM, SMTP, Expo/mobile clients, and web clients. | External systems are wrapped by adapters, gateway payloads are canonicalized, image bytes remain in Cloudinary, and clients consume backend REST/WebSocket contracts rather than database structures. | Payment, Image, Notification, Auth, client API contracts. | Preserve provider-specific protocol rules at the adapter boundary and keep domain modules free of provider payload formats where possible. |
| **Supportability** | Operators and developers must be able to diagnose lifecycle decisions, payment outcomes, notification failures, and cross-context event issues. | Order transitions append audit rows, payment transactions preserve provider references/payload metadata, notification delivery attempts are logged, and NestJS loggers mark handler failures. | Ordering, Payment, Notification, Promotion, Admin/Governance surfaces. | Store enough actor/target/outcome context for investigation, and add correlation/central logging before relying on production SLO claims. |
| **Conceptual Integrity** | The architecture must use one shared language for roles, order states, events, data ownership, and BC responsibilities. | Role vocabulary is centralized in Auth, order lifecycle vocabulary is centralized in Ordering, events are explicit POJOs, and each table group has a single owning BC. | Auth, Ordering, shared events, Restaurant Catalog, Payment, Promotion, Notification, Image, Review & Rating target model. | Avoid parallel meanings for the same business concept, keep Image and Notification independent from Catalog/Review, and treat Review & Rating as its own BC rather than a UI add-on. |

# 2. Quality Attribute Requirements

The following quality attribute scenarios define the measurable requirements that drive architectural decisions in the SoLi Food Delivery Platform. Each scenario is presented with its unique identifier, implementation status, and supporting architectural tactics.

## 2.1. Performance

### QA-P-01 — Restaurant Search Response Time

|  |  |
| --- | --- |
| **Stimulus** | Customer submits a restaurant / item search query |
| **Stimulus Source** | Customer client |
| **Environment** | Normal operational load (≤ 1× projected peak) |
| **Artifact** | restaurant-catalog/search controller + repository (search.repository.ts); PostgreSQL |
| **Response** | First page of results returned with pagination metadata |
| **Response Measure** | p95 ≤ 2 s; page size ≤ 20; results ordered deterministically |
| **Architectural Tactics** | Paginated queries (offset/limit); approved/open composite index on restaurants; planned Redis read-through caching for hot queries (Cache-Aside) |

### QA-P-02 — Order Status Propagation to Customer

|  |  |
| --- | --- |
| **Stimulus** | Order status transitions (e.g., confirmed → preparing) |
| **Stimulus Source** | Restaurant / shipper / admin HTTP client, or system task |
| **Environment** | Normal load; customer device online; WebSocket session active |
| **Artifact** | NotificationGateway → room:user:{userId}; Socket.IO /notifications namespace |
| **Response** | Connected notification clients receive WS\_NOTIFICATION\_CREATED; persisted notification rows remain available for REST inbox reloads |
| **Response Measure** | Backend event-to-WebSocket emit latency target ≤ 5 s p95 |
| **Architectural Tactics** | In-process EventBus → event handler → WebSocket emit; Redis-tracked presence enables fan-out only to active sessions |

### QA-P-03 — Checkout End-to-End Latency

|  |  |
| --- | --- |
| **Stimulus** | Customer submits Place-Order request |
| **Stimulus Source** | Customer client |
| **Environment** | Normal load; payment method = COD |
| **Artifact** | PlaceOrderHandler; Drizzle transaction over orders, order\_items, order\_status\_logs |
| **Response** | Order persisted; OrderPlacedEvent dispatched; response returned |
| **Response Measure** | p95 ≤ 3 s including ACL snapshot reads, promotion reservation, haversine validation, and DB commit |
| **Architectural Tactics** | Single ACID transaction; idempotency short-circuit on Redis hit; haversine in-memory; ACL reads from local snapshot tables (no cross-BC RPC) |

### QA-P-04 — Menu / Availability Update Propagation

|  |  |
| --- | --- |
| **Stimulus** | Restaurant edits menu item price / availability |
| **Stimulus Source** | Restaurant management client |
| **Environment** | Normal load |
| **Artifact** | Restaurant-catalog publishes MenuItemUpdatedEvent; Ordering ACL projector |
| **Response** | ordering\_menu\_item\_snapshots updated; subsequent place-order uses fresh data |
| **Response Measure** | Propagation target ≤ 10 s; current same-process event dispatch is expected to complete substantially faster, but formal latency measurement is still pending |

## 2.2. Availability

### QA-A-01 — Authentication Endpoint Availability

|  |  |
| --- | --- |
| **Stimulus** | Customer / partner submits sign-in or session validation |
| **Stimulus Source** | Any client |
| **Environment** | Calendar month, normal + occasional partial outage |
| **Artifact** | Better Auth integration (lib/auth.ts); PostgreSQL session store |
| **Response** | Successful authentication when PostgreSQL/auth dependencies are available; failures surface as standard HTTP errors |
| **Response Measure** | Availability target: 99.5 percent deployment objective for the authentication path |
| **Architectural Tactics** | Stateless app instances (planned horizontal scale); fail-fast at startup on config errors |

### QA-A-02 — Real-Time Channel Graceful Degradation

|  |  |
| --- | --- |
| **Stimulus** | WebSocket connection lost (network, server restart) |
| **Stimulus Source** | Customer / shipper / restaurant client |
| **Environment** | Mobile network handover, degraded connectivity |
| **Artifact** | NotificationGateway plus REST NotificationController |
| **Response** | Backend supports recovery through the REST inbox at /api/notifications/my; mobile implements a notification socket and on-demand inbox fetch, while the defined unread-count polling hook is not wired and automatic order-detail polling is not implemented across clients |
| **Response Measure** | In-app notifications are persisted with a 90-day expiresAt; reconnect re-joins the per-user room and new deliveries remain idempotent by notification key |
| **Architectural Tactics** | Durable notification store; idempotent notification.id; per-user room rejoin on reconnect |

### QA-A-03 — Optional Notification-Channel Degradation

|  |  |
| --- | --- |
| **Stimulus** | SMTP or FCM unreachable / credentials absent |
| **Stimulus Source** | External provider outage |
| **Environment** | Provider degraded |
| **Artifact** | EmailChannel, PushChannel providers |
| **Response** | Core flows (order placement, payment) continue; the affected notification channel logs failure to notification\_delivery\_logs |
| **Response Measure** | Zero impact on order-state correctness; failed dispatches retried by future iteration (currently logged, not auto-retried) |

## 2.3. Reliability

### QA-R-01 — Order Placement Idempotency

|  |  |
| --- | --- |
| **Stimulus** | Client retries Place-Order request after timeout or unknown response |
| **Stimulus Source** | Customer client |
| **Environment** | Network instability |
| **Artifact** | PlaceOrderHandler; Redis idempotency:order:{key}; orders.cart\_id UNIQUE constraint |
| **Response** | Identical orderId returned; no duplicate orders row; no double-charge |
| **Response Measure** | Zero duplicate orders across retries with identical X-Idempotency-Key within ORDER\_IDEMPOTENCY\_TTL\_SECONDS (fallback 300 s) |
| **Architectural Tactics** | Redis idempotency key (fast path); DB UNIQUE(cart\_id) (backstop); transactional commit before publishing OrderPlacedEvent |

Cập nhật Solution

### QA-R-02 — Payment IPN Webhook Idempotency

|  |  |
| --- | --- |
| **Stimulus** | VNPay retries the IPN callback |
| **Stimulus Source** | VNPay gateway |
| **Environment** | VNPay retry policy (until RspCode=00) |
| **Artifact** | ProcessIpnHandler; payment\_transactions.version |
| **Response** | First call updates state and publishes PaymentConfirmedEvent / PaymentFailedEvent; subsequent calls return success without re-emit |
| **Response Measure** | Zero duplicate state transitions; zero duplicate downstream events under arbitrary retry counts |
| **Architectural Tactics** | Signature verification first; lookup by vnp\_TxnRef; terminal-state short-circuit; optimistic-lock version increment |

### QA-R-03 — Order State-Machine Integrity

|  |  |
| --- | --- |
| **Stimulus** | Any actor (customer, restaurant, shipper, admin, scheduled task) requests an order status transition |
| **Stimulus Source** | Any of the above |
| **Environment** | Normal + concurrent operation |
| **Artifact** | TRANSITIONS map (closed transition matrix); TransitionOrderHandler (enforcement + optimistic lock); OrderLifecycleService (ownership checks); orders.version; order\_status\_logs |
| **Response** | Disallowed transitions rejected with a typed error; allowed transitions commit atomically and append an audit log |
| **Response Measure** | 100% of disallowed transitions rejected; 100% committed transitions logged; concurrent transition attempts fail-safe via optimistic-lock retry / rejection |
| **Architectural Tactics** | Hand-crafted TRANSITIONS map in constants/transitions.ts; TransitionOrderHandler enforces via @CommandHandler; optimistic locking on version; transactional INSERT into order\_status\_logs |

### QA-R-04 — Single-Restaurant Cart Invariant

|  |  |
| --- | --- |
| **Stimulus** | Customer adds an item from Restaurant B to a cart already containing items from Restaurant A |
| **Stimulus Source** | Customer client |
| **Environment** | Normal |
| **Artifact** | CartService |
| **Response** | Request rejected with a structured error (CART\_RESTAURANT\_CONFLICT); existing cart left unchanged |
| **Response Measure** | 100% rejection in unit / e2e tests; cart store remains consistent |
| **Architectural Tactics** | BR-2 enforcement in service before Redis write |

### QA-R-05 — Atomic Shipper Assignment

|  |  |
| --- | --- |
| **Stimulus** | Two shippers concurrently accept the same dispatch |
| **Stimulus Source** | Shipper mobile clients |
| **Environment** | Concurrent acceptance |
| **Artifact** | T-09 (ready\_for\_pickup → picked\_up) in TransitionOrderHandler; orders.version; orders.shipperId |
| **Response** | At most one shipper bound to the order; loser receives a typed conflict response |
| **Response Measure** | Logical guarantee: at most one shipper assignment per successful optimistic-lock commit on the same order row; concurrent validation remains operational work |
| **Architectural Tactics** | Shipper self-assignment occurs inside the same optimistic-lock status update that advances T-09; losing concurrent requests receive ConflictException |

### QA-R-06 — Payment Timeout Recovery

|  |  |
| --- | --- |
| **Stimulus** | A payment transaction remains in pending or awaiting\_ipn state beyond the configured expiresAt deadline |
| **Stimulus Source** | Customer inactivity, gateway delay, or payment abandonment |
| **Environment** | Normal scheduled execution (@Cron(EVERY\_MINUTE)) |
| **Artifact** | PaymentTimeoutTask; payment\_transactions.expiresAt; PaymentFailedEvent |
| **Response** | Expired transaction transitioned to failed via optimistic lock; PaymentFailedEvent published; Ordering BC handler auto-cancels the order through the CQRS path |
| **Response Measure** | Expired transactions are selected by the every-minute sweeper; optimistic locking prevents duplicate state changes |
| **Architectural Tactics** | Scheduled sweeper with optimistic-lock concurrency guard; event-driven cancellation cascade; terminal-state protection prevents double-processing |

### QA-R-07 — Restaurant Acceptance Timeout

|  |  |
| --- | --- |
| **Stimulus** | A restaurant does not accept or reject an order within the configured acceptance window |
| **Stimulus Source** | Restaurant operator inaction |
| **Environment** | Normal scheduled execution (@Cron(EVERY\_MINUTE)) |
| **Artifact** | OrderTimeoutTask; RESTAURANT\_ACCEPT\_TIMEOUT\_SECONDS (from app\_settings); TransitionOrderCommand |
| **Response** | Order auto-cancelled via the same CQRS TransitionOrderCommand path used by all actors; T-05 fires for paid orders triggering the refund event automatically |
| **Response Measure** | Eligible expired orders are scanned every minute and routed through TransitionOrderCommand; stuck-order diagnostics / alerting remain planned |
| **Architectural Tactics** | Scheduler scan; reuse of existing CQRS command path (no bespoke cancellation logic); acceptance window configurable at runtime via app\_settings without redeployment |

### QA-R-08 — Refund and Promotion Compensation Reliability

|  |  |
| --- | --- |
| **Stimulus** | A VNPay-paid order is cancelled through a refund-triggering transition, or an order with a reserved promotion fails / is cancelled |
| **Stimulus Source** | Ordering BC emits OrderCancelledAfterPaymentEvent or OrderStatusChangedEvent(cancelled/refunded) |
| **Environment** | Normal; VNPay Refund API stubbed in current implementation (production retry TBD) |
| **Artifact** | OrderCancelledAfterPaymentHandler; PromotionRollbackOnCancellationHandler; PromotionService |
| **Response** | Payment refund state is advanced in Payment BC; promotion reservations/usages are rolled back through the promotion port; failures are logged and do not roll back the already committed order state |
| **Response Measure** | Order cancellation / failed checkout correctness is independent of refund or promotion-rollback outcome; real refund retry automation remains planned, while promotion counter rollback is implemented idempotently |
| **Architectural Tactics** | Event-driven async compensation; failure containment in payment/refund handlers; promotion rollback through PROMOTION\_APPLICATION\_PORT with idempotent counter decrements and usage status updates |

## 2.4. Security

### QA-S-01 — VNPay Callback Integrity

|  |  |
| --- | --- |
| **Stimulus** | Forged or tampered VNPay IPN payload |
| **Stimulus Source** | Attacker / Internet |
| **Environment** | Public IPN endpoint |
| **Artifact** | VNPayService.verifyReturnUrl / verifyIpn; crypto.timingSafeEqual |
| **Response** | Request rejected; no state mutation; no events emitted |
| **Response Measure** | Invalid HMAC-SHA512 payloads are rejected before state mutation; penetration / negative security tests are recommended validation |
| **Architectural Tactics** | Signature verification before any DB lookup; constant-time comparison; ordered URL-encoded canonicalization per VNPay spec |

### QA-S-02 — Authentication & Session Management

|  |  |
| --- | --- |
| **Stimulus** | User sign-in / session validation |
| **Stimulus Source** | Customer, restaurant, shipper, admin |
| **Environment** | Public endpoints |
| **Artifact** | Better Auth + Drizzle adapter (lib/auth.ts); session, account, verification tables |
| **Response** | Strong session token issued; bearer token validated server-side on each request |
| **Response Measure** | Industry-standard password hashing (Better Auth default — scrypt); session secret ≥ 32 chars enforced at startup via Zod |
| **Architectural Tactics** | Library-managed credential handling; HTTPS-only deployment (deployment constraint); no custom rolling of crypto |

### QA-S-03 — Role-Based Authorization

|  |  |
| --- | --- |
| **Stimulus** | Unauthorized actor accesses an admin / restaurant / shipper endpoint |
| **Stimulus Source** | Any client |
| **Environment** | Any |
| **Artifact** | user.role (multi-role CSV); hasRole() utility; route guards |
| **Response** | 401 (no session) / 403 (insufficient role); unauthorized attempts observable through server/access logs; order lifecycle mutations write persistent audit rows |
| **Response Measure** | Protected endpoints deny missing or mismatched roles before service-layer mutation |
| **Architectural Tactics** | Multi-role bitmap-equivalent (CSV) checked via OR-logic helper; Better Auth admin() plugin for admin scoping |

### QA-S-05 — Input Validation & Injection Resistance

|  |  |
| --- | --- |
| **Stimulus** | Client submits malformed DTO fields or HTML / JS payloads in catalog, cart, order, promotion, or notification requests |
| **Stimulus Source** | Authenticated or public client |
| **Environment** | Any |
| **Artifact** | Global ValidationPipe({ transform: true }) in main.ts; class-validator DTOs |
| **Response** | DTO validation rejects malformed payloads; Drizzle parameterization protects database access |
| **Response Measure** | Invalid DTO payloads rejected before service-layer mutation; SQL injection prevented by Drizzle parameterized queries |

### QA-S-06 — Rate Limiting on Public Endpoints

|  |  |
| --- | --- |
| **Stimulus** | Burst of unauthenticated requests on login, register, or search endpoints |
| **Stimulus Source** | Attacker / abusive client |
| **Environment** | Production |
| **Artifact** | Reverse proxy (planned) or @nestjs/throttler (not yet integrated) |
| **Response** | Excess requests throttled with 429 |
| **Response Measure** | ≤ 100 req/min/IP for login; ≤ 300 req/min/IP for catalog |
| **Architectural Tactics** | Edge-layer throttling (nginx / cloud LB) OR module-level throttler |

## 2.5. Scalability

### QA-SC-01 — Horizontal Scaling of API Instances

|  |  |
| --- | --- |
| **Stimulus** | Browse / search traffic and active notification sessions grow beyond the single-instance baseline during peak hour |
| **Stimulus Source** | Aggregate customer traffic and active WebSocket sessions |
| **Environment** | Peak hour |
| **Artifact** | Stateless NestJS API instances behind a load balancer; PostgreSQL primary |
| **Response** | Additional API instances can absorb stateless HTTP traffic; WebSocket fan-out requires sticky sessions or a Socket.IO Redis adapter before true multi-instance delivery correctness |
| **Response Measure** | Architecture target: p95 search response ≤ 2 s for stateless HTTP traffic; formal load testing and per-instance CPU thresholds remain pending validation |
| **Architectural Tactics** | Stateless HTTP design (no in-memory session); Redis-shared cart, idempotency, and presence; database connection pooling; |

### QA-SC-02 — Cart and Idempotency Storage Scaling

|  |  |
| --- | --- |
| **Stimulus** | High concurrent cart mutation / order submission |
| **Stimulus Source** | Customer fleet |
| **Environment** | Peak |
| **Artifact** | Redis service/instance accessed through an ioredis client with capped backoff retry |
| **Response** | Cart writes complete in O(1) per key; idempotency lookup is O(1) |
| **Response Measure** | Target p95 cart operation ≤ 50 ms; benchmark validation remains operational work |
| **Architectural Tactics** | Per-customer cart key; per-idempotency-key set with TTL; lazy-connect + capped exponential backoff retry |

## 2.6. Flexibility

### QA-FL-01 — Generalizing Payment Provider Integration

|  |  |
| --- | --- |
| **Stimulus** | Add a non-VNPay payment provider (e.g., MoMo, ZaloPay) |
| **Stimulus Source** | Product roadmap |
| **Environment** | Development |
| **Artifact** | IPaymentInitiationPort (payment-initiation.port.ts); Payment module |
| **Response** | Ordering is decoupled from the concrete Payment service |
| **Response Measure** | Current state: zero concrete Payment imports in module/ordering; target state: provider-neutral initiation contract and provider-selection tests |
| **Architectural Tactics** | Ports & Adapters boundary exists |

### QA-FL-02 — Adding a New Order Status

|  |  |
| --- | --- |
| **Stimulus** | Add a new lifecycle status (e.g., awaiting\_courier) |
| **Stimulus Source** | Operations roadmap |
| **Environment** | Development |
| **Artifact** | order.schema.ts enum; TRANSITIONS map; notification handlers |
| **Response** | New status added to enum, transition matrix, and audit log writer |
| **Response Measure** | Required changes are concentrated in the order enum, transition map, and notification mapping |

### QA-FL-03 — Replacing a Notification Channel Provider

|  |  |
| --- | --- |
| **Stimulus** | Replace FCM with another push provider |
| **Stimulus Source** | Operations / cost decision |
| **Environment** | Development |
| **Artifact** | PushProvider interface (push-provider.interface.ts) |
| **Response** | New adapter added; module factory rebinds the token |
| **Response Measure** | Zero changes in event handlers or domain code |

## 2.7. Interoperability

### QA-I-01 — VNPay Gateway Integration

|  |  |
| --- | --- |
| **Stimulus** | Customer pays online |
| **Stimulus Source** | Customer / VNPay return + IPN callbacks |
| **Environment** | Public Internet |
| **Artifact** | VNPayService; vnp\_\* parameters; crypto HMAC-SHA512 |
| **Response** | Payment URL generated; return + IPN parsed; signed correctly; result persisted |
| **Response Measure** | Conformance to VNPay spec is verifiable through sandbox/manual tests for signature, ordering, and encoding |

### QA-I-02 — Push Notification Multi-Channel Dispatch

|  |  |
| --- | --- |
| **Stimulus** | NotificationService persists a notification row from a domain-event handler |
| **Stimulus Source** | Cross-BC event handlers |
| **Environment** | Customer in foreground / background / offline |
| **Artifact** | ChannelDispatcherService; InAppChannelService, EmailChannelService, PushChannelService |
| **Response** | Channels chosen by user preferences and presence; each channel delivers independently |
| **Response Measure** | Delivery attempts are recorded in notification\_delivery\_logs; provider success-rate targets require operational monitoring |

### QA-I-03 — Image Upload via Cloudinary

|  |  |
| --- | --- |
| **Stimulus** | Restaurant uploads a menu-item image |
| **Stimulus Source** | Restaurant management client |
| **Environment** | Normal |
| **Artifact** | Cloudinary provider (cloudinary.provider.ts); signed upload |
| **Response** | Image uploaded to Cloudinary; URL persisted in images table |
| **Response Measure** | Target upload latency p95 ≤ 5 s for images ≤ 2 MB; actual latency depends on Cloudinary/network conditions |

## 2.8. Supportability

### QA-SUP-01 — Audit Trail for Order Lifecycle

|  |  |
| --- | --- |
| **Stimulus** | Any order status transition |
| **Stimulus Source** | Any actor |
| **Environment** | Any |
| **Artifact** | order\_status\_logs table |
| **Response** | One row per transition: {orderId, fromStatus, toStatus, triggeredBy (UUID|null), triggeredByRole, note, createdAt}; fromStatus is nullable for the initial creation entry |
| **Response Measure** | 100% of committed transitions audited; queryable by orderId, actor, or time range |

### QA-SUP-02 — Structured Logging on Cross-BC Events

|  |  |
| --- | --- |
| **Stimulus** | An event handler fails (e.g., ACL projection error, channel dispatch error) |
| **Stimulus Source** | Internal |
| **Environment** | Production |
| **Artifact** | NestJS Logger; handler-specific failure policies in @EventsHandler classes |
| **Response** | Error logged at ERROR level with context (eventType, aggregateId); notification and refund handlers absorb failures, while ACL projectors currently log and rethrow after failed snapshot writes |
| **Response Measure** | Handler failures are logged with contextual IDs; ≤ 5 minute detection requires active log monitoring until APM is integrated |

### QA-SUP-03 — Stuck-Order Diagnostics

|  |  |
| --- | --- |
| **Stimulus** | An order remains in a non-terminal status beyond a configured threshold |
| **Stimulus Source** | Scheduler |
| **Environment** | Production |
| **Artifact** | Future diagnostic task and admin monitoring surface; current OrderTimeoutTask only auto-cancels expired pending / paid orders |
| **Response** | Order flagged with a reason code and surfaced on the admin monitoring view |
| **Response Measure** | Detection latency ≤ 1 minute past threshold |

## 2.9. Maintainability

### QA-MA-01 — Bounded-Context Boundary Enforcement

|  |  |
| --- | --- |
| **Stimulus** | A developer attempts to import a Payment / Promotion concrete class into Ordering |
| **Stimulus Source** | Pull request |
| **Environment** | Development |
| **Artifact** | Ports (PAYMENT\_INITIATION\_PORT, PROMOTION\_APPLICATION\_PORT); ACL snapshot tables |
| **Response** | The compiler permits it, but architectural reviews / planned ESLint boundary rules forbid it; only the port symbol is imported |
| **Response Measure** | Zero cross-BC concrete imports in module/ordering (verified by grep / planned ESLint rule) |

### QA-MA-02 — Schema Evolution via Drizzle Migrations

|  |  |
| --- | --- |
| **Stimulus** | New table / column added |
| **Stimulus Source** | Developer |
| **Environment** | Development → staging → production |
| **Artifact** | Drizzle Kit migrations; drizzle.config.ts |
| **Response** | Generated migration file applied; existing data preserved |
| **Response Measure** | Migrations are forward-compatible (no destructive rewrites without a coordinated release) |

## 2.10. Testability

### QA-T-01 — Deterministic Order Placement Tests

|  |  |
| --- | --- |
| **Stimulus** | A new lifecycle / pricing rule is added |
| **Stimulus Source** | Developer |
| **Environment** | CI |
| **Artifact** | Jest unit + e2e tests; payment e2e (test/payment.e2e-spec.ts) |
| **Response** | Tests pass deterministically against ephemeral DB + Redis + stub providers |
| **Response Measure** | Existing e2e/spec coverage exercises payment, order, cart, ACL, promotion, and notification paths; coverage thresholds are not formalized |
| **Architectural Tactics** | Provider abstractions allow NoopEmailProvider / StubPushProvider in tests |

## 2.11. Usability

### QA-U-01 — Sub-2-Minute Registration Flow

|  |  |
| --- | --- |
| **Stimulus** | New customer signs up |
| **Stimulus Source** | Customer (mobile / web) |
| **Environment** | Normal mobile network |
| **Artifact** | Better Auth emailAndPassword flow; client UX |
| **Response** | Account created, session issued, first screen rendered |
| **Response Measure** | ≥ 90% of first-time users complete in ≤ 2 minutes; SUS ≥ 80 in usability tests |
| **Backend Constraint** | Account-creation API response p95 ≤ 2 s |

### QA-U-02 — Predictable Restaurant Discovery

|  |  |
| --- | --- |
| **Stimulus** | Customer browses restaurants from the home screen |
| **Stimulus Source** | Customer |
| **Environment** | Normal |
| **Artifact** | Restaurant-catalog public endpoints |
| **Response** | Stable pagination cursors; consistent ordering across requests |
| **Response Measure** | Deterministic backend ordering is implemented; user task-completion metrics remain a client usability-test target |

## 2.12. Conceptual Integrity

### QA-CI-01 — Single Order-Status Vocabulary

|  |  |
| --- | --- |
| **Stimulus** | Any module reads or writes order status |
| **Stimulus Source** | Internal modules |
| **Environment** | Any |
| **Artifact** | orderStatusEnum in order.schema.ts |
| **Response** | All modules consume the same enum; cross-BC consumers receive status as a string literal type matching the enum |
| **Response Measure** | Zero parallel status vocabularies across implemented modules; contract tests for the allowed set are recommended validation |

### QA-CI-02 — Event Envelope Consistency

|  |  |
| --- | --- |
| **Stimulus** | A new domain event is introduced |
| **Stimulus Source** | Developer |
| **Environment** | Development |
| **Artifact** | shared/events — all events are immutable POJOs with explicit constructors |
| **Response** | New event follows the same shape and is exported through the barrel index.ts |
| **Response Measure** | Code review currently enforces event-shape consistency |

# 3. Architectural Representation

To describe the architecture of the SoLi Food Delivery Platform, the following views are presented, each targeting a different architectural concern: Logical, Implementation, Deployment, and Data. PlantUML source diagrams for all views are provided in the Appendix for reference and toolchain rendering.

## ![](data:image/png;base64...)3.1. Logical View

*Figure 3.1 — SoLi Logical View (Bounded Contexts, Ports, Domain Events)*

* This view is intentionally domain-only. It shows bounded contexts, business capabilities, ports, and domain-event dependencies without implementation, persistence, or external integration detail.
* Auth BC owns identity, sessions, role-based access control, and user profile data. All actors authenticate through Auth BC; role vocabulary is shared with Governance.
* Restaurant Catalog BC owns restaurant identity, menu catalog, public search, delivery-zone definitions, and item availability. Search remains inside Catalog rather than becoming a separate subsystem.
* Image BC is independent from Restaurant Catalog. Catalog references image metadata, while Image BC owns image-related business responsibilities; storage and CDN integration detail appears only in later views.
* Ordering BC owns cart, checkout, order lifecycle, delivery progress, order history, and ACL snapshots for catalog and delivery-zone data. It defines PAYMENT\_INITIATION\_PORT and PROMOTION\_APPLICATION\_PORT as outbound ports; Payment BC and Promotion BC implement these ports respectively, keeping Ordering decoupled from payment and promotion specifics.
* Payment BC owns payment processing, payment lifecycle, and refund handling. It implements the PAYMENT\_INITIATION\_PORT port defined by Ordering. Provider and gateway integration detail appears only in the Implementation View.
* Promotion BC owns promotion rules, coupons, and a reservation lifecycle for atomic coupon application. It implements the PROMOTION\_APPLICATION\_PORT port defined by Ordering.
* Notification BC owns notifications, inbox behavior, user preferences, and ACL snapshots for restaurant data. It is not merged with Ordering or Review because user communication is its own business concern.
* Review & Rating BC owns eligibility (verified via an Order Lifecycle contract rather than direct history access, avoiding UI-level coupling), reviews, ratings, and aggregation that feeds rating summaries back to Restaurant Catalog.
* Admin/Governance BC owns partner approval, platform oversight of orders and promotions, role governance, and audit. Catalog reads approval status from Governance through a partner approval contract. Role vocabulary is sourced from Auth.
* The Domain Events Hub shown here represents business event flow between bounded contexts. Catalog, Ordering, Payment, and Promotion publish domain events to the Hub; Notification, Review, and Governance subscribe. Transport and subscription routing detail appears in the Implementation View.

## 3.2. Implementation View

![](data:image/png;base64...)

*Figure 3.2 — SoLi Implementation View (Module-Level Architecture)*

This view shows the implementation architecture at module level inside the modular monolith. Each bounded context is reduced to the implementation chain that is actually important at this level: controller, service, repository, schema, shared-kernel contracts, and explicit integration adapters.

**Architecture Summary**

* Each implemented bounded context keeps an explicit controller → service → repository → schema path, and each schema connects directly to PostgreSQL so persistence ownership is visible at the correct abstraction level.
* Cross-context dependencies now originate from internal components only: CatalogService depends on ImageService, while OrderingService reaches Payment and Promotion through shared ports rather than through BC-to-BC links.
* The Shared Kernel is no longer decorative: the Domain Events Hub receives publications from Catalog, Ordering, Payment, and Promotion services, and dispatches them to Notification plus Ordering ACL consumers; validators are also used directly by selected controllers.
* PostgreSQL backs every bounded-context schema; Redis is used only by OrderingService and NotificationService.
* External integrations terminate at explicit adapters and service-level callers only: Image uses CloudinaryAdapter, Payment uses VNPayAdapter, and Notification uses FCMAdapter plus SMTPAdapter.

## 3.3. Deployment View

![](data:image/png;base64...)

*Figure 3.3 — SoLi Deployment View (Production Topology)*

This view describes the deployment target required by QA-SC-01 and the availability scenarios, while preserving the current modular-monolith constraint. Scaling means adding complete API instances that each load every bounded context; it does not split the system into microservices.

**Environment**

* Current repository delivery is image-based: GitHub Actions validates the monorepo, builds API/web Docker images with Docker Buildx, publishes them to GHCR, and packages the mobile APK through EAS. Render deploy hooks are documented in CD\_GUIDE.md as the next deployment automation step and are marked as a target because they are not present in ci.yml yet.
* The production topology introduces a managed HTTPS edge and load balancer in front of the web service and an API autoscaling group. The minimum HA target is API Instance 1 and API Instance 2; API Instance N represents autoscaling under load.
* Each API instance runs the same NestJS Docker image and contains Auth, Restaurant Catalog, Image, Ordering, Payment, Promotion, Notification, and all implemented modules. Cross-BC events remain in-process inside each instance.
* PostgreSQL remains the durable source of truth. The target deployment adds managed backups and a read-replica option for reporting/monitoring load, but write ownership remains a single-primary model.
* Redis/Valkey is shared across API instances for cart keys, checkout locks, idempotency keys, WebSocket presence, and future rate-limit buckets.
* WebSocket scale requires one of two explicit strategies before multi-instance correctness can be claimed: load-balancer sticky sessions so a user's socket stays on the instance that owns its room, or a Socket.IO Redis adapter so room membership and emits are coordinated across instances.
* Rate limiting is a planned deployment control, implemented either at the edge/reverse proxy or inside NestJS with Redis-backed counters so quotas are consistent across all API instances.
* Monitoring is layered: Render logs and health/startup diagnostics exist as the current baseline, while Prometheus/Grafana/APM, alerting, and stuck-order dashboards are planned production additions needed for supportability claims.

**Deployment Example**

* Developer pushes to master.
* ci.yml runs validate.yml, then publish-docker.yml and publish-mobile.yml after validation succeeds.
* publish-docker.yml builds the API image from apps/api/Dockerfile and the web image from apps/web/Dockerfile, tags them with branch and short-SHA metadata, and pushes them to GHCR.
* Render deploy hooks pull the immutable GHCR image tags and roll the web service plus every API instance in the autoscaling group.
* The load balancer routes REST traffic across full API instances; realtime traffic uses sticky sessions or the Socket.IO Redis adapter target.
* All API instances share PostgreSQL and Redis/Valkey and call VNPay, Cloudinary, FCM, and SMTP only through implementation-layer provider adapters.

## 3.4. Data View

![](data:image/png;base64...)

*Figure 3.4 — SoLi Data View (Bounded-Context Data Ownership)*

This view focuses on data ownership and storage strategy. It defines how PostgreSQL table groups map to bounded-context ownership and documents the distinction between real foreign keys (within a BC) and logical UUID references (across BCs).

| **Bounded Context** | **Owned Entities / Tables** | **Storage Strategy** | **Notes** |
| --- | --- | --- | --- |
| **Auth** | identity, sessions, RBAC, user profile | PostgreSQL — real FKs within Auth boundary | Better Auth session/account/verification tables |
| **Restaurant Catalog** | restaurants, delivery zones, menu categories, menu items, modifier groups, modifier options | PostgreSQL — real internal FKs within aggregate family | Image URLs referenced by Catalog; image bytes owned by Cloudinary |
| **Image** | image metadata, provider identifiers | PostgreSQL (images table) + Cloudinary (image bytes) | Catalog references image URLs but does not own image storage |
| **Ordering** | orders, order\_items, order\_status\_logs, app\_settings, ACL snapshot tables | PostgreSQL — real FKs to orders; logical UUID refs for customerId, restaurantId, shipperId | Delivery fulfillment represented by order status + shipperId |
| **Payment** | payment\_transactions | PostgreSQL — orderId and customerId are logical refs (no FK constraint) | Enables future independent extraction of Payment BC |
| **Promotion** | promotions, coupon\_codes, promotion\_usages | PostgreSQL — logical UUID refs to Ordering and Auth | Checkout compensation independent of Ordering table structure |
| **Notification** | notifications, preferences, device\_tokens, delivery\_logs, notification restaurant snapshots | PostgreSQL — logical refs; Redis for presence | Durable inbox for realtime degradation recovery |
| **Review & Rating** | reviews, ratings (target schema) | PostgreSQL — target; not yet implemented in Drizzle | Planned BC for SRS UC-22; separate from Ordering and Catalog |
| **Admin/Governance** | partner\_applications, governance\_audit\_logs (target schema) | PostgreSQL — target; not yet implemented | Current admin behavior via existing BCs; dedicated tables planned |
| **Redis/Valkey** | cart state, checkout locks, idempotency caches, WS presence, rate-limit buckets | Redis — volatile, high-churn state | External to PostgreSQL; not BC ownership boundary |
| **External Providers** | VNPay (gateway), Cloudinary (image bytes), FCM (push), SMTP (email) | External systems — data authority outside platform | Platform stores references/metadata only |

**Primary Storage Architecture**

* PostgreSQL remains one physical database for the modular monolith, but tables are grouped by bounded-context ownership and avoid unnecessary cross-BC foreign keys to preserve future database-per-service extractability.
* Cross-context identifiers are stored as UUID references to preserve bounded-context autonomy and database-per-service readiness.
* Redis/Valkey stores volatile and high-churn state: carts, checkout locks, idempotency response caches, WebSocket presence reference counts, and planned rate-limit counters.

**Security Measures**

* All client-server and provider traffic is encrypted using HTTPS/TLS in production.
* Secrets are injected through environment variables validated at startup by Zod.
* Passwords and sessions are handled by Better Auth; the backend does not implement custom credential cryptography.
* RBAC uses admin, restaurant, shipper, and user roles with route guards plus service/handler ownership checks.
* Cross-context identifiers are stored as UUID references to preserve bounded-context autonomy and database-per-service readiness.
* Drizzle parameterized queries protect database access from SQL injection.
* Notification rooms are server-assigned from validated sessions and scoped as room:user:{userId}; Redis presence keys do not authorize access by themselves.
* Payment signatures are verified before database lookup or mutation.
* Order, payment, promotion, and notification state changes are audited through append-only or lifecycle tables.