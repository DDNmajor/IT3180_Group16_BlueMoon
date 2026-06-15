# BlueMoon Fee Management System

> Phần mềm quản lý thu phí chung cư BlueMoon — Môn IT3180 Nhập môn CNPM  
> Đại học Bách Khoa Hà Nội — Nhóm 16

## Thành viên nhóm

| Họ tên | MSSV | Vai trò | GitHub |
|--------|------|---------|--------|
| Lê Quang Huy | ... | Product Owner · Leader | @DDNmajor |
| Trần Khánh Linh | ... | Scrum Master | @linhch123-phe |
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
| **Phân quyền** | `admin` (toàn quyền), `staff` (giới hạn), chặn tự đổi vai trò/xoá chính mình | ✅ |
| **Dashboard** | Thống kê: số hộ, nhân khẩu, khoản thu, tổng thu tháng hiện tại | ✅ |
| **Người dùng** | CRUD (admin only), BCrypt encode, cờ đổi MK lần đầu, audit log | ✅ |
| **Loại khoản thu** | CRUD, validate trùng tên, phân loại bắt buộc định kỳ / đột xuất / tự nguyện | ✅ |
| **Mẫu thu định kỳ** | CRUD, tự động tạo `KhoanThu` lúc 8:00 ngày 28 hàng tháng, toggle bật/tắt, tạo thủ công theo kỳ, gửi email thông báo | ✅ |
| **Khoản thu** | CRUD, mã duy nhất, phí theo diện tích (`donGiaPerM2`), filter tháng/loại/trạng thái, auto-apply cho hộ bắt buộc | ✅ |
| **Hộ gia đình** | CRUD, diện tích, tầng/khu vực, email chủ hộ, chặn xoá khi còn nhân khẩu hoặc nợ phí | ✅ |
| **Nhân khẩu** | CRUD, validate CCCD (9/12 số), tình trạng cư trú, tìm kiếm theo CCCD | ✅ |
| **Biến động cư trú** | Ghi nhận Tạm trú/Tạm vắng/Chuyển đến/Chuyển đi, auto-update tình trạng, scheduled job xử lý hết hạn | ✅ |
| **Thanh toán** | Ghi nhận, nộp thêm, hoàn tiền, filter đa chiều (hộ / khoản / trạng thái / tìm kiếm) | ✅ |
| **Thu hộ** | Hóa đơn điện/nước/internet/gas (`HoaDonThuHo` — `/thu-ho`): tạo, gen hàng loạt, email, xác nhận, hủy/khôi phục | ✅ |
| **Theo dõi thu phí** | Bảng cross-tab: tất cả hộ vs trạng thái đóng phí theo tháng/khoản | ✅ |
| **Tra cứu nợ phí** | Danh sách hộ còn nợ, filter theo kỳ thu và khoản thu, hiển thị số tiền còn thiếu | ✅ |
| **Thống kê thu phí** | Tổng phải thu / đã thu / còn nợ, số hộ đóng/chưa đóng, filter theo khoản và kỳ thu | ✅ |
| **Xuất báo cáo Excel** | Xuất file `.xlsx` chi tiết và tổng hợp, hỗ trợ filter trạng thái | ✅ |
| **Import Excel hộ gia đình** | Upload file Excel để nhập hàng loạt hộ gia đình và nhân khẩu, validate từng dòng | ✅ |
| **Phí gửi xe** | Quản lý phương tiện của hộ (xe máy/ô tô), tính phí theo loại xe | ✅ |
| **Lịch sử email** | Ghi log toàn bộ email đã gửi, filter theo loại/trạng thái/căn hộ, xem chi tiết nội dung | ✅ |
| **Thùng rác** | Khôi phục hộ gia đình và khoản thu đã xoá mềm (soft delete) | ✅ |
| **Nhật ký hoạt động** | Ghi audit log toàn hệ thống, filter theo loại đối tượng / người dùng / khoảng ngày (admin only) | ✅ |
| **Email thông báo** | Gửi email @Async qua Brevo SMTP khi khoản thu mới được tạo, nhắc nợ tự động và thủ công | ✅ |
| **Giao diện** | Light/Dark mode, sticky navbar + sidebar, glassmorphism (light), starfield (dark) | ✅ |

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
```

### Bước 2 — Khởi tạo database

Mở MySQL Workbench hoặc terminal và chạy:

```sql
source database/bluemoon_schema.sql;
```

> File này tạo database, toàn bộ bảng và index. Chạy lại bất cứ lúc nào để reset về trạng thái ban đầu.

Nếu cần dữ liệu mẫu nhỏ để test nhanh:

```sql
source database/mock_data.sql;
```

Nếu cần bộ dữ liệu demo đầy đủ (50 hộ, lịch sử thanh toán, hóa đơn thu hộ):

```sql
source database/example_data.sql;
```

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
├── controller/          # 16 Spring MVC Controllers
│   ├── HomeController               # / → /dashboard (thống kê)
│   ├── AuthController               # /login
│   ├── ErrorPageController          # /error/403, /error/404
│   ├── NguoiDungController          # /nguoi-dung/** (admin only)
│   ├── HoGiaDinhController          # /ho-gia-dinh/** + import Excel
│   ├── NhanKhauController           # /nhan-khau/** + biến động
│   ├── KhoanThuController           # /khoan-thu/**
│   ├── LoaiKhoanThuController       # /loai-khoan-thu/**
│   ├── MauKhoanThuController        # /mau-khoan-thu/** + toggle + tạo kỳ thủ công
│   ├── ThanhToanController          # /thanh-toan/** + theo-doi + nop-them/hoan-tien/xoa
│   ├── ThanhToanBaoCaoController    # /thanh-toan/thong-ke, /no-phi, /thong-ke/export, email-nhac-no
│   ├── HoaDonThuHoController        # /thu-ho/** (hóa đơn thu hộ)
│   ├── PhuongTienController         # /ho-gia-dinh/{idHo}/phuong-tien/** (phí gửi xe)
│   ├── LichSuEmailController        # /lich-su-email/**
│   ├── ThungRacController           # /thung-rac/** (khôi phục soft delete)
│   └── AuditLogController           # /audit-log/** (admin only, 2 tab)
│
├── config/              # GenPortFilter, InternalPortConfig (cổng nội bộ 8081 cho /thu-ho/gen)
│
├── service/             # 19 Service classes
│   ├── CustomUserDetailsService     # Spring Security user loading
│   ├── NguoiDungService             # BCrypt encode, audit log, self-role guard
│   ├── HoGiaDinhService             # Delete constraint, CCCD search, audit log
│   ├── NhanKhauService              # CCCD uniqueness, audit log
│   ├── BienDongService              # Ghi biến động + auto-update + scheduled hết hạn
│   ├── KhoanThuService              # Auto-apply bắt buộc, phí diện tích, audit log
│   ├── LoaiKhoanThuService
│   ├── MauKhoanThuService           # Scheduled job ngày 28, startup check, toggle active
│   ├── ThanhToanService             # nopThem, baoDaHoanTien, delete/reset
│   ├── BaoCaoThanhToanService       # Tổng hợp thống kê thu phí
│   ├── ExcelExportService           # Xuất báo cáo .xlsx (Apache POI)
│   ├── ExcelImportService           # Import hộ/nhân khẩu từ file .xlsx
│   ├── PhuongTienService            # CRUD phương tiện, tính phí gửi xe
│   ├── LichSuEmailService           # Query + filter lịch sử email
│   ├── EmailService                 # Gửi email @Async qua Brevo SMTP
│   ├── EmailSchedulerService        # Scheduled job nhắc nợ tự động
│   ├── HoaDonThuHoService           # CRUD hóa đơn thu hộ + gen/email/xác nhận
│   ├── SimulationDataService        # Sinh số tiền mô phỏng khi gen hàng loạt
│   └── AuditLogService              # Ghi log vào DB + SLF4J
│
├── dao/                 # 12 Spring Data JPA Repositories
│   ├── NguoiDungRepository
│   ├── HoGiaDinhRepository          # findByCccdNhanKhau (JPQL join)
│   ├── NhanKhauRepository
│   ├── BienDongRepository
│   ├── KhoanThuRepository           # existsByMauKhoanThuIdAndKyThu (idempotent check)
│   ├── LoaiKhoanThuRepository
│   ├── MauKhoanThuRepository
│   ├── ThanhToanRepository          # sumSoTienDaNopThangNay, aggregate queries
│   ├── PhuongTienRepository
│   ├── HoaDonThuHoRepository
│   ├── LichSuEmailRepository
│   └── AuditLogRepository           # findWithFilter (dynamic JPQL), findDistinctNguoiDung
│
├── model/               # 12 JPA Entities + 11 Enums
│   ├── Entities: NguoiDung, HoGiaDinh, NhanKhau, BienDong
│   │            LoaiKhoanThu, KhoanThu, MauKhoanThu, ThanhToan
│   │            PhuongTien, HoaDonThuHo, LichSuEmail, AuditLog
│   └── Enums:   VaiTro, LoaiApDung, TinhTrangCuTru, LoaiBienDong
│                TrangThaiThanhToan, PhuongThucThanhToan
│                LoaiXe, LoaiEmail, LoaiTinhPhi
│                LoaiDichVuThuHo, TrangThaiHoaDonThuHo
│
└── util/
    ├── SecurityConfig               # BCrypt bean, @EnableAsync, route rules, form login
    └── DataInitializer              # ApplicationRunner — auto-tạo admin khi lần đầu chạy

src/main/resources/
├── templates/           # 32 Thymeleaf HTML templates (Bootstrap 5.3.2)
│   ├── fragments/layout.html        # head, navbar (sticky), sidebar (sticky), alerts, scripts
│   ├── auth/login.html
│   ├── error/{403,404}.html
│   ├── dashboard.html
│   ├── nguoi-dung/{list,form}.html
│   ├── ho-gia-dinh/{list,form,detail}.html
│   ├── nhan-khau/{list,form,bien-dong-form}.html
│   ├── loai-khoan-thu/{list,form}.html
│   ├── khoan-thu/{list,form}.html
│   ├── mau-khoan-thu/{list,form}.html
│   ├── phuong-tien/form.html
│   ├── thanh-toan/{list,form,theo-doi,no-phi,thong-ke,email-nhac-no}.html
│   ├── thu-ho/{list,form,gen}.html
│   ├── lich-su-email/list.html
│   ├── thung-rac/index.html
│   └── audit-log/list.html
├── static/css/
│   ├── base-theme.css               # Layout, typography, sticky navbar/sidebar
│   ├── moona-theme.css              # Dark mode (Moonlit Observatory palette)
│   └── kanata-theme.css             # Light mode (Soft Purple Glassmorphism)
└── static/

database/
├── bluemoon_schema.sql              # Schema đầy đủ (12 bảng, gồm hoa_don_thu_ho + lich_su_email)
├── mock_data.sql                    # Dữ liệu mẫu nhỏ (3 hộ, 2 khoản thu, 4 thanh toán)
└── example_data.sql                 # Dữ liệu demo lớn (50 hộ, lịch sử thanh toán, hóa đơn thu hộ)
```

---

## Phân quyền

| Vai trò | Quyền truy cập |
|---------|---------------|
| `admin` | Toàn bộ, bao gồm `/nguoi-dung/**`, `/audit-log/**` và `/thung-rac/**` |
| `staff` | Dashboard, hộ gia đình, nhân khẩu, khoản thu, mẫu thu, thanh toán, thu hộ, phí gửi xe, báo cáo |

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
| Sprint 3 | Thống kê nâng cao, báo cáo Excel, phí gửi xe, import hộ, tra cứu nợ, email log | ✅ Hoàn thành |
| Sprint 4 | v2.0 — Phí gửi xe PER_XE, module thu hộ `HoaDonThuHo`, refactor UI sidebar, email nhắc nợ tự động, thùng rác, lịch sử email | ✅ Hoàn thành |

---

## Tổng quan kỹ thuật

### Kiến trúc phân lớp

```
Browser (Bootstrap 5.3.2)
    ↓ HTTP
Controller (16 classes)   — nhận request, validate input, gọi Service, đưa Model vào Template
    ↓
Service (19 classes)      — business logic, @Transactional, gọi Repository + EmailService
    ↓
DAO / Repository (12)     — Spring Data JPA, custom @Query, JpaSpecificationExecutor
    ↓
MySQL 8 (12 bảng)         — schema quản lý bằng SQL thuần, ddl-auto=validate
```

Thymeleaf render HTML server-side. Tất cả email gửi `@Async` qua Brevo SMTP, mỗi lần gửi đều ghi vào `lich_su_email`.

---

### Chế độ tính phí (LoaiTinhPhi)

| Giá trị | Ý nghĩa | Trường dùng |
|---------|---------|------------|
| `FIXED` | Số tiền cố định cho mọi hộ | `soTien` |
| `PER_M2` | Đơn giá × diện tích căn hộ | `donGiaPerM2` × `hoGiaDinh.dienTich` |
| `PER_XE` | Đơn giá × số xe mỗi loại | `giaXeMay` × xe máy + `giaOto` × ô tô |
| `PER_PERSON` | Đơn giá × số nhân khẩu thường trú | `soTien` × countNhanKhau(≠ CHUYEN_DI) |

> Thu hộ điện/nước/internet/gas **không** dùng `LoaiTinhPhi` — quản lý riêng qua entity `HoaDonThuHo` tại `/thu-ho`.

---

### Scheduled Jobs

| Cron | Service | Việc làm |
|------|---------|---------|
| `0 0 8 28 * *` | `MauKhoanThuService` | Tạo `KhoanThu` tháng tới từ tất cả mẫu đang bật |
| `0 0 1 * * *` | `BienDongService` | Xử lý biến động hết hạn (TAM_TRU/TAM_VANG → THUONG_TRU/CHUYEN_DI) |
| `0 0 8 * * MON` | `EmailSchedulerService` | Gửi email nhắc tất cả hộ còn nợ (thứ Hai hàng tuần) |
| `0 0 8 * * *` | `EmailSchedulerService` | Gửi email nhắc hộ có phí đến hạn trong 1–3 ngày |
| `0 0 9 * * *` | `EmailSchedulerService` | Gửi email nhắc hộ vừa quá hạn ngày hôm trước |

Ngoài ra, `MauKhoanThuService` và `BienDongService` đều có `@EventListener(ApplicationReadyEvent)` để back-fill dữ liệu ngay khi ứng dụng khởi động.

---

### Hệ thống Email

| Loại (`LoaiEmail`) | Khi nào gửi | Sync/Async |
|--------------------|------------|-----------|
| `THONG_BAO_KHOAN_THU` | Khoản thu mới được áp dụng cho hộ | `@Async` |
| `CHAO_MUNG_HO_MOI` | Hộ mới được tạo, liệt kê phí bắt buộc đang áp dụng | `@Async` |
| `NHAC_NO_TU_DONG` | Gửi theo lịch (3 scheduled jobs) | `@Async` |
| `NHAC_NO_THU_CONG` | Staff gửi thủ công từ trang tra cứu nợ | Sync |
| `THU_HO_THONG_BAO` | Thông báo hóa đơn thu hộ (điện/nước/internet/gas) | `@Async` |
| `THU_HO_XAC_NHAN` | Biên nhận xác nhận đã thu hóa đơn thu hộ | `@Async` |

Mọi lần gửi (thành công hay thất bại) đều được lưu vào `lich_su_email`. Xem lại tại `/audit-log?tab=email` hoặc `/lich-su-email`.

---

### Luồng nghiệp vụ chính

**Tạo hộ gia đình mới**
1. Lưu hộ → `autoApplyForNewHo()` áp tất cả khoản thu bắt buộc đang có → tạo `ThanhToan` CON_NO cho mỗi khoản → gửi email chào mừng

**Tạo khoản thu bắt buộc mới**
1. Lưu khoản → `autoApplyNeuBatBuoc()` tạo `ThanhToan` CON_NO cho mọi hộ → tính `soTienYeuCau` theo `loaiTinhPhi` → gửi email thông báo cho từng hộ

**Tự động tạo khoản thu định kỳ (ngày 28)**
1. Scheduler kiểm tra từng mẫu đang `active` → nếu chưa có khoản cho tháng tới thì tạo → gọi `KhoanThuService.save()` → kích hoạt auto-apply

**Ghi nhận thanh toán**
1. Kiểm tra đã có `CON_NO` chưa → nếu có: `nopThem()` cộng dồn; nếu chưa: tạo mới → tính lại `trangThai` (DA_DONG / CON_NO / DONG_DU)

**Soft delete & Thùng rác**
- `@SQLRestriction("deleted_at IS NULL")` trên `HoGiaDinh` và `KhoanThu` — JPA tự lọc, không cần sửa query
- Thùng rác dùng native SQL bypass restriction để restore hoặc xóa vĩnh viễn

---

### URL tham chiếu nhanh

| URL | Mô tả |
|-----|-------|
| `/dashboard` | Tổng quan: KPI, biểu đồ 6 tháng, sắp hạn, top nợ |
| `/ho-gia-dinh` | Danh sách hộ, tìm kiếm, nhập Excel |
| `/ho-gia-dinh/{id}` | Chi tiết hộ: lịch sử thanh toán, nhân khẩu, phương tiện |
| `/khoan-thu` | Danh sách khoản thu, filter theo loại/tháng/trạng thái |
| `/mau-khoan-thu` | Mẫu khoản thu định kỳ, toggle bật/tắt, tạo kỳ thủ công |
| `/thanh-toan` | Danh sách thanh toán, ghi nhận, nộp thêm, hoàn tiền |
| `/thanh-toan/theo-doi` | Cross-tab: hộ × trạng thái đóng phí |
| `/thanh-toan/no-phi` | Tra cứu nợ, gửi email nhắc thủ công |
| `/thanh-toan/thong-ke` | Thống kê tổng hợp, xuất Excel |
| `/thu-ho` | Hóa đơn thu hộ: danh sách, tạo, gen, gửi email, xác nhận |
| `/thu-ho/gen` | Gen hàng loạt (chỉ truy cập qua cổng nội bộ `8081`) |
| `/thung-rac` | Khôi phục / xóa vĩnh viễn (admin only) |
| `/audit-log` | Nhật ký hoạt động + lịch sử email (admin only) |
