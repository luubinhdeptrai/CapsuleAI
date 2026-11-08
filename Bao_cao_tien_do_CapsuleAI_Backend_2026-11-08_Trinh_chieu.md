# CAPSULEAI

## BÁO CÁO TIẾN ĐỘ BACKEND

**Sprint 1–2 · Identity · Password Recovery · Wardrobe**

Java 21 · Spring Boot · PostgreSQL

**Trọng tâm:** Architecture · Security · Data Integrity · Idempotency

**08/11/2026**

---

# 01. Tech Stack & Engineering Foundation

- **Java 21** — ngôn ngữ phát triển Backend.
- **Spring Boot 4.1.1** — REST API và application configuration.
- **Spring Security 7.1.1** — authentication và authorization.
- **Spring JDBC (`JdbcClient`)** — kiểm soát SQL và persistence rõ ràng.
- **PostgreSQL 17** — relational database và transaction.
- **Flyway** — quản lý database schema versioning.
- **Docker Compose** — môi trường phát triển cục bộ.
- **Maven · JUnit · Testcontainers · GitHub Actions** — build, test, CI.

---

# 02. Software Architecture

- **Modular Monolith** — một ứng dụng, nhiều business modules độc lập.
- **Bounded Contexts** — phân chia theo nghiệp vụ, không theo loại kỹ thuật.
- **Identity Module** — account, authentication, session, recovery.
- **Wardrobe Module** — garment domain và garment persistence.
- **Pragmatic Clean Architecture** — `domain / application / adapter / configuration`.
- **Ports & Adapters** — tách business logic khỏi JDBC, SMTP, HTTP.
- **Published Module API** — giao tiếp giữa modules qua Java contracts.
- **Không dùng Microservices/Redis/Kafka sớm** — hạn chế complexity không cần thiết.

---

# 03. Database Design & Ownership

- **Schema Ownership** — tách `identity.*` và `wardrobe.*` trong PostgreSQL.
- **No cross-schema FK** — giữ ranh giới dữ liệu giữa các modules.
- **No cross-schema SQL** — Wardrobe không truy cập trực tiếp Identity tables.
- **Database Constraints** — bảo vệ data integrity ở tầng lưu trữ.
- **Transaction Boundaries** — ghi nhiều bảng theo một thao tác nhất quán.

---

# 04. Identity & Authentication — Sprint 1

- **Registration** — tạo tài khoản và kiểm tra dữ liệu đầu vào.
- **Login** — xác thực người dùng.
- **PBKDF2 Password Hashing** — không lưu plaintext password.
- **JWT Access Token** — xác thực API qua Bearer token.
- **Persistent Session** — lưu trạng thái session ở PostgreSQL.
- **Session Authority Check** — kiểm tra session còn hiệu lực khi dùng JWT.
- **Refresh-Token Rotation** — cấp refresh token mới sau mỗi lần refresh.
- **Reuse Detection** — phát hiện refresh token cũ bị tái sử dụng.
- **Logout / Revocation** — vô hiệu hóa session dù JWT còn hạn.

---

# 05. Password Recovery — US-AUTH-005

- **Request / Check / Complete** — ba API cho quy trình reset password.
- **Cryptographically Random Token** — token khó dự đoán.
- **Hashed Reset Token** — không lưu raw reset token trong database.
- **30-minute Expiration** — giới hạn thời gian sử dụng.
- **Account-wide Session Revocation** — thu hồi các active sessions sau reset.

---

# 06. Password Recovery — Security & Reliability

- **Rate Limiting per Email** — giới hạn số lần yêu cầu reset.
- **Rate Limiting per Client IP** — hạn chế request/verification abuse.
- **Generic Response** — tránh tiết lộ email có tài khoản hay không.
- **SMTP Adapter** — tách việc gửi email khỏi application logic.
- **Retention & Cleanup Job** — dọn dữ liệu reset theo chính sách.

---

# 07. Manual Garment Creation — US-GAR-001

- **`POST /api/v1/wardrobe/garments`** — API tạo garment.
- **JWT-based Ownership** — chủ sở hữu lấy từ authenticated identity.
- **Imageless Creation** — tạo garment không phụ thuộc ảnh hoặc AI.
- **Category + Color** — hai thuộc tính bắt buộc.

---

# 08. Idempotency, Concurrency & Safe Writes

- **`Idempotency-Key`** — định danh một lần tạo garment về mặt logic.
- **`creation_fingerprint`** — SHA-256 của dữ liệu đã được chấp nhận.
- **Unique `(owner, creation_key)`** — chặn duplicate do retry.
- **Same Key + Same Data** — trả garment đã tạo, không thêm bản ghi.
- **Same Key + Different Data** — từ chối request không nhất quán.
- **Different Key + Same Data** — cho phép sở hữu hai garment giống nhau.
- **PostgreSQL `ON CONFLICT`** — xử lý cạnh tranh ghi cùng key.
- **Transaction Rollback** — lỗi ghi không để lại garment dở dang.

---

# 09. Development Workflow & Quality Strategy

- **BRD / PRD / SRS** — xác định business và system requirements.
- **User Story + Acceptance Criteria** — định nghĩa phạm vi và kết quả cần đạt.
- **Requirements Traceability** — liên kết requirement → code → test.
- **Just-in-Time Design** — thiết kế chi tiết theo từng Sprint Story.
- **Implementation Guide** — thống nhất design trước khi coding.
- **Unit Tests** — kiểm tra domain và application rules.
- **Integration Tests + Testcontainers** — xác minh PostgreSQL/API thật.
- **Architecture / OpenAPI Contract Tests** — kiểm soát boundary và API.
- **CI + `mvnw clean verify`** — regression trước khi nghiệm thu.

---
