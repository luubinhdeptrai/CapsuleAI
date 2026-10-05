Software Requirements Specification

for

<UITfood>

**Version 1.0 approved**

**Prepared by <Development Team>**

**<University of Information Technology>**

**<09/03/2026>**

**Table of Contents**

**Table of Contents ii**

**Revision History ii**

**1.** **Introduction 2**

1.1 Document Purpose 2

1.2 Document Conventions 2

1.3 Project Scope 2

1.4 References 2

**2.** **Overall Description 2**

2.1 Product Perspective 2

2.2 User Classes and Characteristics 2

2.3 Operating Environment 2

2.4 Design and Implementation Constraints 2

2.5 Assumptions and Dependencies 2

**3.** **System Features 2**

3.1 System Feature 1 2

3.2 System Feature 2 (and so on) 2

**4.** **Data Requirements 2**

4.1 Logical Data Model 2

4.2 Data Dictionary 2

4.3 Reports 2

4.4 Data Acquisition, Integrity, Retention, and Disposal 2

**5.** **External Interface Requirements 2**

5.1 User Interfaces 2

5.2 Software Interfaces 2

5.3 Hardware Interfaces 2

5.4 Communications Interfaces 2

**6.** **Quality Attributes 2**

6.1 Usability 2

6.2 Performance 2

6.3 Security 2

6.4 Safety 2

6.5 [Others as relevant] 2

**7.** **Internationalization and Localization Requirements 2**

**8.** **Other Requirements 2**

**9. Glossary 2**

**10. Analysis Models 2**

**Revision History**

| **Name** | **Date** | **Reason For Changes** | **Version** |
| --- | --- | --- | --- |
|  |  |  |  |
|  |  |  |  |

# Introduction

## Document Purpose

This Software Requirements Specification (SRS) details the requirements for Release 1 (MVP) of the Food Delivery Platform. It is intended to guide the development team, which includes business analysts, frontend developers, backend developers, and DevOps roles.

## Document Conventions

This document follows standard typographical and formatting conventions to ensure clarity and consistency. The following rules apply throughout the document:

**Bold Text**: Used to emphasize important terms, specific UI elements, or distinct roles (e.g., Customer, Restaurant Partner, Shipper).

*Italics*: Used for referencing external documents, specific technologies (e.g., NestJS, Socket.io), or highlighting definitions.

Requirement Priorities: Functional requirements will be explicitly labeled with priorities:

* High: Essential for the Release 1 (MVP) launch.
* Medium: Important but can be deferred to a minor update or Release 2.
* Low: Desirable enhancements planned for Release 3 or beyond.

**Requirement and Feature Identifiers**:

To maintain traceability between this SRS and the Vision and Scope Document, unique ID codes are manually assigned to features, risks, and metrics. If new items are added, they must follow this sequential numbering format:

FE-[X]: Major System Features (e.g., FE-1, FE-2)

FR-[X]: Specific Functional Requirements belonging to a Feature (e.g., FR-1.1, FR-1.2)

NFR-[X]: Non-Functional Requirements (Performance, Security, Reliability)

BO-[X]: Business Objectives (e.g., BO-1)

SM-[X]: Success Metrics (e.g., SM-1)

RI-[X]: Business Risks (e.g., RI-1)

LI-[X]: Limitations and Exclusions (e.g., LI-1)

## Project Scope

For a comprehensive breakdown of long-term strategic goals, detailed business objectives, and overall success metrics, please check out the foundational Vision and Scope Document - Food Delivery Platform (Version 1.0).

## References

Vision and Scope Document – Food Delivery Platform (Version 1.0)

# Overall Description

## Product Perspective

The Food Delivery Platform is an entirely new, independent product developed from the ground up. It is designed to serve as both a practical marketplace solution for the Vietnamese food and beverage service industry and a comprehensive academic reference for modern multi-role web application development.

**Context and Origin:**

Currently, the food delivery ecosystem often relies on fragmented third-party services, manual phone calls, or in-person visits, resulting in wasted time for customers, lost revenue for restaurants, and suboptimal routing for delivery personnel. While mature systems like GrabFood and ShopeeFood exist in the market , this platform is being newly architected to provide a streamlined, centralized alternative that connects the three core participants of the food delivery value chain:

1. **Customers (Food Orderers):** Seeking fast, convenient browsing and ordering.
2. **Restaurant Partners (Food Providers):** Seeking to expand their digital customer base and manage orders efficiently.
3. **Delivery Personnel (Shippers):** Seeking structured route optimization and flexible earning opportunities.

**System Ecosystem and Major Interfaces:**

The platform operates as a closely integrated mobile ecosystem. For Release 1 (MVP), the strategic priority is placed entirely on native mobile applications (iOS and Android) to provide a superior, platform-specific user experience, utilizing mobile hardware features like native GPS and push notifications.

The product consists of several interconnected front-end applications interacting with a centralized backend REST API and WebSocket server:

* **Customer Native App (iOS & Android):** A mobile application that interfaces with the backend to fetch restaurant data, manage carts, and receive real-time order status updates via push notifications and WebSockets.
* **Shipper Native App (iOS & Android):** A dedicated mobile application for delivery personnel equipped with live location tracking, allowing them to manage availability, receive dispatch requests, and trigger delivery lifecycle events.
* **Restaurant App/Portal (Tablet/Mobile):** A mobile-optimized application designed for kitchen environments to manage menus and quickly update order preparation statuses.
* **Admin Dashboard:** A centralized web-based interface reserved for system administrators to perform manual reviews, monitor platform health, and manage system configurations.

*External System Interfaces:* To support the native mobile environment, the system will interface with mobile-specific external services. This includes integration with Apple Push Notification service (APNs) and Firebase Cloud Messaging (FCM) for real-time alerts. Mapping and geolocation services will rely on native mobile SDKs provided by external partners (e.g., Google Maps SDK for iOS/Android or Mapbox). For Release 1, the platform supports Cash on Delivery (COD) and online payments via VNPay; subsequent releases may introduce additional mobile-optimized payment gateway integrations (e.g., MoMo, Apple Pay, Google Pay).

## User Classes and Characteristics

The Food Delivery Platform serves a multi-sided marketplace ecosystem. The system is designed for four primary user classes, each with distinct environments, technical proficiencies, and operational needs.

### Customers (Food Orderers) - *Favored User Class*

* **Description:** The general public who use the platform to discover restaurants, order food, and track deliveries. As the primary revenue drivers, their user experience dictates the platform's success, making them the favored user class.
* **Characteristics & Environment:** They encompass a wide demographic with varying levels of technical expertise. They will access the platform exclusively via native iOS or Android applications on their personal smartphones.
* **Key Needs:** They require a highly intuitive, low-friction mobile interface. Key features include fast search functionality, seamless cart management, clear checkout workflows supporting both Cash on Delivery (COD) and VNPay, and real-time push notifications for order tracking.

### Restaurant Partners (Food Providers)

* **Description:** Restaurant owners, managers, and kitchen staff responsible for maintaining menus, receiving orders, and preparing food.
* **Characteristics & Environment:** They operate in fast-paced, high-stress, and often messy kitchen environments. They will primarily use mobile-optimized native apps on tablets or large-screen smartphones. Their technical proficiency ranges from low to moderate.
* **Key Needs:** The interface must be highly visible and require minimal interaction to perform tasks. They need loud, distinct push notifications for new orders, high-contrast buttons to quickly accept orders or update preparation statuses, and simple mobile workflows for toggling menu item availability.

### Delivery Personnel (Shippers)

* **Description:** Independent gig-economy workers who pick up food from restaurants and deliver it to customers.
* **Characteristics & Environment:** They are constantly on the move, operating outdoors in various weather conditions and lighting. They rely entirely on their iOS or Android smartphones, which are typically mounted on their motorbikes. They are highly dependent on mobile data and GPS hardware.
* **Key Needs:** Their native mobile app must be optimized for low battery consumption and stable performance under fluctuating network conditions. The UI requires high contrast for sunlight readability, large touch targets, seamless native map integration for routing, and quick access to call customers or confirm deliveries.

### System Administrators

* **Description:** Platform operators and internal support staff responsible for maintaining ecosystem quality, onboarding users, and monitoring platform health.
* **Characteristics & Environment:** They have high technical proficiency and operate from standard office environments. Unlike the other user classes, administrators will interact with the platform via a secure, web-based desktop dashboard.
* **Key Needs:** They require comprehensive data views, efficient workflows for manually verifying and approving new Restaurant and Shipper registrations, and access to basic revenue and volume reporting.

## Operating Environment

The Food Delivery Platform will operate across a distributed environment, encompassing native mobile applications for end-users, web interfaces for administrators, and a containerized cloud backend.

**Client-Side Operating Environments:**

* **Customer Application:**
  + **Platform:** Native mobile applications for iOS and Android.
  + **OS Versions:** Target support for iOS 14.0 and later, and Android 8.0 (Oreo) and later.
  + **Hardware:** Smartphones with active internet connections (4G/5G/Wi-Fi) and location services enabled.
* **Shipper Application:**
  + **Platform:** Native mobile applications for iOS and Android.
  + **OS Versions:** Target support for iOS 14.0+ and Android 8.0+.
  + **Hardware:** Smartphones with persistent mobile data connections, GPS hardware, and sufficient battery capacity to handle continuous location tracking.
* **Restaurant Portal:**
  + **Platform:** Mobile-optimized native application or responsive web portal (accessible via Chrome, Safari).
  + **Hardware:** Tablets (e.g., iPads, Android tablets) or large-screen smartphones situated in kitchen environments, requiring a stable Wi-Fi or cellular connection.
* **Admin Dashboard:**
  + **Platform:** Web browser-based application.
  + **Compatibility:** Optimized for modern desktop browsers (Google Chrome, Mozilla Firefox, Apple Safari, Microsoft Edge).

**Server and Backend Environment:**

* **Hosting & Infrastructure:** The system will be hosted on a cloud platform (AWS, Google Cloud, or Azure), utilizing free-tier or student-tier resources for the initial release to manage academic budget constraints. Servers should ideally be located in a Southeast Asia region (e.g., Singapore or Vietnam, if available) to ensure low latency for users.
* **Server OS:** Linux-based environments operating Docker containers.
* **Backend Framework:** Node.js running the NestJS framework.
* **Database Systems:** PostgreSQL for the primary relational database and Redis for caching, session management, and WebSocket message brokering.
* **Asynchronous Processing:** Message queues managed via Bull Queue or RabbitMQ to handle asynchronous tasks like order dispatching at scale.
* **Real-time Communication:** WebSocket infrastructure powered by Socket.io, requiring a server environment configured to handle high numbers of concurrent, persistent TCP connections.

**Geographical Scope:**

* For Release 1 (MVP), the software's operational usage will be geographically restricted to a single, designated service area within Vietnam.

**Coexistence Requirements:**

* The mobile applications must peacefully coexist with native OS push notification services (APNs for Apple, FCM for Firebase/Android).
* The system must cleanly integrate with external mapping and geolocation SDKs (such as Google Maps or Mapbox) operating on the client devices.

## Design and Implementation Constraints

The design and development of the Food Delivery Platform are subject to several technical, financial, and operational constraints that the development team must adhere to during Release 1 (MVP):

### Budget and Infrastructure Constraints

* **Zero/Low-Cost Cloud Hosting:** Due to the academic nature of the project, all cloud infrastructure (servers, databases, message queues) must utilize free-tier or student-tier resources on platforms such as AWS, Google Cloud, or Microsoft Azure. This places strict limitations on available server RAM, CPU compute time, and database storage capacity.
* **Third-Party Services:** Any external APIs used for mapping (e.g., Google Maps, Mapbox) or push notifications (e.g., Firebase) must operate within their respective free usage quotas.

### Technology Stack Restrictions

* **Backend Architecture:** The backend API must be developed specifically using **NestJS (Node.js)**.
* **Database Systems:** The system is constrained to using **PostgreSQL** as the primary relational database and **Redis** for caching, session management, and WebSocket brokering. No other database engines (like MongoDB or MySQL) may be substituted without approval.
* **Deployment:** All backend services, including databases and queues, must be completely containerized using **Docker** and orchestrated via **Docker Compose** to ensure environment consistency.
* **Client Architecture:** Customer and Shipper frontends must be developed as **native mobile applications (iOS and Android)**. Web-based alternatives for these user classes are strictly excluded for Release 1.

### Team and Timeline Constraints

* **Resource Limitations:** The development team is restricted to an academic group size of 3 members. Consequently, complex features like ML-based predictive ETAs, multi-branch restaurant chains, and additional online payment integrations beyond VNPay (e.g., MoMo) are explicitly deferred to later releases.

### Hardware and Operating System Constraints

* **Shipper App Resource Usage:** Because delivery personnel rely heavily on mobile data and battery life, the native Shipper application is constrained to highly optimized background geolocation tracking. It must not drain a standard smartphone battery within a 4-hour delivery shift.
* **Real-time Concurrency:** The WebSocket server (Socket.io) must be artificially capped or highly optimized to prevent memory exhaustion on the free-tier server instances when handling multiple concurrent real-time order tracking connections.

### Security and Compliance

* **Credential Management:** All API keys, database credentials, and secret tokens must be injected via environment variables (.env files) and are strictly prohibited from being hardcoded or committed to any source code repository (e.g., GitHub, GitLab).

## Assumptions and Dependencies

The requirements and subsequent development of the Food Delivery Platform MVP (Release 1) are based on the following assumptions and external dependencies. If any of these factors change or prove incorrect, the project scope, timeline, or technical architecture may need to be re-evaluated.

**Assumptions:**

* **Hardware Availability:** It is assumed that all primary users (Customers and Shippers) possess iOS or Android smartphones capable of running modern native applications and have reliable access to mobile data (4G/5G). It is also assumed that Restaurant Partners have access to tablets or smartphones within their kitchen environments connected to stable Wi-Fi.
* **User Participation:** The marketplace model assumes a minimum viable pool of registered restaurants and delivery personnel will be onboarded prior to the launch to ensure orders can actually be fulfilled.
* **Operational Accuracy:** The system assumes that Restaurant Partners will accurately and promptly update their menu availability and operational hours, and that Shippers will honestly toggle their online/offline status.
* **Academic Budget:** It is assumed that the compute, database, and caching requirements for Release 1 can be comfortably supported within the free-tier or student-tier limits of the chosen cloud provider (AWS, Google Cloud, or Azure) without incurring unexpected financial costs.

**Dependencies:**

* **Push Notification Services:** Real-time mobile alerts rely entirely on the availability and performance of external services: Apple Push Notification service (APNs) for iOS devices and Firebase Cloud Messaging (FCM) for Android devices.
* **Mapping and Geolocation SDKs:** The accuracy of delivery routing and location tracking depends on third-party mobile SDKs (such as Google Maps SDK or Mapbox) and the native GPS hardware of the users' smartphones. The project is heavily dependent on staying within the free-tier usage quotas provided by these external mapping APIs.
* **App Store Review Processes:** Because the system prioritizes native mobile applications, the release timeline is strictly dependent on the review and approval processes of the Apple App Store and Google Play Store. Delays or rejections by these platforms are outside the development team's control.
* **Open Source Ecosystem:** The project relies on the continued maintenance, security, and compatibility of its core open-source frameworks, primarily NestJS, React Native (or chosen mobile framework), Docker, PostgreSQL, and Socket.io.

# System Features

## Customer Core Functionality (Native Mobile)

### Description

This feature encompasses the primary user journey for food orderers using the native iOS/Android application. It includes user registration, restaurant discovery, shopping cart management, and the checkout process. Because it directly drives platform transactions, this feature is of **High** priority.

### Stimulus/Response Sequences

| **Stimulus** | **Response** |
| --- | --- |
| Customer opens the app and enters email or OAuth credentials | System authenticates the user, creates/retrieves the profile, and grants access to the home screen. |
| Customer searches for a restaurant by name or filters by food category and geographic location. | System queries the database and returns a list of active restaurants matching the criteria within the user's delivery radius. |
| Customer adds a menu item (specifying quantity) to their shopping cart. | System queries the database and returns a list of active restaurants matching the criteria within the user's delivery radius. |
| Customer adds a menu item (specifying quantity) to their shopping cart. | System updates the cart state, calculates the running total, and stores the cart locally/remotely. |
| Customer proceeds to checkout, confirms the delivery address, and selects a payment method (COD or VNPay). | If COD is selected, the system finalizes the order, calculates the final total (including delivery fees), routes the order to the respective restaurant, and transitions the user to the order tracking screen. If VNPay is selected, the system initiates the VNPay payment flow and only finalizes/routes the order after successful payment confirmation. |

### Functional Requirements

* **FR-1.1:** The system shall allow customers to register, log in, and manage their profiles via email and standard OAuth providers (e.g., Google, Apple) on native mobile devices.
* **FR-1.2:** The system shall provide a search and filtering interface to browse restaurants by name, food category, and proximity.
* **FR-1.3:** The system shall prevent users from adding items from multiple different restaurants into a single shopping cart simultaneously. If attempted, the system shall prompt the user to clear the current cart first.
* **FR-1.4:** The system shall allow customers to select a payment method at checkout and shall support both Cash on Delivery (COD) and online payment via VNPay for Release 1.
* **FR-1.5:** The system shall validate that the provided delivery address falls within the restaurant's designated operational radius before allowing order submission.

## Real-Time Order Tracking (Native Mobile)

### Description

This feature provides visibility into the order lifecycle for the customer via the native mobile app. For the MVP, it utilizes WebSockets and native push notifications to provide status updates. Full map-based live GPS tracking is deferred to a future release. Priority is **High**.

### Stimulus/Response Sequences

* **Stimulus:** The restaurant partner accepts the order or updates the status to "Preparing".
  + **Response:** System sends a push notification (APNs/FCM) to the customer's phone and updates the in-app order status tracker via WebSocket.
* **Stimulus:** The shipper marks the order as "Picked Up".
  + **Response:** System alerts the customer that the food is on the way and provides the shipper's basic contact details (name, phone number, license plate).
* **Stimulus:** The shipper marks the order as "Delivered".
  + **Response:** System notifies the customer of the completed delivery and prompts for an optional rating (TBD in Release 2).

**3.2.3 Functional Requirements**

* **FR-2.1:** The system shall provide basic order status updates (Pending, Accepted, Preparing, Picked Up, Delivered, Cancelled) to the customer.
* **FR-2.2:** The system shall utilize WebSocket connectivity (Socket.io) to push status updates in real-time while the application is active in the foreground.
* **FR-2.3:** The system shall utilize native push notifications (APNs for iOS, FCM for Android) to deliver critical status updates when the application is in the background or closed.
* **FR-2.4:** If an order is canceled by the restaurant or administrator, the system shall immediately notify the customer and display the cancellation reason.

## Restaurant Menu and Order Management

### Description

A tablet or mobile-optimized portal allowing restaurant partners to manage their offerings, control item availability, and process incoming customer orders in a fast-paced kitchen environment. Priority is **High**.

### Stimulus/Response Sequences

* **Stimulus:** Restaurant staff toggles a menu item's status to "Sold Out".
  + **Response:** System immediately updates the customer-facing database, preventing new users from adding the item to their carts.
* **Stimulus:** A new customer order is routed to the restaurant.
  + **Response:** System plays a loud, continuous audio alert and displays a high-contrast popup until a staff member acknowledges it.
* **Stimulus:** Restaurant staff clicks "Accept Order".
  + **Response:** System stops the audio alert, moves the order to the "Preparing" queue, and triggers the customer notification sequence.

### Functional Requirements

* **FR-3.1:** The system shall allow authenticated restaurant staff to view, add, edit, and remove menu items, categories, and prices.
* **FR-3.2:** The system shall provide a single-tap toggle for staff to mark specific menu items or the entire restaurant as "Unavailable/Closed" in real-time.
* **FR-3.3:** The system must generate an auditory and visual alert for all incoming orders that bypasses standard device notification limits where possible.
* **FR-3.4:** The system shall allow staff to transition order states sequentially (New -> Accepted -> Preparing -> Ready for Pickup).

**3.4 Admin Dashboard (Web-Based)**

**3.4.1 Description**

This feature provides a secure, web-based dashboard for System Administrators to operate and govern the platform. It includes manual verification and approval workflows for Restaurant Partners and Delivery Personnel (Shippers), operational oversight of orders, lightweight reporting access, and configuration management required to run Release 1 (MVP). Priority is **High**.

**3.4.2 Stimulus/Response Sequences**

**•** **Stimulus:** An administrator logs in to the Admin Dashboard.

**–** **Response:** The system authenticates the administrator and displays an overview of pending approvals and active orders.

**•** **Stimulus:** An administrator reviews a pending Restaurant Partner or Shipper registration.

**–** **Response:** The system records an approve/reject decision, updates the applicant’s verification status, and makes the decision outcome visible to the applicant.

**•** **Stimulus:** An administrator monitors or intervenes in an order lifecycle.

**–** **Response:** The system displays order details and status history, and (when authorized) records administrative actions such as cancellation with a reason.

**3.4.3 Functional Requirements**

**•** **FR-4.1 (Must-have):** The system shall restrict Admin Dashboard access to authenticated System Administrator accounts.

**•** **FR-4.2 (Must-have):** The system shall enforce role-based access control (RBAC) for administrative actions (e.g., approvals, suspensions, order intervention, configuration updates).

**•** **FR-4.3 (Must-have):** The system shall allow administrators to view and search user accounts by role (Customer, Restaurant Partner, Shipper) and by account status (Pending Verification, Approved, Rejected, Suspended).

**•** **FR-4.4 (Must-have):** The system shall provide administrators with a queue of pending Restaurant Partner registrations requiring manual verification and approval.

**•** **FR-4.5 (Must-have):** The system shall provide administrators with a queue of pending Shipper registrations requiring manual verification and approval.

**•** **FR-4.6 (Must-have):** The system shall allow administrators to approve or reject Restaurant Partner and Shipper registrations and shall require a decision note (reason) for rejections.

**•** **FR-4.7 (Must-have):** Upon an approval or rejection decision, the system shall persist the verification status and shall make the status visible to the applicant upon subsequent authentication attempts.

**•** **FR-4.8 (Must-have):** The system shall allow administrators to suspend and reactivate Restaurant Partner and Shipper accounts.

**•** **FR-4.9 (Must-have):** When an account is suspended, the system shall prevent the suspended Restaurant Partner or Shipper from receiving or processing new orders.

**•** **FR-4.10 (Must-have):** The system shall provide administrators with an order monitoring view that lists orders and supports filtering at minimum by status, time range, and Restaurant Partner.

**•** **FR-4.11 (Must-have):** The system shall allow administrators to view order details including status history, assigned Shipper (if any), and any cancellation reason.

**•** **FR-4.12 (Must-have):** The system shall allow authorized administrators to cancel an order and shall require a cancellation reason; the system shall record the actor (administrator) and timestamp and notify affected parties.

**•** **FR-4.13 (Must-have):** The system shall allow administrators to configure the platform commission percentage used to calculate Estimated Platform Commission on completed orders.

**•** **FR-4.14 (Must-have):** The system shall maintain a history of commission configuration changes including effective date/time and the administrator who performed the change.

**•** **FR-4.15 (Must-have):** The system shall provide administrators access to the logical reports defined in the Reports section and shall support exporting report data in a machine-readable format (e.g., CSV).

**•** **FR-4.16 (Must-have):** The system shall record an immutable audit log for administrative actions, including at minimum administrator identity, action type, target entity, timestamp, and before/after status when applicable.

**•** **FR-4.17 (Should-have):** The system shall allow administrators to define and maintain the active service area boundaries and geographic zones used to enforce the MVP geographical scope constraint.

**•** **FR-4.18 (Should-have):** The system shall allow administrators to hide or unpublish Restaurant Partner profile information or menu items that violate platform content standards.

**•** **FR-4.19 (Should-have):** The system shall allow administrators to attach internal operational notes to a user account or order record for support and investigation purposes.

**•** **FR-4.20 (Nice-to-have):** The system shall provide near real-time operational monitoring widgets (e.g., counts of active orders by status, recent cancellations, and pending approvals) on the Admin Dashboard homepage.

**•** **FR-4.21 (Nice-to-have):** The system shall provide administrators with advanced monitoring and analytics views (e.g., heat maps by zone and peak-hour volume trends) for subsequent releases.

**•** **FR-4.22 (Nice-to-have):** The system shall support administrative fraud/anomaly detection alerts (e.g., repeated cancellation patterns) for subsequent releases.

# Data Requirements

This section defines the data entities, their attributes, and the relationships required to support the Food Delivery Platform's core operations. The system utilizes a relational database (PostgreSQL) as its primary data store, supplemented by Redis for ephemeral data (like active shopping carts and active shipper locations).

## Logical Data Model

The logical data model represents the core business objects manipulated by the system. Below is a structural breakdown of the primary database entities and their Entity-Relationship (ER) mappings.

![](data:image/png;base64...)

## Reports

For Release 1 (MVP), reporting capabilities are kept lightweight and are exclusively accessible to system administrators via the secure Web Dashboard. These reports are designed to monitor platform health, track early user adoption, and calculate basic financial metrics associated with Cash on Delivery (COD) transactions.

The system shall generate the following logical reports. (Note: Specific visual layouts and charts will be determined during the UI/UX design phase).

### Daily and Weekly Order Volume Report

* **Purpose:** To track overall platform usage and assess whether the system is meeting the target of 30 active restaurant partners and 500 active customers.
* **Content/Columns:** Date, Total Orders Placed, Total Orders Completed, Total Orders Cancelled, Average Order-to-Delivery Time.
* **Sorting & Grouping:** Data shall be grouped by Day or Week. Default sort is descending by Date.
* **Filtering Options:** Administrators must be able to filter the data by a specific Date Range and by specific Geographic Zones (if multiple zones are tested in the MVP).

### Financial & Commission Summary (COD)

* **Purpose:** Since all MVP transactions use Cash on Delivery (COD), the platform needs a reliable way to calculate the total Gross Merchandise Value (GMV) and the platform's expected commission from the restaurants.
* **Content/Columns:** Restaurant Name, Total Completed Orders, Gross Merchandise Value (Total COD collected by shippers for that restaurant), Estimated Platform Commission (calculated as a fixed percentage, TBD, of the GMV).
* **Sorting & Grouping:** Grouped by Restaurant. Default sort is descending by Gross Merchandise Value.
* **Filtering Options:** Filterable by Date Range (e.g., current month, previous week) to facilitate manual billing or reconciliation processes outside the system.

### User Registration & Approval Status Report

* **Purpose:** To monitor the backlog of new restaurants and delivery personnel awaiting manual verification.
* **Content/Columns:** User Role (Restaurant/Shipper), Total Pending Accounts, Total Approved Accounts, Total Rejected Accounts.
* **Sorting & Grouping:** Grouped by User Role and Status.
* **Filtering Options:** Filterable by Date of Registration.

## 4. Data Acquisition, Integrity, Retention, and Disposal

The platform is data-centric: it manages user accounts, restaurant catalogs, orders, payments, and delivery records. Authoritative business data lives in **PostgreSQL 18** (accessed through **Drizzle ORM**); transient and cache data live in **Redis**; binary media lives in **Cloudinary**.

### 4.1 Data Acquisition

**DATA-1.** Account, profile, restaurant, menu, address, and delivery-zone data shall be acquired through validated forms in the web, admin, and mobile clients. Every payload shall be validated server-side with DTO validation (class-validator) and client-side with Zod before persistence.

**DATA-2.** Dish and restaurant images shall be acquired via signed, direct-to-Cloudinary uploads; the API issues a short-lived signature and never proxies the binary itself. Each stored image record shall capture ownerId and ownerType.

**DATA-3.** Geographic coordinates for restaurant storefronts and delivery zones shall be acquired from the Photon geocoding API and stored as latitude/longitude; delivery reachability is computed via Haversine/PostGIS distance.

**DATA-4.** Payment outcomes shall be acquired from VNPay through both the browser return URL and the server-to-server IPN callback; the IPN callback is treated as authoritative.

**DATA-5.** Push-notification device tokens (FCM) shall be acquired from the mobile client on login and refreshed when the operating system rotates them.

**DATA-6.** Operational telemetry (traces, metrics, logs, and real-user-monitoring events) shall be acquired automatically by OpenTelemetry on the API, Grafana Faro on the web, and Sentry on mobile, without capturing payment-card data or passwords.

### 4.2 Data Integrity

**INT-1.** All multi-row business operations (order placement, payment settlement, refunds, status transitions) shall execute inside PostgreSQL ACID transactions; a failed step shall roll the whole operation back.

**INT-2.** The database schema shall be evolved exclusively through versioned Drizzle migrations (drizzle/out/); ad-hoc production schema edits are prohibited. Referential integrity shall be enforced with foreign keys and unique constraints.

**INT-3.** Price snapshotting: at checkout, the order shall persist a snapshot of every line-item price. Historical orders shall remain accurate even if the menu price later changes.

**INT-4.** Silent price-increase prevention: if an Anti-Corruption-Layer (ACL) snapshot price exceeds the price held in the cart at checkout, the system shall reject placement with a conflict error rather than silently charging more.

**INT-5.** ACL snapshots: the Ordering and Notification bounded contexts shall mirror the Restaurant/Menu data they depend on locally, so cross-context changes cannot corrupt in-flight orders.

**INT-6.** VNPay callbacks shall be verified with the gateway HMAC secure-hash before any state change, and shall be processed idempotently so a duplicated callback cannot double-settle or double-refund an order.

**INT-7.** Image deletion shall require ownership verification (the session owner matching ownerId) before the Cloudinary asset and its database record are removed.

**INT-8.** Order-lifecycle state transitions shall be guarded so that only legal transitions occur (for example, an order cannot move from delivered back to preparing).

### 4.3 Backups, Checkpointing, and Mirroring

**BAK-1.** The managed Render PostgreSQL instance shall have automated backups enabled with point-in-time recovery (PITR); the recovery point objective (RPO) target is at most 24 hours and the recovery time objective (RTO) target is at most 4 hours.

**BAK-2.** Redis shall be treated as a rebuildable cache and transient store (carts, sessions, rate-limit counters). It is not the system of record; loss of Redis shall degrade but not destroy business data.

**BAK-3.** Cloudinary acts as the durable store and CDN for media; the application shall not keep a second authoritative copy of image binaries.

**BAK-4.** Infrastructure shape shall be reproducible from Terraform (infra/render/) with remote state in HCP Terraform; runtime secrets are excluded from state.

### 4.4 Data Retention

**RET-1.** Financial and order records (orders, payments, refunds, payout history) shall be retained for the period required by Vietnamese commercial and tax law (a minimum of 10 years for accounting documents) and shall not be hard-deleted by ordinary user actions.

**RET-2.** Carts are transient and shall be stored in Redis with a bounded time-to-live (TTL) and evicted automatically when abandoned.

**RET-3.** Authentication sessions (better-auth cookies) shall expire after their configured lifetime; expired sessions shall be purged.

**RET-4.** Deleted menu items referenced by historical orders shall be preserved logically through order snapshots (RET-1) even after they are removed from the live catalog.

**RET-5.** Notification records and audit logs shall be retained for at least 12 months to support dispute resolution and compliance.

**RET-6.** Telemetry retention is governed by the Grafana Cloud, Sentry, and PostHog plans and shall not retain personally identifying payloads beyond what those policies allow.

### 4.5 Data Disposal

**DIS-1.** When a user requests account deletion, the platform shall delete or irreversibly anonymize personal data (name, contact details, addresses, device tokens) while retaining legally required transactional records in anonymized form.

**DIS-2.** Image disposal shall remove both the Cloudinary asset and its database record, and shall be blocked for non-owners (INT-7).

**DIS-3.** Residual data — orphaned cache entries, expired sessions, abandoned carts — shall be removed by TTL expiry or scheduled cleanup.

**DIS-4.** Local copies and interim artifacts (CI build outputs, Docker layers, Turborepo cache) shall not contain production secrets and shall be disposable.

**DIS-5.** Backup rotation shall ensure superseded backups are purged on the provider's retention schedule so disposed data is not indefinitely recoverable.

## 5. External Interface Requirements

### 5.1 User Interfaces

The platform presents three distinct user-facing surfaces plus auto-generated API documentation. The **web restaurant portal** (apps/web) serves restaurant owners and managers and is built with Vite 7, React 19, Tailwind v4, and shadcn/ui, served by nginx. The **admin panel** (apps/admin) serves platform administrators on the same stack as the web portal but without real-user monitoring or product analytics. The **mobile customer app** (apps/mobile) serves customers and is built with Expo SDK 55, React Native 0.83, and NativeWind v4.

**UI-1.** All web and admin screens shall follow the shared "Stitch" design system: an oklch color palette (green primary, amber secondary), Plus Jakarta Sans headings with Inter body text, Material Symbols iconography, and the documented glass, gradient, and shadow utilities.

**UI-2.** Layouts shall be responsive across the standard breakpoints (from mobile at 360 px through desktop at 1280 px and above) and shall support light and dark themes via design tokens.

**UI-3.** Every screen shall provide explicit loading (skeletons, not bare spinners), empty, and error states; transient failures use inline messages or toasts, never silent failure.

**UI-4.** Form fields shall show the label above the input, helper text where useful, and validation errors below the field; placeholders shall not be used as the only label.

**UI-5.** Interactive controls shall be keyboard-accessible and meet WCAG 2.1 AA contrast (at least 4.5:1 for body text and 3:1 for large text); primary actions provide a visible focus ring and an active/pressed state.

**UI-6.** A consistent global navigation (a sidebar on web and admin, a bottom tab bar on mobile) and a consistent primary call-to-action label per intent shall be used across the product.

**UI-7.** Detailed visual specifications are maintained separately in the design system and design.md; this SRS references them rather than duplicating pixel-level detail.

### 5.2 Software Interfaces

The API (apps/api, NestJS 11 on Node 22, listening on port 3000) is the integration hub. It exposes REST and WebSocket interfaces and consumes several managed services. The interfaces are as follows.

**SI-1. REST API (HTTP/JSON), clients to API.** All CRUD and command operations use JSON request and response bodies. The interactive contract is published via Swagger / Scalar at /api/docs. Requests are authenticated with better-auth cookies (withCredentials).

**SI-2. WebSocket (Socket.IO), API and clients bidirectional.** Real-time order-status and notification events (new order to restaurant, status changes to customer and shipper) are exchanged as event-based JSON messages.

**SI-3. PostgreSQL 18 via Drizzle ORM, API and database.** This is the system of record; the connection is configured via DATABASE\_URL and accessed only through Drizzle repositories.

**SI-4. Redis (ioredis), API and cache.** Holds the transient cart, session assistance, rate-limit counters, and ephemeral state; the connection is configured via REDIS\_URL/REDIS\_HOST.

**SI-5. Cloudinary (REST and signed upload), clients to Cloudinary and API to Cloudinary.** Provides image storage, transformation, and CDN delivery. The API issues signed upload parameters; deletion goes through the API with an ownership check.

**SI-6. VNPay payment gateway, API and VNPay.** Handles online payment through a browser redirect to VNPay plus a server-side IPN callback. HMAC vnp\_SecureHash verification is mandatory and amounts are in VND.

**SI-7. Photon geocoding API, API to Photon.** Resolves address text to coordinates for storefronts and delivery zones over REST/JSON.

**SI-8. Firebase Cloud Messaging (FCM), API to FCM to devices.** Delivers push notifications to the mobile app; device tokens are managed per device.

**SI-9. SMTP email service, API to SMTP.** Sends transactional email (verification, status, receipts), configured via the SMTP\_\* variables.

**SI-10. better-auth, API and clients.** Provides authentication and session management (cookie-based on web and admin, @better-auth/expo on mobile) and role utilities for the roles user, restaurant, shipper, and admin.

**SI-11. Grafana Cloud (OTLP), API to Grafana.** Receives traces, metrics, and logs over the OpenTelemetry Protocol.

**SI-12. Grafana Faro, PostHog, and Sentry, clients to SaaS.** Provide web real-user monitoring and product analytics on the web, and crash and performance monitoring on mobile.

The following cross-cutting interface requirements also apply. **SI-13:** communication between bounded contexts shall follow the documented patterns — ACL snapshots for core domains (Ordering, Notification) and the public-API/interface-symbol pattern for support domains (Payment, IAM, Image); contexts shall not read or write another context's tables directly. **SI-14:** money amounts crossing any interface shall be expressed as integer VND (đồng, with no fractional units). **SI-15:** outbound calls to third-party services (VNPay, Photon, Cloudinary, FCM, SMTP) shall apply timeouts and, where the operation is user-visible such as notifications, a background retry with backoff on failure. **SI-16:** the API shall restrict cross-origin requests to the configured client origins (a CORS allowlist) and require credentials for authenticated routes.

### 5.3 Hardware Interfaces

The platform is cloud-hosted software with no proprietary hardware; the relevant hardware interactions are on client devices.

**HW-1.** The mobile app shall access the device GPS/location services to determine the customer's delivery position and to support shipper navigation; permission shall be requested at point of use.

**HW-2.** The mobile app shall access the device camera and photo library (where a feature requires an image) and the notification subsystem for FCM push delivery.

**HW-3.** Web and admin clients require only a standard input device (keyboard and pointer, or touch) and a modern browser; no specialized peripherals are required.

**HW-4.** Server components run on the cloud provider's virtualized compute (Render); no direct physical-hardware interface requirements apply to the backend.

### 5.4 Communications Interfaces

**COM-1.** All client-server traffic shall use HTTPS/TLS 1.2 or later; WebSocket traffic shall use WSS. Plain HTTP is permitted only for local development.

**COM-2.** Session cookies shall be issued with the Secure, HttpOnly, and an appropriate SameSite policy; cross-site authenticated requests require credentials: 'include'.

**COM-3.** Email shall be sent over authenticated SMTP (TLS); push shall be delivered over FCM's HTTPS transport.

**COM-4.** The VNPay redirect and IPN exchange shall carry the HMAC secure hash; the API shall reject any message whose signature does not validate.

**COM-5.** Real-time delivery (WebSocket) shall target an end-to-end event latency of at most 2 seconds under nominal load and shall reconnect automatically after transient disconnects.

**COM-6.** Telemetry export shall use OTLP over HTTPS to Grafana Cloud; failures to export telemetry shall not block or degrade business request handling.

## 6. Quality Attributes

### 6.1 Usability

**USE-1.** A new restaurant owner shall be able to register, create a profile, add a first menu item (with photo), and define a delivery zone without external assistance, guided by in-product flows.

**USE-2.** The product shall conform to the Stitch design system (UI-1) for visual and interaction consistency, minimizing relearning across screens.

**USE-3.** The user interface shall meet WCAG 2.1 AA accessibility (contrast, keyboard operability, focus visibility, and semantic structure).

**USE-4.** Destructive actions (delete menu item, cancel order, suspend account) shall require explicit confirmation and shall be recoverable or clearly final by design.

**USE-5.** All user-facing currency, dates, and numbers shall be formatted per the customer locale (see Section 7).

### 6.2 Performance

**PERF-1.** For read endpoints under nominal load, the API shall return a response with a p95 latency of at most 300 ms and a p99 of at most 800 ms, excluding third-party gateway time.

**PERF-2.** Write and command endpoints (place order, accept order) shall complete with a p95 of at most 700 ms, excluding external payment-gateway round-trips.

**PERF-3.** Web pages shall target Core Web Vitals of LCP at most 2.5 s, INP at most 200 ms, and CLS at most 0.1 on a mid-range device over a typical 4G connection.

**PERF-4.** Real-time order and notification events shall be delivered within at most 2 seconds (COM-5).

**PERF-5.** The system shall sustain at least 500 concurrent active users and 50 order placements per minute without breaching PERF-1 or PERF-2, scaling horizontally if exceeded.

**PERF-6.** Hot read paths (restaurant lists, menus, carts) shall use Redis caching and database indexing to meet PERF-1.

### 6.3 Security

**SEC-1.** Authentication shall be handled by better-auth; passwords shall be stored only as salted one-way hashes, never in plaintext or reversibly.

**SEC-2.** Authorization shall be role-based using the global AuthGuard and @Roles([...]) decorators for the roles user, restaurant, shipper, and admin; every protected endpoint shall enforce both authentication and the correct role.

**SEC-3.** Resource-level ownership shall be enforced (for example, a restaurant can only mutate its own menu, and an image can only be deleted by its owner).

**SEC-4.** All input shall be validated and sanitized server-side; the ORM shall be used in a way that prevents SQL injection (parameterized queries only).

**SEC-5.** Secrets (BETTER\_AUTH\_SECRET, CLOUDINARY\_\*, VNPAY\_\*, SMTP\_\*, and database/Redis credentials) shall be stored in Render service settings or environment groups and shall never be committed to the repository or Terraform state.

**SEC-6.** Payment integrity shall be protected by VNPay HMAC verification and idempotent settlement (INT-6); the platform shall not store raw card data.

**SEC-7.** Transport shall be encrypted end-to-end (COM-1) and CORS shall be restricted to known origins (SI-16).

**SEC-8.** The platform shall apply rate limiting and abuse protection on authentication and ordering endpoints.

**SEC-9.** The product shall align with the OWASP Top 10 mitigations and with Vietnam's Personal Data Protection Decree (PDPD 13/2023) for handling of personal data.

### 6.4 Safety

The product is commerce software and is not life-critical, but it manages money, food, and field workers.

**SAF-1.** The system shall prevent financial harm by guaranteeing payment correctness: no double-charge, no double-refund, and no charge without a corresponding confirmed order (INT-6, INT-3).

**SAF-2.** Where dietary or allergen information is provided by a restaurant, the system shall display it accurately on the item detail screen and shall not silently drop it.

**SAF-3.** The system shall not instruct or require shippers to perform unsafe actions; navigation and assignment features are advisory and shall respect driver acceptance and availability controls.

**SAF-4.** Administrative suspension and reactivation of partner accounts shall take effect immediately to allow rapid response to unsafe or fraudulent behavior.

### 6.5 Other Quality Attributes

**AVL-1 (Availability).** The platform shall target service availability of at least 99.5% monthly for the API and web portal, supported by provider health checks and automatic restart; telemetry failures shall not affect availability.

**REL-1 (Reliability).** Critical asynchronous actions (notifications) shall use a self-healing background retry; failed FCM or SMTP sends shall be retried with backoff.

**SCA-1 (Scalability).** The API shall be stateless (shared state in Postgres and Redis) so it can scale horizontally behind the provider's load balancing.

**MNT-1 (Maintainability and Modifiability).** The codebase shall preserve DDD bounded-context boundaries (Section 5.2, SI-13) and pass lint, type-check, unit, and end-to-end gates in CI before merge.

**OBS-1 (Observability).** The system shall emit traces, metrics, and logs (OpenTelemetry to Grafana Cloud), web real-user monitoring (Faro), and mobile crash reporting (Sentry).

**INT-OP-1 (Interoperability).** External integration shall use standard REST/JSON, WebSocket, OTLP, and SMTP so components can be replaced without bespoke protocols.

**PORT-1 (Portability).** The backend and frontends shall be containerized (Docker, GHCR images) so they can be deployed to any compatible container host.

When attributes conflict, the resolution order is security and data integrity first, then reliability and availability, then performance, and finally convenience and usability polish. For example, payment-integrity checks (INT-6) take precedence over shaving latency (PERF-2).

## 7. Internationalization and Localization Requirements

The platform's primary market is Vietnam.

**I18N-1 (Currency).** All monetary values shall be Vietnamese đồng (VND), stored and transmitted as integers (with no decimal subunit) and displayed with thousands separators and the ₫ symbol (for example, 135,000 ₫).

**I18N-2 (Language).** The product shall support Vietnamese (primary) and English (secondary) user-facing text, with full UTF-8 support for Vietnamese diacritics in names, addresses, and dish titles.

**I18N-3 (Dates and times).** Timestamps shall be stored in UTC and presented in the Asia/Ho\_Chi\_Minh (UTC+7) timezone using locale-appropriate date and number formats.

**I18N-4 (Names).** The system shall accommodate Vietnamese name ordering (family name first) and shall not assume a Western given-name/surname split that would corrupt display.

**I18N-5 (Addresses and phone numbers).** Address capture shall follow Vietnamese conventions (ward, district, province) and phone numbers shall accept the +84 and local 0… formats.

**I18N-6 (Units).** Distances and delivery radii shall use the metric system (kilometers); measurements shall be metric throughout.

**I18N-7 (Localization isolation).** User-facing strings shall be externalized so additional locales can be added without code changes to business logic.

## 8. Other Requirements

### 8.1 Legal, Regulatory, and Compliance

**LEG-1.** The platform shall comply with Vietnam's Personal Data Protection Decree (PDPD 13/2023/ND-CP) regarding consent, access, and deletion of personal data (this supports DIS-1).

**LEG-2.** Online payment shall be conducted through the licensed VNPay gateway in conformance with State Bank of Vietnam payment regulations; the platform itself shall not become a card-data processor.

**LEG-3.** Order and payment records shall be retained for the legally mandated accounting period (RET-1) to support tax and audit obligations.

**LEG-4.** E-commerce operation shall observe Vietnam's decrees on e-commerce activity (information disclosure and dispute handling).

### 8.2 Installation, Configuration, Startup, and Shutdown

**OPS-1.** Local infrastructure (PostgreSQL 18 and Redis) shall be brought up via docker compose up -d; applications run via Turborepo (pnpm dev).

**OPS-2.** Configuration shall be supplied through environment variables (.env), documented in ARCHITECTURE.md; no environment-specific values shall be hard-coded.

**OPS-3.** The database schema shall be applied via migrations (pnpm --filter api db:migrate, or db:push in CI) before the API serves traffic.

**OPS-4.** The API shall bootstrap OpenTelemetry before the application (node --require ./dist/telemetry dist/main) so startup is fully traced.

**OPS-5.** Deployment shall be automated through GitHub Actions to GHCR Docker images to Render deploy hooks, with infrastructure shape governed by Terraform.

### 8.3 Logging, Monitoring, and Audit Trail

**LOG-1.** The API shall emit structured logs and distributed traces correlating a request across bounded contexts.

**LOG-2.** Administrative actions (approvals, suspensions, manual cancellations and refunds, role changes) shall be recorded in an audit trail capturing actor, action, target, and timestamp, retained per RET-5.

**LOG-3.** Logs and telemetry shall not record secrets, passwords, full payment credentials, or unnecessary personal data.

**LOG-4.** Platform health (order volume, error rates, restaurant online/offline status) shall be observable through dashboards for administrators (this supports UC-30 and UC-34).

## 9. Glossary

**ACL (Anti-Corruption Layer)** — A pattern where a bounded context keeps a local snapshot or translation of another context's data to avoid direct coupling.

**Admin** — Platform-staff role with governance privileges (approvals, suspensions, reports).

**ASR** — Architecturally Significant Requirement.

**Bounded Context (BC)** — A DDD boundary owning its own model and tables (for example, Ordering, Payment, Notification).

**CDN** — Content Delivery Network; Cloudinary serves media via CDN.

**CORS** — Cross-Origin Resource Sharing; the API restricts which web origins may call it.

**CQRS** — Command Query Responsibility Segregation; separating write commands from read queries.

**DDD** — Domain-Driven Design.

**DTO** — Data Transfer Object; a validated request or response shape.

**FCM** — Firebase Cloud Messaging; Google's push-notification transport.

**GMV** — Gross Merchandise Value; total order value flowing through the platform.

**HMAC** — Hash-based Message Authentication Code; used to verify VNPay messages.

**IAM** — Identity and Access Management.

**IPN** — Instant Payment Notification; VNPay's server-to-server callback.

**OTLP** — OpenTelemetry Protocol; the transport for traces, metrics, and logs.

**PDPD** — Personal Data Protection Decree (Vietnam, 13/2023/ND-CP).

**PITR** — Point-In-Time Recovery for databases.

**RPO / RTO** — Recovery Point Objective / Recovery Time Objective.

**RUM** — Real-User Monitoring (Grafana Faro on the web client).

**Shipper** — Delivery-driver role responsible for picking up and delivering orders.

**SPA** — Single-Page Application (the web and admin clients).

**SRS** — Software Requirements Specification (this document family).

**TTL** — Time-To-Live; the expiry duration for transient data (carts, sessions).

**VND (đồng)** — Vietnamese currency; integer-only amounts in this system.

**VNPay** — Vietnamese online payment gateway integrated for online payments.

**WCAG** — Web Content Accessibility Guidelines (target level AA).

**WSS** — WebSocket Secure (TLS-encrypted WebSocket).

## 10. Analysis Models

The following models support and illustrate the requirements above. They are maintained as separate artifacts and incorporated here by reference.

The **system and API architecture** model — [docs/api-architecture.mmd](http://../../../../docs/api-architecture.mmd) — describes the bounded contexts, modules, and their communication patterns (ACL versus public-API). The **repository and project structure** model — [docs/project-structure.mmd](http://../../../../docs/project-structure.mmd) — describes the monorepo layout of apps/ and infra/. The **local development environment** model — [docs/docker-dev-environment.mmd](http://../../../../docs/docker-dev-environment.mmd) — describes the Compose topology (Postgres and Redis).

The **sequence diagrams** — [SRS\_SequenceDiagrams.md](http://./SRS_SequenceDiagrams.md) — describe the interaction flows for key use cases (ordering, payment, delivery). The **use-case diagrams** — [UC diagrams/](http://./UC%20diagrams/) — describe the actor-to-use-case relationships for UC-1 through UC-35. The **bounded-context model** — [bounded-context.md](http://../bounded-context.md) — describes the context map and ownership of domain concepts. The **data model (ERD)** is defined authoritatively in apps/api/src/drizzle/schema.ts as Drizzle ORM entity and relationship definitions. The **quality-attribute (utility) tree** — [Utility-Tree-ASRs.md](http://./Utility-Tree-ASRs.md) — describes the architecturally significant requirements and their scenarios.

*End of SRS continuation. Sections 1–3 are in* [*SRS\_FoodDelivery.md*](http://./SRS_FoodDelivery.md)*.*