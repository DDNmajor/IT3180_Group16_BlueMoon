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

Xây dựng phần mềm quản lý thu phí cho Ban quản trị chung cư BlueMoon, gồm: quản lý hộ khẩu/nhân khẩu, biến động cư trú, tạo khoản thu, ghi nhận thanh toán và thống kê báo cáo.

**Nền tảng:** Spring Boot 4.0.6 · Thymeleaf · Spring Data JPA · Spring Security · MySQL 8  
**Phương pháp:** Agile/Scrum (3 Sprint)

---

## Tính năng đã hoàn thành

| Module | Chức năng chính | Trạng thái |
|--------|----------------|-----------|
| **Xác thực** | Đăng nhập/đăng xuất, BCrypt, trang lỗi 403/404, xử lý tài khoản bị vô hiệu | ✅ |
| **Phân quyền** | ADMIN (toàn quyền), NHANVIEN (giới hạn), chặn tự đổi vai trò | ✅ |
| **Dashboard** | Thống kê: số hộ, nhân khẩu, khoản thu, tổng thu tháng hiện tại | ✅ |
| **Người dùng** | CRUD (admin only), BCrypt encode, đánh dấu đổi MK lần đầu, audit log | ✅ |
| **Loại khoản thu** | CRUD, validate trùng tên, phân loại bắt buộc/tự nguyện, inline errors | ✅ |
| **Khoản thu** | CRUD, mã duy nhất, filter tháng/loại/trạng thái, auto-tạo ThanhToan khi loại bắt buộc | ✅ |
| **Hộ gia đình** | CRUD, tầng/khu vực, chặn xoá khi còn nhân khẩu hoặc nợ phí, audit log | ✅ |
| **Nhân khẩu** | CRUD, validate CCCD (9/12 số), tình trạng cư trú, audit log | ✅ |
| **Biến động cư trú** | Ghi nhận Tạm trú/Tạm vắng/Chuyển đến/Chuyển đi, auto-cập nhật tình trạng | ✅ |
| **Thanh toán** | Ghi nhận, nộp thêm (CON_NO), hoàn tiền (DONG_DU), lịch sử theo hộ/khoản | ✅ |
| **Theo dõi thu phí** | Trạng thái từng căn hộ theo tháng/khoản thu | ✅ |
| **Tra cứu** | Tìm theo số căn hộ, tên chủ hộ, CCCD; trang detail đầy đủ | ✅ |

---

## Hướng dẫn cài đặt

### Yêu cầu

- [JDK 17+](https://adoptium.net/)
- [MySQL 8+](https://dev.mysql.com/downloads/mysql/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (hoặc IDE tuỳ chọn)
- [Git](https://git-scm.com/)

### Bước 1 — Clone repository

```bash
git clone https://github.com/<org>/BlueMoon.git
cd BlueMoon
git checkout develop
```

### Bước 2 — Khởi tạo database

Mở MySQL Workbench hoặc terminal và chạy **theo thứ tự**:

```sql
source database/bluemoon_schema.sql;
source database/migration_v2_payment_fields.sql;
source database/migration_v3_khoan_thu_enhancements.sql;
source database/migration_v4_auth_enhancements.sql;
source database/migration_v5_doi_mat_khau.sql;
source database/migration_v6_ho_nhan_khau.sql;
```

> Nếu bắt đầu từ DB trống, chạy `bluemoon_schema.sql` trước rồi mới chạy các migration theo thứ tự.

### Bước 3 — Tạo file cấu hình

```
src/main/resources/application.properties.example
          ↓  copy thành
src/main/resources/application.properties
```

Chỉnh các giá trị sau:

```properties
spring.datasource.password=<mysql_password_của_bạn>
app.admin.password=<đặt_mật_khẩu_admin_tuỳ_ý>
```

> `application.properties` đã có trong `.gitignore` — **không commit file này**.

### Bước 4 — Chạy ứng dụng

```bash
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run        # macOS/Linux
```

Hoặc mở IntelliJ → chạy `BlueMoonApplication.java`.

### Bước 5 — Truy cập

Mở trình duyệt: **http://localhost:8080**

Đăng nhập bằng tài khoản `admin` với mật khẩu đã cấu hình.  
App tự tạo tài khoản admin (BCrypt) khi khởi động lần đầu nếu chưa có.

---

## Cấu trúc source code

```
src/main/java/com/bluemoon/
├── controller/          # 10 Spring MVC Controllers
│   ├── HomeController           # / → /dashboard (thống kê)
│   ├── AuthController           # /login
│   ├── ErrorPageController      # /error/403, /error/404
│   ├── NguoiDungController      # /nguoi-dung/** (admin only)
│   ├── HoGiaDinhController      # /ho-gia-dinh/**
│   ├── NhanKhauController       # /nhan-khau/** + biến động
│   ├── KhoanThuController       # /khoan-thu/**
│   ├── LoaiKhoanThuController   # /loai-khoan-thu/**
│   └── ThanhToanController      # /thanh-toan/**
│
├── service/             # 8 Service classes
│   ├── CustomUserDetailsService # Spring Security user loading
│   ├── NguoiDungService         # BCrypt encode, audit log, self-role guard
│   ├── HoGiaDinhService         # Delete constraint, CCCD search, audit log
│   ├── NhanKhauService          # CCCD uniqueness, audit log, saveRaw
│   ├── BienDongService          # Ghi biến động + auto-update tinhTrang
│   ├── KhoanThuService          # Auto-apply bắt buộc, audit log
│   ├── LoaiKhoanThuService
│   └── ThanhToanService         # nopThem, baoDaHoanTien, daDongHoanTat
│
├── dao/                 # 7 Spring Data JPA Repositories
│   ├── NguoiDungRepository
│   ├── HoGiaDinhRepository      # findByCccdNhanKhau (JPQL join)
│   ├── NhanKhauRepository
│   ├── BienDongRepository
│   ├── KhoanThuRepository
│   ├── LoaiKhoanThuRepository
│   └── ThanhToanRepository
│
├── model/               # 7 JPA Entities + 6 Enums
│   ├── Entities: NguoiDung, HoGiaDinh, NhanKhau, BienDong
│   │            KhoanThu, LoaiKhoanThu, ThanhToan
│   └── Enums:   VaiTro, LoaiApDung, TinhTrangCuTru, LoaiBienDong
│                TrangThaiThanhToan, PhuongThucThanhToan
│
└── util/
    ├── SecurityConfig           # BCrypt bean, route rules, form login
    └── DataInitializer          # Auto-tạo admin khi lần đầu chạy

src/main/resources/
├── templates/           # 20 Thymeleaf HTML templates (Bootstrap 5)
│   ├── fragments/layout.html    # head, navbar, sidebar, alerts, scripts
│   ├── auth/login.html
│   ├── error/{403,404}.html
│   ├── dashboard.html
│   ├── nguoi-dung/{list,form}.html
│   ├── ho-gia-dinh/{list,form,detail}.html
│   ├── nhan-khau/{list,form,bien-dong-form}.html
│   ├── khoan-thu/{list,form}.html
│   ├── loai-khoan-thu/{list,form}.html
│   └── thanh-toan/{list,form,theo-doi}.html
└── static/              # CSS, JS, images

database/
├── bluemoon_schema.sql                    # Schema khởi tạo (v1)
├── migration_v2_payment_fields.sql        # Bổ sung trường thanh toán
├── migration_v3_khoan_thu_enhancements.sql
├── migration_v4_auth_enhancements.sql     # Thêm cột active
├── migration_v5_doi_mat_khau.sql          # Thêm doi_mat_khau_lan_dau
└── migration_v6_ho_nhan_khau.sql          # tang_khu_vuc, tinh_trang, bảng bien_dong
```

---

## Phân quyền

| Vai trò | Quyền truy cập |
|---------|---------------|
| `admin` | Toàn bộ, bao gồm `/nguoi-dung/**` |
| `nhanvien` | Dashboard, hộ gia đình, nhân khẩu, khoản thu, thanh toán |

Không thể tự đổi vai trò hoặc xoá tài khoản đang đăng nhập.

---

## Branching Strategy

| Nhánh | Mục đích |
|-------|---------|
| `main` | Nhánh chính — chỉ merge khi hoàn thành Sprint |
| `develop` | Nhánh tích hợp chung |
| `feature/[tên]` | Mỗi tính năng một nhánh riêng |
| `hotfix/[mô-tả]` | Sửa lỗi khẩn |

**Quy trình:** tạo `feature/*` từ `develop` → Pull Request vào `develop` → merge vào `main` khi sprint done.

---

## Kế hoạch Sprint

| Sprint | Nội dung | Trạng thái |
|--------|----------|-----------|
| Sprint 0 | Khởi động, lập kế hoạch, thiết lập repo, schema DB | ✅ Hoàn thành |
| Sprint 1 | Đăng nhập/phân quyền, khoản thu, loại khoản thu, dashboard | ✅ Hoàn thành |
| Sprint 2 | Hộ gia đình, nhân khẩu, biến động, thanh toán, theo dõi thu phí | ✅ Hoàn thành |
| Sprint 3 | Thống kê nâng cao, báo cáo, kiểm thử tích hợp | 🔄 In Progress |
