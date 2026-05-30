# BlueMoon Fee Management System

> Phần mềm quản lý thu phí chung cư BlueMoon — Môn IT3180 Nhập môn CNPM  
> Đại học Bách Khoa Hà Nội — Nhóm 16

## Thành viên nhóm

| Họ tên | MSSV | Vai trò | GitHub |
|--------|------|---------|--------|
| Trần Khánh Linh | ... | Scrum Master | @linhch123-phe |
| Lê Quang Huy | ... | Product Owner | @DDNmajor |
| Trần Thị Nhật Linh | ... | Developer | @nhat-linh05 |
| Đoàn Văn Thắng | ... | Developer | @vanthang10tin |
| Đặng Hải Đăng | ... | Developer | @danghaidang04 |

## Mô tả dự án

Xây dựng phần mềm quản lý thu phí cho Ban quản trị chung cư BlueMoon, gồm các chức năng: quản lý hộ khẩu/nhân khẩu, tạo khoản thu, ghi nhận thanh toán và thống kê báo cáo.

**Nền tảng:** Spring Boot + Thymeleaf + Spring Data JPA + MySQL  
**Phương pháp:** Agile/Scrum (3 Sprint)

---

## Hướng dẫn cài đặt

### Yêu cầu cài đặt trước
- [JDK 17+](https://adoptium.net/)
- [MySQL 8+](https://dev.mysql.com/downloads/mysql/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (hoặc IDE tùy chọn)
- [Git](https://git-scm.com/)

### Bước 1 — Clone repository

```bash
git clone https://github.com/<org>/BlueMoon.git
cd BlueMoon
git checkout develop
```

### Bước 2 — Tạo database trong MySQL

Mở MySQL Workbench (hoặc terminal MySQL), chạy:

```sql
source <đường_dẫn_đến_repo>/database/bluemoon_schema.sql;
```

Sau đó import dữ liệu mẫu (nếu có file `database/data_sample.sql`):

```sql
source <đường_dẫn_đến_repo>/database/data_sample.sql;
```

### Bước 3 — Tạo file cấu hình

Copy file mẫu và điền thông tin của bạn:

```
src/main/resources/application.properties.example
          ↓  copy thành
src/main/resources/application.properties
```

Mở `application.properties` và chỉnh 3 chỗ sau:

```properties
# Mật khẩu MySQL trên máy bạn
spring.datasource.password=<mysql_password_của_bạn>

# Tài khoản admin sẽ được tạo tự động khi app khởi động lần đầu
app.admin.password=<đặt_mật_khẩu_admin_tùy_ý>
```

> `application.properties` đã được thêm vào `.gitignore` — **không commit file này lên git**.

### Bước 4 — Chạy ứng dụng

**Cách 1 — Terminal:**
```bash
./mvnw spring-boot:run          # macOS/Linux
mvnw.cmd spring-boot:run        # Windows
```

**Cách 2 — IntelliJ IDEA:**  
Mở project → tìm `BlueMoonApplication.java` → nhấn nút Run (▶)

### Bước 5 — Truy cập

Mở trình duyệt: **http://localhost:8080**

Đăng nhập bằng tài khoản admin vừa cấu hình ở Bước 3.

> **Lần đầu chạy:** App tự động tạo tài khoản admin trong DB với mật khẩu đã được mã hóa BCrypt.

---

## Branching Strategy

| Nhánh | Mục đích |
|-------|---------|
| `main` | Nhánh chính, chỉ merge khi hoàn thành Sprint |
| `develop` | Nhánh phát triển chung |
| `feature/[tên-chức-năng]` | Mỗi tính năng một nhánh riêng |
| `hotfix/[mô-tả]` | Sửa lỗi khẩn |

**Quy trình:** Tạo `feature/*` từ `develop` → làm xong → tạo Pull Request vào `develop`.

---

## Kế hoạch Sprint

| Sprint | Nội dung | Trạng thái |
|--------|----------|-----------|
| Sprint 0 | Khởi động, lập kế hoạch, thiết lập repo, tạo schema DB | ✅ Hoàn thành |
| Sprint 1 | Đăng nhập, phân quyền, quản lý khoản thu | 🔄 In Progress |
| Sprint 2 | Thu phí, quản lý hộ gia đình, nhân khẩu | 🔄 In Progress |
| Sprint 3 | Thống kê, báo cáo, kiểm thử tích hợp | Not Started |

---

## Cấu trúc source code

```
src/main/java/com/bluemoon/
├── controller/     # Spring MVC Controllers (7 controllers)
├── service/        # Business logic + CustomUserDetailsService
├── dao/            # Spring Data JPA Repositories (6 repositories)
├── model/          # JPA Entities (6 entities + VaiTro enum)
└── util/           # SecurityConfig, DataInitializer

src/main/resources/
├── templates/      # Thymeleaf HTML (16 trang, Bootstrap 5)
│   ├── fragments/  # Base layout (navbar, sidebar, alerts)
│   ├── auth/       # Trang đăng nhập
│   ├── dashboard.html
│   ├── nguoi-dung/
│   ├── loai-khoan-thu/
│   ├── khoan-thu/
│   ├── ho-gia-dinh/
│   ├── nhan-khau/
│   └── thanh-toan/
└── static/         # CSS, JS, images (frontend tự thêm)
```

---

## Phân công công việc còn lại

Skeleton toàn bộ layer (Model → DAO → Service → Controller → Template → Security) đã hoàn thành.  
Công việc còn lại là **hoàn thiện từng feature**: UI/UX, validation, business logic, và test.

| Người | Feature phụ trách | Công việc cụ thể | Branch |
|-------|------------------|-----------------|--------|
| **Trần Khánh Linh** (SM) | Layout + Authentication | Hoàn thiện UI login page, active state sidebar, trang lỗi 403/404, test đăng nhập/phân quyền | `feature/auth-ui` |
| **Lê Quang Huy** (PO) | Dashboard + Khoản thu | Dashboard: query thống kê thật (số hộ, tổng thu); Khoản thu: validation + UI | `feature/dashboard`, `feature/khoan-thu` |
| **Trần Thị Nhật Linh** | Người dùng + Loại khoản thu | CRUD hoàn chỉnh: validation form, thông báo lỗi, UI polish cho 2 module | `feature/nguoi-dung`, `feature/loai-khoan-thu` |
| **Đoàn Văn Thắng** | Hộ gia đình + Nhân khẩu | CRUD hoàn chỉnh, trang detail hộ hiển thị nhân khẩu + lịch sử thanh toán | `feature/ho-gia-dinh`, `feature/nhan-khau` |
| **Đặng Hải Đăng** | Thanh toán | Ghi nhận thanh toán, lịch sử theo hộ/theo khoản, validation chống thu trùng | `feature/thanh-toan` |

### Định nghĩa "hoàn thiện" cho mỗi feature
- [ ] Form có validation (trường bắt buộc, định dạng đúng)
- [ ] Hiển thị thông báo lỗi rõ ràng trên UI
- [ ] Không crash khi nhập sai dữ liệu
- [ ] Giao diện nhất quán với base layout
