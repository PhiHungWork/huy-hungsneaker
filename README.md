# 👟 Dự Án Hệ Thống Thương Mại Điện Tử - Huy & Hưng Sneaker

**Huy & Hưng Sneaker** là một hệ thống thương mại điện tử chuyên cung cấp giày thể thao kết hợp nền tảng quản lý doanh thu (Revenue Management) chuyên nghiệp. Dự án được xây dựng dựa trên kiến trúc Monolithic với Spring Boot (Java), phân rã luồng xử lý rõ ràng giữa Khách hàng (Storefront) và Quản trị viên (Admin Dashboard).

---

## 📂 1. Chi Tiết Cấu Trúc File & Thư Mục (Project Structure)

Dự án được tổ chức theo mô hình MVC (Model - View - Controller), đảm bảo nguyên lý thiết kế Clean Code và tách biệt logic nghiệp vụ khỏi giao diện:

- **`src/main/java/com/example/QuanLiThuChi/controller/` (Controllers):**
  - **Luồng Public (Khách hàng):** `CustomerProductController` (Hiển thị sản phẩm), `CartController` (Giỏ hàng), `CustomerOrderController` (Thanh toán).
  - **Luồng Protected (Admin):** `ProductAdminController`, `OrderAdminController`, `AdminAccountController`, `AdminController`.
  - **Báo cáo (Report):** `ReportController` xử lý luồng xuất file Excel.

- **`src/main/java/com/example/QuanLiThuChi/service/` (Services):**
  - Đóng gói toàn bộ logic nghiệp vụ (Business Logic). 
  - Đáng chú ý nhất là `NotificationServiceImpl` xử lý luồng gửi email xác nhận MIME (HTML).

- **`src/main/resources/templates/` (Views - Thymeleaf):**
  - `storefront/`: Các view dành cho khách hàng như `public-products.html`, `cart.html`, `checkout.html`.
  - `admin/`: Các view phục vụ luồng quản trị viên (Dashboard, bảng sản phẩm, quản lý đơn hàng).
  - `email/`: Các file mẫu email (`order-notification.html`) dùng cho gửi thông báo động thay vì plain-text thông thường.

- **Thành phần hạ tầng (Infrastructure):**
  - `application.properties`: Nơi cấu hình toàn bộ biến môi trường (PostgreSQL, JavaMailSender, Cloudinary, Render Config).
  - `data.sql`: Script tự động khởi tạo dữ liệu mẫu (Sản phẩm, User, Category) khi ứng dụng chạy lần đầu.
  - `Dockerfile` & `pom.xml`: Phục vụ tự động hóa việc đóng gói và triển khai ứng dụng (CI/CD) lên nền tảng Cloud (Render).

---

## 🚀 2. Hướng Dẫn Chạy Dự Án (How to Run)

### Yêu Cầu Hệ Thống (Prerequisites)
- **Java Development Kit (JDK):** Phiên bản 17 (hoặc mới hơn).
- **Maven:** Trình quản lý dependency.
- **Database:** PostgreSQL (Khuyến nghị version 14+).

### Các Bước Cài Đặt (Setup Steps)

1. **Khởi tạo Database (PostgreSQL):**
   - Mở PostgreSQL (pgAdmin hoặc DBeaver), tạo một cơ sở dữ liệu có tên `HuyvaHungstore`.
   
2. **Cấu hình môi trường (Environment Configuration):**
   Mở file `src/main/resources/application.properties` và đảm bảo/cập nhật thông tin cấu hình (nếu cần đổi sang Local PostgreSQL của bạn):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/HuyvaHungstore
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```
   > **Lưu ý:** Spring Boot đã được cấu hình với `spring.jpa.hibernate.ddl-auto=update` và sẽ tự động nạp dữ liệu mẫu từ `data.sql` khi bạn chạy.

3. **Chạy ứng dụng (Running the Application):**
   Tại thư mục gốc của dự án (`QuanLiThuChi/QuanLiThuChi`), mở terminal và chạy lệnh:
   ```bash
   mvn spring-boot:run
   ```
   Hoặc chạy file `QuanLiThuChiApplication.java` từ trong IDE (IntelliJ, Eclipse, VSCode).

4. **Trải nghiệm Hệ Thống:**
   - **Giao diện Khách hàng (Storefront):** Truy cập http://localhost:8080/
   - **Giao diện Quản trị viên (Admin):** Truy cập http://localhost:8080/admin/dashboard
     - *Tài khoản mặc định (tạo sẵn trong `data.sql`):* 
     - **Tài khoản:** `admin`
     - **Mật khẩu:** `123456`

---

## 🔄 3. Luồng Dữ Liệu Xử Lý (Data Processing Flow)

Hệ thống hoạt động với 2 luồng xử lý độc lập nhưng liên kết chặt chẽ về dữ liệu:

1. **Luồng Khách Hàng (Customer User Journey):**
   - **Trải nghiệm Mua Sắm (Browsing):** Người dùng truy cập trang chủ (Public). Hệ thống load sản phẩm từ CSDL và render thông qua `CustomerProductController` với thiết kế giao diện SEO-Optimized và Responsive.
   - **Thêm vào Giỏ (Cart):** Giỏ hàng được quản lý linh hoạt, tránh load lại trang toàn phần.
   - **Thanh toán (Checkout):** Khách hàng điền thông tin và thanh toán. `CustomerOrderController` tiếp nhận -> validate thông tin -> lưu đơn hàng vào PostgreSQL -> Gọi Asynchronous Thread để gửi email (MIME/HTML) cho khách hàng -> Render trang Success.

2. **Luồng Quản Trị Viên (Admin Management Journey):**
   - **Xác Thực (Authentication):** Quản trị viên đăng nhập qua form. Hệ thống xác thực bằng Spring Security (Mật khẩu được mã hóa băm BCrypt trong CSDL).
   - **Quản lý Vận hành (Operations):** Admin thêm/sửa/xóa (CRUD) sản phẩm (`ProductAdminController`), quản lý trạng thái đơn hàng (Đang xử lý -> Đã giao). Việc upload ảnh được đẩy trực tiếp lên Cloudinary thay vì lưu ổ cứng.
   - **Xuất Báo cáo (Reporting):** Admin bấm nút xuất báo cáo. `ReportController` truy xuất tổng doanh thu -> Đẩy cho Apache POI sinh In-Memory file Excel -> Gửi file Stream xuống trình duyệt của admin.

---

## ⭐ 4. Các Điểm Cộng & Chiều Sâu Kỹ Thuật (Highlight Features & Technical Depth)

> Dự án này không đơn thuần chỉ là thao tác CRUD (Create, Read, Update, Delete) cơ bản, mà đã áp dụng các kiến trúc và kỹ thuật tiêu chuẩn thực tế (Production-Ready) của Doanh nghiệp.

### 🎨 4.1. Về Giao diện (Front-End)
- **Giao diện SEO-Optimized & Responsive:** Trang chủ/Storefront được thiết kế tuân theo cấu trúc Landing Page 11-section tiêu chuẩn.
- **Thiết kế UI/UX Hiện Đại:** Ứng dụng hệ màu HSL linh hoạt kết hợp với các micro-animations (hiệu ứng tinh tế khi di chuột, cuộn trang), đem lại cảm giác cao cấp và chuyên nghiệp như các trang thương mại điện tử lớn.

### ⚙️ 4.2. Về Kỹ thuật & Kiến trúc (Back-End)
- **Lựa chọn PostgreSQL thay vì MySQL (Phân tích & So sánh Kỹ thuật):**
  Việc sử dụng PostgreSQL thay vì MySQL là một quyết định kiến trúc có chủ đích dựa trên đặc thù của hệ thống thương mại điện tử:
  - **Xử lý Giao dịch Đồng thời (Concurrency):** PostgreSQL nổi bật với cơ chế **MVCC (Multiversion Concurrency Control)** tiên tiến. Trong môi trường E-commerce, khi nhiều khách hàng cùng lúc nhấn "Thanh toán" hoặc thêm vào giỏ hàng, MVCC giúp các luồng đọc/ghi không bị khóa (locks) lẫn nhau. Điều này vượt trội hơn so với MySQL (thường gặp thắt cổ chai hoặc deadlock ở các query phức tạp), đảm bảo số lượng hàng tồn kho (stock) luôn được cập nhật chính xác và an toàn tuyệt đối dưới tải cao.
  - **Tính Toàn vẹn Dữ liệu (Strict Data Integrity):** Quản lý doanh thu và giá tiền yêu cầu độ chính xác tuyệt đối. PostgreSQL tuân thủ cực kỳ nghiêm ngặt các tiêu chuẩn SQL (Strict SQL Standard), nó sẽ chủ động ném lỗi (throw error) nếu dữ liệu không hợp lệ. Trong khi đó, MySQL có xu hướng tự động cắt gọt (truncate) hoặc ép kiểu ngầm định (implicit casting), dễ dẫn đến sai số ngầm trong các báo cáo tài chính của Admin.
  - **Hỗ trợ Cloud Deployment Nguyên bản (Native Cloud-Ready):** Nền tảng Render (hệ sinh thái được chọn để deploy dự án) cung cấp Managed PostgreSQL Server chất lượng cao và tương thích hoàn toàn. Việc dùng PostgreSQL giúp quá trình CI/CD trở nên mượt mà, loại bỏ mọi rủi ro về cấu hình Network/Storage so với việc phải tự cấu hình một Database MySQL rời rạc.

- **Lưu Trữ Ảnh với Cloudinary API (Image Hosting):** 
  Khắc phục triệt để bài toán mất ảnh mỗi lần ứng dụng khởi động lại khi triển khai trên nền tảng Cloud (Ephemeral Storage). Khi Admin tạo sản phẩm, ảnh được mã hóa Multipart và upload lên CDN Cloudinary; Server chỉ cần lưu chuỗi URL, giúp tăng tốc độ tải trang.

- **Xử lý Bất Đồng Bộ (Asynchronous Processing) & MIME Email:**
  Việc gửi email xác nhận qua SMTP của Google có độ trễ lớn. Hệ thống đã tách luồng gửi mail ra khỏi thread chính (Main Thread) giúp khách hàng sau khi bấm "Thanh Toán" không phải chịu đựng tình trạng xoay vòng loading dài (tránh Time-out). Email sử dụng Thymeleaf HTML Template (không phải plain-text) tạo sự chuyên nghiệp tuyệt đối.

- **Dynamic In-Memory Excel Report Generation (Apache POI):**
  Khi xuất file báo cáo tổng hợp, thay vì ứng dụng phải lưu file Excel tạm xuống ổ cứng (gây rác bộ nhớ server, tốn I/O), hệ thống dùng Apache POI vẽ trực tiếp byte data của Excel và stream (tuôn) thẳng qua `HttpServletResponse` dưới dạng `.xlsx` để Admin tải ngay lập tức về máy.

- **Bảo Mật Role-Based (Phân quyền):**
  Áp dụng mô hình Interceptor/Security Filter chia tách rõ ràng:
  - **Public APIs**: Ai cũng có thể vào mua hàng.
  - **Protected APIs**: Chỉ có tài khoản role Admin (có BCrypt Password Hash) mới được truy cập Dashboard và các Endpoints báo cáo doanh thu.
