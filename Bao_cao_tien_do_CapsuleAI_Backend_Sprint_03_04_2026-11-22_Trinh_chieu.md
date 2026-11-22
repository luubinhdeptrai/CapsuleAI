# CAPSULEAI

## BÁO CÁO TIẾN ĐỘ BACKEND

**Sprint 3–4 · Garment Editing · Private Image Processing · Personal Context · Removal · Search**

Java 21 · Spring Boot 4.1.1 · PostgreSQL 17

**Trọng tâm:** Data Integrity · Concurrency Control · Private Media · Reliable Cleanup · Search & Pagination

**22/11/2026**

---

# 01. Tổng Quan Tiến Độ — Sprint 3 & Sprint 4

- **Sprint 3 — Hoàn thành 3/3 User Stories:** `US-GAR-002`, `US-GAR-003`, `US-PROF-001`.
- **Sprint 4 — Đã triển khai 3/6 User Stories:** `US-GAR-005`, `US-GAR-006`, `US-WAR-003`.
- **Trọng tâm Sprint 3:** chỉnh sửa dữ liệu đã xác nhận, quản lý ảnh riêng tư, khai báo sở thích cá nhân.
- **Trọng tâm Sprint 4 đã triển khai:** xóa garment an toàn, bổ sung ảnh cho garment hiện có, tìm kiếm và lọc wardrobe.
- **Architecture:** tiếp tục sử dụng Modular Monolith, Clean Architecture và Ports & Adapters.

---

# 02. Garment Editing — US-GAR-002 (Sprint 3)

- **`PATCH /api/v1/wardrobe/garments/{garmentId}`** — cập nhật thông tin garment hiện có.

---

# 03. Optimistic Locking & Safe Concurrent Editing — US-GAR-002

- **Garment Revision** — mỗi garment có revision phản ánh phiên bản dữ liệu đã được xác nhận.
- **`expectedRevision`** — client gửi phiên bản đã xem khi yêu cầu chỉnh sửa.
- **Conditional SQL Update** — chỉ cập nhật nếu revision trong PostgreSQL còn khớp.
- **`409 REVISION_CONFLICT`** — ngăn một thiết bị ghi đè thay đổi đã được chấp nhận từ thiết bị khác.
- **Single Successful Writer** — hai request cạnh tranh cùng revision không được cùng ghi đè thành công.
- **Immutable-style Domain Transition** — xây dựng trạng thái garment mới qua domain logic trước khi lưu.
- **Revision-aware API** — client có cơ sở tải lại dữ liệu và xử lý conflict minh bạch.

---

# 04. Private Photo Intake Pipeline — US-GAR-003 (Sprint 3)

- **Multipart Photo Upload** — tiếp nhận ảnh qua `POST /api/v1/wardrobe/photo-uploads`.
- **JPEG / PNG / HEIC / HEIF** — hỗ trợ luồng kiểm tra và xử lý các định dạng ảnh được chọn.

---

# 05. Secure Image Processing — US-GAR-003

- **Content Validation** — kiểm tra nội dung ảnh thực, không chỉ tin vào tên file hoặc `Content-Type`.
- **15 MB Upload Limit** — chặn file đầu vào vượt giới hạn đã quy định.
- **Image Dimension & Pixel Limits** — giảm nguy cơ decompression bomb và tiêu thụ RAM bất thường.
- **Decode Resource Budget** — giới hạn thời gian, bộ nhớ và mức sử dụng tài nguyên khi xử lý ảnh.
- **Metadata Removal** — tạo ảnh chuẩn hóa không mang metadata không cần thiết của file gốc.
- **Fail-closed Handling** — ảnh không hợp lệ hoặc không thể giải mã an toàn được từ chối.
- **Private Object Storage** — ảnh nằm trong S3-compatible storage, không sử dụng public bucket URL để cấp quyền truy cập.

---

# 06. Personal Context — US-PROF-001 (Sprint 3)

- **Personal Context Module** — quản lý sở thích và nhu cầu độc lập với Wardrobe.
- **`GET /api/v1/personal-context/preferences`** — đọc Style Preferences và Common Needs.
- **`PATCH /api/v1/personal-context/preferences`** — cập nhật từng phần với presence-aware semantics.
- **Style Preferences** — lưu tập các phong cách do người dùng chủ động lựa chọn.
- **Six Common Needs** — quản lý sáu nhu cầu dài hạn với mức ưu tiên từ `0` đến `3`.

---

# 07. Safe Garment Removal — US-GAR-005 (Sprint 4)

- **`POST /api/v1/wardrobe/garments/{garmentId}/removal`** — lệnh xóa có `confirmed` và `expectedRevision`.
- **Logical Removal** — garment rời khỏi wardrobe hiện tại ngay khi transaction commit.
- **Minimal Historical Record** — chỉ giữ identity, owner, creation fingerprint, Category, Color, provenance, timestamps và revision.
- **Durable Cleanup Obligation** — ghi nhận nghĩa vụ dọn dẹp trong cùng transaction với removal.

---

# 08. Background Cleanup & Failure Recovery — US-GAR-005

- **Database-backed Cleanup Worker** — xử lý các nhiệm vụ xóa còn tồn đọng từ PostgreSQL.
- **Lease & Retry** — công việc chưa hoàn thành được xử lý lại khi đến hạn.
- **Two-stage Cleanup** — giảm dữ liệu PostgreSQL trước, xóa object ảnh trong S3 sau.
- **External I/O Outside Transaction** — không giữ khóa database trong lúc gọi S3.
- **Idempotent Object Deletion** — retry an toàn kể cả khi server crash sau thao tác xóa ảnh.

---

# 09. Attach Photo to Existing Garment — US-GAR-006 (Sprint 4)

- **`POST /api/v1/wardrobe/garments/{garmentId}/photo-attachment`** — gắn ảnh vào garment hiện có.
- **Existing Imageless Garment Only** — chỉ garment hiện tại, chưa có ảnh và thuộc đúng owner được phép nhận ảnh.

---

# 10. Wardrobe Search & Filtering — US-WAR-003 (Sprint 4)

- **`GET /api/v1/wardrobe/garments`** — mở rộng API browse bằng `q`, `category` và `color`.
- **Vietnamese Search Aliases** — tìm bằng tên gọi tiếng Việt và nhãn tiếng Anh của controlled values.
- **195 Vocabulary Values** — từ điển cho Category, Subtype, Color, Material, Pattern và Style.
- **Literal Substring Search** — tìm theo cụm từ; không phân biệt hoa/thường nhưng có phân biệt dấu.
- **Unicode NFC** — chuẩn hóa input tiếng Việt và xử lý một số biến thể đặt dấu.
- **OR Within Filter Group** — nhiều Category hoặc nhiều Color cùng nhóm được kết hợp bằng OR.
- **AND Across Conditions** — `q`, Category và Color kết hợp bằng AND.
- **Owner & Current Only** — không trả về garment đã xóa hoặc thuộc tài khoản khác.

---

# 11. Kết Quả Đạt Được & Kế Hoạch Tiếp Theo

- **Sprint 3:** hoàn thành Backend cho `US-GAR-002`, `US-GAR-003`, `US-PROF-001`.
- **Sprint 4:** hoàn thành Backend cho `US-GAR-005`, `US-GAR-006`, `US-WAR-003`.
- **Năng lực kỹ thuật nổi bật:** Optimistic Locking · Atomic Transactions · S3 Private Media · Background Cleanup · Idempotency · Keyset Pagination.
- **Kiểm soát chất lượng:** Unit Tests · Integration Tests · Concurrency Tests · OpenAPI Contract · Regression Testing.
- **Sprint 4 — Tiếp tục triển khai:** `US-PROF-002` (Optional Body/Gender Context).
- **Sprint 4 — Tiếp tục triển khai:** `US-CTX-001` (Location & Weather Context).
- **Sprint 4 — Tiếp tục triển khai:** `US-BULK-001` (Bulk Garment Import).
- **Trạng thái:** 3/6 User Stories Sprint 4 đã triển khai; 3 User Stories còn lại chưa triển khai.

---
