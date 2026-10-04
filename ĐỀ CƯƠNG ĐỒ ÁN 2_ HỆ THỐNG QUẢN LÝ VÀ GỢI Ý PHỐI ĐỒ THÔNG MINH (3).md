# ĐỀ CƯƠNG ĐỒ ÁN 2: HỆ THỐNG QUẢN LÝ VÀ GỢI Ý PHỐI ĐỒ THÔNG MINH

## 1. Động lực nghiên cứu và lý do chọn đề tài

### 1.1. Bối cảnh thực tiễn và bài toán thực tế

* **Hiện tượng "Tủ đồ đầy nhưng không có gì để mặc" (Decision Fatigue):**
* Người dùng hiện đại thường đối mặt với áp lực lựa chọn trang phục mỗi sáng, dẫn đến việc lặp đi lặp lại một vài bộ đồ quen thuộc dù sở hữu số lượng quần áo lớn.
* Việc thiếu cái nhìn tổng quan về các món đồ hiện có làm giảm đáng kể khả năng khai thác tối đa giá trị sử dụng của tủ đồ.
* **Hành vi mua sắm bốc đồng và lãng phí thời trang (Impulse Buying & Fast Fashion):**
* Người tiêu dùng thường mua sắm theo cảm tính các món đồ giảm giá hoặc theo xu hướng mà không đánh giá khả năng kết hợp với những trang phục sẵn có.
* Hệ quả là nhiều món đồ chỉ mặc 1–2 lần hoặc không bao giờ được sử dụng, gây lãng phí tài chính cá nhân và tạo áp lực lên môi trường (rác thải dệt may).
* **Nâng cao chất lượng phối đồ và phong cách cá nhân (Elevating Personal Style & Outfit Quality):**
  + Cung cấp các giải pháp gợi ý trang phục thông minh dựa trên lý thuyết phối màu, quy tắc chuẩn hóa phom dáng và sự tương thích về ngữ cảnh sử dụng (thời tiết, sự kiện, công việc, màu sắc, dáng người, dáng quần áo).
  + Hỗ trợ người dùng tự tin hơn trong định hình phong cách cá nhân, giảm thiểu rủi ro phối đồ lệch tông hoặc không phù hợp với hoàn cảnh thực tế.
* **Rào cản từ các công cụ truyền thống:**
  + Các ứng dụng số hóa tủ đồ trước đây đòi hỏi người dùng phải nhập liệu và cắt phông nền thủ công quá nhiều, khiến tỷ lệ rời bỏ ứng dụng (drop-off rate) ở giai đoạn khởi tạo rất cao.

### 1.2. Lý do chọn đề tài và tính mới

* **Kết hợp Thị giác máy tính (Computer Vision) để khó khăn nhập liệu:** Tự động hóa hoàn toàn khâu tách nền và trích xuất thuộc tính (loại trang phục, màu sắc, chất liệu), giúp việc số hóa tủ đồ diễn ra nhanh chóng.
* **Thuật toán gợi ý trang phục đa yếu tố (Context-aware Styling Engine):** Không chỉ ghép đồ ngẫu nhiên mà kết hợp giữa lý thuyết bánh xe màu sắc (Color Theory), phân loại theo mùa/thời tiết thời gian thực và sở thích cá nhân.
* **Điểm sáng tạo cốt lõi – Thuật toán phân tích khoảng trống tủ đồ (Wardrobe Multiplier / Gap Analysis):**
  + Thay vì chỉ gợi ý đồ để kích thích mua sắm vô tội vạ, hệ thống áp dụng mô hình tính toán độ hữu dụng biên (Marginal Utility).
  + Đề xuất món đồ cần mua dựa trên số lượng outfit mới được tạo ra (ví dụ: *"Đôi giày sneaker trắng này sẽ mở khóa thêm 14 outfit từ tủ đồ hiện tại"*), giải quyết trực tiếp bài toán chi tiêu thông minh và thời trang bền vững.

## 2. Khảo sát hiện trạng các hệ thống tương đương và Đề xuất hệ thống

### 2.1. Khảo sát các hệ thống tương đương

* **Acloset:**
* *Ưu điểm:* Ứng dụng phổ biến, hỗ trợ AI nhận diện trang phục cơ bản, có tính năng gợi ý theo thời tiết và cộng đồng chia sẻ.
* *Hạn chế:* Giao diện còn rối rắm, quảng cáo nhiều; tính năng mua sắm mang tính liên kết bán hàng đại trà (Affiliate), chưa có thuật toán phân tích xem món đồ mới tối ưu hóa tủ đồ hiện có ở mức độ nào.
* **Whering:**
* *Ưu điểm:* Tập trung vào thời trang bền vững, giao diện lấy cảm hứng từ tủ đồ kỹ thuật số trực quan (tương tự phim *Clueless*).
* *Hạn chế:* Tốc độ và độ chính xác của AI bóc tách thuộc tính còn hạn chế, người dùng vẫn phải chỉnh sửa thủ công nhiều; thiếu cơ chế đo lường định lượng giá trị mua sắm mới.
* **Stylebook:**
* *Ưu điểm:* Quản lý chi tiết, có thống kê chi phí trên mỗi lần mặc (Cost-per-wear).
* *Hạn chế:* Chỉ có trên iOS, tính phí mua ứng dụng; 100% quy trình cắt ảnh và gắn nhãn là thủ công, không có AI hỗ trợ nên người dùng mất rất nhiều thời gian ban đầu.

### 2.2. Bảng so sánh tính năng

| **Tiêu chí** | **Stylebook** | **Whering** | **Acloset** | **Hệ thống đề xuất (CapsuleAI)** |
| --- | --- | --- | --- | --- |
| **Nền tảng** | iOS (Tính phí) | iOS / Android | iOS / Android | Cross-platform (iOS / Android) |
| **Tự động xóa nền & Gán nhãn** | Không (Thủ công) | Bán tự động | Có (Cơ bản) | **Có (AI tự động + User xác nhận)** |
| **Gợi ý phối đồ theo thời tiết** | Không | Cơ bản | Có | **Có (Thời tiết + Lý thuyết màu sắc)** |
| **Shuffle/Tùy biến từng món** | Thủ công | Có | Có | **Có (Hỗ trợ xáo trộn từng phần)** |
| **Phân tích khoảng trống (Gap Analysis)** | Không | Không | Không | **Có (Tính điểm Wardrobe Multiplier)** |
| **Định hướng giá trị** | Theo dõi tủ đồ | Tủ đồ bền vững | Mạng xã hội thời trang | **Tối ưu hóa tiện ích & Mua sắm thông minh** |

### 2.3. Hệ thống đề xuất

* **Mô hình tổng thể:** Một ứng dụng di động thông minh đóng vai trò "Trợ lý phong cách và Cố vấn mua sắm cá nhân".
* **Trụ cột kỹ thuật giải quyết bài toán:**
* **Module 1 (Digitalization):** Sử dụng các mô hình thị giác máy tính chuyên biệt để xóa nền và trích xuất đặc trưng trang phục với thời gian thực tế thấp.
* **Module 2 (Outfit Matching Engine):** Công cụ gợi ý kết hợp phương pháp dựa trên tập luật (Rule-based: bánh xe màu sắc, form dáng, thời tiết) và lọc dựa trên nội dung (Content-based filtering) dựa trên lịch sử tương tác của người dùng.
* **Module 3 (Strategic Recommendation Engine):** Thuật toán tính toán số tổ hợp outfit mới khả dĩ từ danh mục trang phục chuẩn (Wardrobe Capsule Staples), đưa ra các gợi ý mua sắm mang lại giá trị cao nhất.

## 3. Đối tượng và Phạm vi nghiên cứu

### 3.1. Đối tượng nghiên cứu

* **Về mặt kỹ thuật:**
* Các mô hình Computer Vision phục vụ tác vụ phân đoạn ảnh (Image Segmentation/Background Removal) như U2-Net/RMBG.
* Các mô hình phân loại thuộc tính thời trang đa nhãn (Multi-label Fashion Classification) hoặc mô hình Vision-Language (như CLIP zero-shot/fine-tuned) để nhận diện danh mục, màu sắc, họa tiết.
* Các giải thuật gợi ý (Recommendation Systems) và lý thuyết phối màu thời trang (Monochromatic, Complementary, Analogous).
* **Về mặt dữ liệu:**
* Bộ dữ liệu hình ảnh thời trang chuẩn hóa (thuộc tính quần áo, kiểu dáng).
* Dữ liệu thời tiết theo thời gian thực (nhiệt độ, điều kiện khí hậu).

### 3.2. Đối tượng sử dụng (Target Personas)

* **Nhóm người đi làm bận rộn (Indecisive Professionals):** Cần trang phục nhanh gọn, lịch sự, phù hợp thời tiết mỗi sáng mà không mất thời gian đắn đo.
* **Nhóm tối giản (Fashion-conscious Minimalists):** Hướng đến xây dựng tủ đồ sở hữu ít đồ nhưng tối đa hóa được số cách phối.
* **Nhóm mua sắm thông minh (Smart Shoppers):** Muốn kiểm soát chi tiêu, chỉ mua những món đồ thực sự bổ trợ và mở rộng công năng cho tủ quần áo hiện có.

### 3.3. Phạm vi nghiên cứu và Giới hạn đề tài

* **Phạm vi thực hiện (In-Scope):**
* Hỗ trợ các phân loại trang phục thường ngày cơ bản: Áo (Top), Quần/Váy (Bottom), Áo khoác (Outerwear), Giày dép (Footwear).
* Xử lý ảnh 2D tĩnh chụp từ camera điện thoại: Tách nền, trích xuất danh mục và bảng màu chủ đạo (Color Palette).
* Thuật toán ghép bộ trang phục (Outfit Generator) kết hợp 2–4 món đồ dựa trên ngữ cảnh như công việc, sở thích, thời tiết.
* Phân tích khoảng trống tủ đồ và tính toán chỉ số mở khóa outfit mới (Combo Multiplier).
* **Giới hạn không thực hiện trong Đồ án 2 (Out-of-Scope):**
* **Không làm Virtual Try-On (AR/3D):** Không dựng mô hình 3D hay thử đồ ảo lên người dùng vì vượt quá phạm vi tài nguyên tính toán và thời gian của Đồ án 2.
* **Không tích hợp cổng thanh toán trực tiếp:** Ứng dụng chỉ dừng ở mức gợi ý mua sắm và điều hướng qua liên kết ngoài (external link), không xây dựng sàn thương mại điện tử trọn gói.
* **Không đi sâu vào phụ kiện phức tạp:** Tạm thời chưa xử lý trang sức nhỏ, khăn, phụ kiện tóc, đồng hồ.

## 4. Mục tiêu đề tài

### 4.1. Yêu cầu chức năng hệ thống (Functional Requirements)

* **Phân hệ Quản lý tài khoản & Cá nhân hóa:**
* Đăng ký, đăng nhập, quản lý thông tin người dùng.
* Thiết lập hồ sơ phong cách (Style Preference), giới tính và quyền truy cập vị trí (Location).
* **Phân hệ Số hóa tủ đồ (AI Digital Closet):**
* Chụp ảnh trực tiếp hoặc tải ảnh từ thư viện thiết bị.
* Tự động xóa nền ảnh (Background Removal) và lưu trữ ảnh định dạng PNG trong suốt.
* Tự động trích xuất nhãn thuộc tính: Phân loại món đồ (Category), màu sắc chủ đạo (Primary Color), phân mùa (Season).
* Cho phép người dùng chỉnh sửa, bổ sung thông tin thuộc tính trước khi lưu vào cơ sở dữ liệu.
* **Phân hệ Gợi ý phối đồ (AI Outfit Generator):**
* Tự động sinh danh sách outfit (ít nhất 3 bộ) tương thích với thời tiết hiện tại mỗi khi mở ứng dụng.
* Tính năng **"Shuffle"**: Cho phép người dùng xáo trộn, thay đổi riêng lẻ từng thành phần trong outfit (ví dụ: giữ nguyên quần và áo khoác, chỉ đổi áo thun bên trong).
* Tính năng **"Wear This Today"**: Ghi nhận bộ trang phục được chọn mặc trong ngày để lưu vào nhật ký và huấn luyện hành vi người dùng.
* **Phân hệ Mua sắm chiến lược (Wardrobe Multiplier & Gap Analysis):**
* Đánh giá tỷ lệ hoàn thiện của tủ đồ hiện tại dựa trên bộ quy chuẩn trang phục nền tảng.
* Đề xuất các món đồ còn thiếu (Gaps) kèm theo giá ước lượng, độ bền chất liệu.
* Hiển thị chỉ số trực quan: Số lượng outfit mới được kích hoạt khi có thêm món đồ này.
* Trực quan hóa hình ảnh mô phỏng các outfit mới được ghép nối giữa món đồ đề xuất với quần áo có sẵn.

### 4.2. Yêu cầu dữ liệu (Data Requirements)

* **Dữ liệu trang phục cá nhân (Wardrobe Item Schema):**
* Ảnh gốc và ảnh đã tách phông nền.
* Metadata kỹ thuật: Mã định danh (id), phân loại chính (category), phân loại phụ (sub\_category), màu sắc (hex\_colors), mùa phù hợp (seasons), số lần đã mặc (wear\_count), ngày tạo.
* **Dữ liệu Outfit (Outfit Combination Schema):**
* Danh sách các item IDs cấu thành outfit (top\_id, bottom\_id, shoes\_id, outerwear\_id).
* Điểm số phù hợp (Compatibility Score) tính theo thuật toán.
* Nhật ký lịch sử mặc (Outfits Log) kèm trạng thái đánh giá (Like/Dislike).
* **Dữ liệu ngoại cảnh:**
* Dữ liệu thời tiết theo thời gian thực (Nhiệt độ, chỉ số UV, trạng thái mưa/nắng) lấy từ OpenWeatherMap API hoặc nguồn tương đương.
* **Dữ liệu danh mục chuẩn phục vụ gợi ý mua sắm:**
* Danh mục các món đồ cơ bản kinh điển (Capsule Staples: Áo phông trắng, quần jean xanh, giày sneaker trắng, áo blazer, v.v.) dùng làm tập dữ liệu mốc để chạy thuật toán Gap Analysis.

### 4.3. Yêu cầu giao diện, phần cứng, phần mềm

* **Yêu cầu giao diện (UI/UX):**
* Thiết kế theo phong cách tối giản (Minimalist), trực quan, tập trung làm nổi bật hình ảnh trang phục.
* Màn hình chụp ảnh có lưới canh chỉnh (Overlay guide) giúp người dùng chụp ảnh trang phục đúng góc độ chuẩn.
* Màn hình chính dạng Carousel/Card Swipe để lướt qua các bộ outfit gợi ý nhanh chóng.
* **Yêu cầu phần cứng:**
* *Phía Client (Thiết bị người dùng):* Điện thoại thông minh iOS (iOS 14+) hoặc Android (Android 10+), RAM tối thiểu 3GB, có camera ≥ 8MP và kết nối mạng ổn định.
* *Phía Server/Phát triển:* Máy chủ chạy backend và AI Inference (sử dụng GPU chuyên dụng hoặc các máy ảo Cloud có GPU hỗ trợ tăng tốc xử lý inference cho mô hình Computer Vision).
* **Yêu cầu phần mềm và Công nghệ dự kiến:**
* *Mobile App Framework:* React Native
* *Backend API:* Java (Spring boot).
* *AI/CV Service:* Python
* *Cơ sở dữ liệu:* MongoDB.
* *Lưu trữ tệp tin (Object Storage):* AWS S3

### 4.4. Yêu cầu phi chức năng (Non-functional Requirements)

* **Hiệu năng (Performance):**
* Thời gian xử lý tách nền và trích xuất thuộc tính ảnh không vượt quá **3 – 5 giây** trên ảnh chụp thông thường.
* Thời gian sinh và hiển thị danh sách gợi ý outfit dưới **3 giây**.
* Thời gian phản hồi API trung bình cho các thao tác đọc/ghi dữ liệu thông thường dưới **500ms**.
* **Độ chính xác (Accuracy):**
* Tỷ lệ nhận diện đúng phân loại chính (Category) và màu chủ đạo đạt tối thiểu **85%** đối với ảnh chụp có ánh sáng rõ ràng, vật thể không bị che khuất quá 20%.
* Thuật toán tách nền đảm bảo đường viền rõ nét, không làm mất các chi tiết đặc trưng của trang phục.
* **Tính khả dụng (Usability):**
* Thao tác số hóa 1 món đồ mới (chụp -> tách nền -> xác nhận tag) hoàn thành trong tối đa 3 lần chạm màn hình.
* Giao diện thân thiện, dễ hiểu đối với người dùng phổ thông, không đòi hỏi kiến thức chuyên môn về thời trang.
* **Bảo mật và Quyền riêng tư (Security & Privacy):**
* Ảnh cá nhân và dữ liệu tủ đồ của người dùng phải được phân quyền truy cập nghiêm ngặt (Access Control).
* Xác thực người dùng an toàn thông qua JWT (JSON Web Token) hoặc OAuth 2.0.
* Toàn bộ dữ liệu truyền tải giữa Client và Server được mã hóa qua HTTPS/TLS.
* **Khả năng mở rộng và Bảo trì (Maintainability & Scalability):**
  + Tách biệt độc lập giữa Core Backend xử lý nghiệp vụ và AI Service xử lý hình ảnh để dễ dàng nâng cấp mô hình AI trong tương lai mà không ảnh hưởng hệ thống chung.