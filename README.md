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
| **Theo dõi thu phí** | Bảng cross-tab: tất cả hộ vs trạng thái đóng phí theo tháng/khoản | ✅ |
| **Nhật ký hoạt động** | Ghi audit log toàn hệ thống, filter theo loại đối tượng / người dùng / khoảng ngày (admin only) | ✅ |
| **Email thông báo** | Gửi email @Async qua Brevo SMTP khi khoản thu mới được tạo, bỏ qua nếu hộ không có email | ✅ |
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
source database/migration_v6b_bien_dong_fix.sql;
source database/migration_v7_bien_dong_ngay_ket_thuc.sql;
source database/migration_v8_audit_log.sql;
source database/migration_v9_dien_tich_fee.sql;
source database/migration_v10_mau_khoan_thu.sql;
source database/migration_v11_email_ho_gia_dinh.sql;
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
├── controller/          # 9 Spring MVC Controllers
│   ├── HomeController           # / → /dashboard (thống kê)
│   ├── AuthController           # /login
│   ├── ErrorPageController      # /error/403, /error/404
│   ├── NguoiDungController      # /nguoi-dung/** (admin only)
│   ├── HoGiaDinhController      # /ho-gia-dinh/**
│   ├── NhanKhauController       # /nhan-khau/** + biến động
│   ├── KhoanThuController       # /khoan-thu/**
│   ├── LoaiKhoanThuController   # /loai-khoan-thu/**
│   ├── MauKhoanThuController    # /mau-khoan-thu/** + toggle + tạo kỳ thủ công
│   ├── ThanhToanController      # /thanh-toan/** + theo dõi
│   └── AuditLogController       # /audit-log/** (admin only)
│
├── service/             # 10 Service classes
│   ├── CustomUserDetailsService # Spring Security user loading
│   ├── NguoiDungService         # BCrypt encode, audit log, self-role guard
│   ├── HoGiaDinhService         # Delete constraint, CCCD search, audit log
│   ├── NhanKhauService          # CCCD uniqueness, audit log
│   ├── BienDongService          # Ghi biến động + auto-update + scheduled hết hạn
│   ├── KhoanThuService          # Auto-apply bắt buộc, phí diện tích, audit log
│   ├── LoaiKhoanThuService
│   ├── MauKhoanThuService       # Scheduled job ngày 28, startup check, toggle active
│   ├── ThanhToanService         # nopThem, baoDaHoanTien, delete/reset
│   ├── AuditLogService          # Ghi log vào DB + SLF4J
│   └── EmailService             # Gửi email @Async qua Brevo SMTP
│
├── dao/                 # 9 Spring Data JPA Repositories
│   ├── NguoiDungRepository
│   ├── HoGiaDinhRepository      # findByCccdNhanKhau (JPQL join)
│   ├── NhanKhauRepository
│   ├── BienDongRepository
│   ├── KhoanThuRepository       # existsByMauKhoanThuIdAndKyThu (idempotent check)
│   ├── LoaiKhoanThuRepository
│   ├── MauKhoanThuRepository
│   ├── ThanhToanRepository      # sumSoTienDaNopThangNay, existsByHoAndKhoan
│   └── AuditLogRepository       # findWithFilter (dynamic JPQL), findDistinctNguoiDung
│
├── model/               # 9 JPA Entities + 6 Enums
│   ├── Entities: NguoiDung, HoGiaDinh, NhanKhau, BienDong
│   │            LoaiKhoanThu, KhoanThu, MauKhoanThu, ThanhToan, AuditLog
│   └── Enums:   VaiTro (admin/staff), LoaiApDung, TinhTrangCuTru
│                LoaiBienDong, TrangThaiThanhToan, PhuongThucThanhToan
│
└── util/
    ├── SecurityConfig           # BCrypt bean, @EnableAsync, route rules, form login
    └── DataInitializer          # ApplicationRunner — auto-tạo admin khi lần đầu chạy

src/main/resources/
├── templates/           # 23 Thymeleaf HTML templates (Bootstrap 5.3.2)
│   ├── fragments/layout.html    # head, navbar (sticky), sidebar (sticky), alerts, scripts
│   ├── auth/login.html          # login với theme toggle riêng
│   ├── error/{403,404}.html
│   ├── dashboard.html
│   ├── nguoi-dung/{list,form}.html
│   ├── ho-gia-dinh/{list,form,detail}.html
│   ├── nhan-khau/{list,form,bien-dong-form}.html
│   ├── loai-khoan-thu/{list,form}.html
│   ├── khoan-thu/{list,form}.html
│   ├── mau-khoan-thu/{list,form}.html
│   ├── thanh-toan/{list,form,theo-doi}.html
│   └── audit-log/list.html
├── static/css/
│   ├── base-theme.css           # Layout, typography, sticky navbar/sidebar
│   ├── moona-theme.css          # Dark mode (Moonlit Observatory palette)
│   └── kanata-theme.css         # Light mode (Soft Purple Glassmorphism)
└── static/

database/                        # Chạy theo thứ tự trên DB mới
├── bluemoon_schema.sql                       # v1  — Schema khởi tạo
├── migration_v2_payment_fields.sql           # v2  — Trường thanh toán
├── migration_v3_khoan_thu_enhancements.sql   # v3  — Cải tiến khoản thu
├── migration_v4_auth_enhancements.sql        # v4  — Cột active người dùng
├── migration_v5_doi_mat_khau.sql             # v5  — doi_mat_khau_lan_dau
├── migration_v6_ho_nhan_khau.sql             # v6  — tang_khu_vuc, tinh_trang, bien_dong
├── migration_v6b_bien_dong_fix.sql           # v6b — Fix bảng bien_dong
├── migration_v7_bien_dong_ngay_ket_thuc.sql  # v7  — ngay_ket_thuc
├── migration_v8_audit_log.sql                # v8  — Bảng audit_log
├── migration_v9_dien_tich_fee.sql            # v9  — don_gia_per_m2, so_tien_yeu_cau
├── migration_v10_mau_khoan_thu.sql           # v10 — Bảng mau_khoan_thu, FK id_mau
└── migration_v11_email_ho_gia_dinh.sql       # v11 — Cột email hộ gia đình
```

---

## Phân quyền

| Vai trò | Quyền truy cập |
|---------|---------------|
| `admin` | Toàn bộ, bao gồm `/nguoi-dung/**` và `/audit-log/**` |
| `staff` | Dashboard, hộ gia đình, nhân khẩu, khoản thu, mẫu thu, thanh toán |

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

---

## Phân công Sprint 3

---

### 👤 Người A · UC014: Đổi mật khẩu

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/NguoiDungController.java` | Thêm `GET /doi-mat-khau` và `POST /doi-mat-khau` |
| `service/NguoiDungService.java` | Thêm `doiMatKhau(tenDangNhap, matKhauCu, matKhauMoi)` |
| `templates/nguoi-dung/doi-mat-khau.html` | Template mới (pattern Form page) |
| `fragments/layout.html` | Thêm link "Đổi mật khẩu" vào khu vực user trên **navbar** (cạnh tên user) |

**Tiêu chuẩn hoàn thành (DoD):**
- [ ] `GET /doi-mat-khau` hiển thị form 3 trường: mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới
- [ ] Validation server-side: mật khẩu cũ sai → `bindingResult.rejectValue`; mật khẩu mới < 6 ký tự → lỗi; 2 trường mới không khớp → lỗi; mật khẩu mới trùng cũ → lỗi
- [ ] `NguoiDungService.doiMatKhau()` dùng `passwordEncoder.matches()` kiểm tra mật khẩu cũ trước khi `passwordEncoder.encode()` mật khẩu mới
- [ ] Sau khi thành công: set `doiMatKhauLanDau = false`, gọi `new SecurityContextLogoutHandler().logout(request, response, auth)`, redirect `/login?logout`
- [ ] Ghi audit log qua `auditLogService.log("Đổi mật khẩu", "Người dùng", "user=" + tenDangNhap, tenDangNhap)`
- [ ] Form dùng đúng pattern Bootstrap 5 + `invalid-feedback`, không dùng `th:field` cho password (tránh lộ giá trị)

**Lưu ý kỹ thuật:**
- `/doi-mat-khau` đã được phép theo rule `anyRequest().authenticated()` — **không sửa `SecurityConfig`**
- Dùng `SecurityContextLogoutHandler` thay vì `HttpSession.invalidate()` trực tiếp để Spring Security xử lý đúng session

**Rủi ro conflict:** Thấp. Chỉ chạm `NguoiDungController` và `NguoiDungService` ở phần cuối file.

---

### 👤 Người B · UC011: Tra cứu nợ phí

> **Lưu ý:** Filter `?trangThai=CON_NO` đã tồn tại tại `/thanh-toan`. UC011 là trang **chuyên biệt** tập trung vào nợ phí với filter bổ sung theo kỳ thu và khoản thu, hiển thị số tiền còn thiếu cụ thể.

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/ThanhToanController.java` | Thêm `GET /thanh-toan/tra-cuu-no` |
| `service/ThanhToanService.java` | Thêm `findDanhSachNo(Integer idKhoan, YearMonth kyThu)` |
| `dao/ThanhToanRepository.java` | Thêm `@Query` lọc `CON_NO` theo `idKhoan` + `kyThu` |
| `templates/thanh-toan/tra-cuu-no.html` | Template mới |

**Tiêu chuẩn hoàn thành (DoD):**
- [ ] `GET /thanh-toan/tra-cuu-no` liệt kê `ThanhToan` có `trangThai = CON_NO`
- [ ] Filter `?idKhoan=X` lọc theo khoản thu; `?kyThu=yyyy-MM` lọc theo `KhoanThu.kyThu` — query dùng `YEAR()`+`MONTH()` trong JPQL
- [ ] Mỗi dòng: Mã căn hộ · Tên chủ hộ · Tên khoản thu · Số tiền yêu cầu (`getSoTienYeuCauHieuLuc()`) · Đã nộp · Còn thiếu · Hạn nộp
- [ ] Dòng quá hạn (`hanNop < today`) hiển thị badge `bg-danger`
- [ ] Khi không có nợ → thông báo "Không có khoản nợ nào"
- [ ] Nút "Xuất Excel" link sang `GET /bao-cao/xuat-excel?trangThai=CON_NO&idKhoan=X` (kết nối UC013 Người D)
- [ ] Thêm link "Tra cứu nợ phí" vào sidebar dưới nhóm "Quản lý thu phí" *(giao snippet cho SM)*
- [ ] Mọi user đã đăng nhập đều truy cập được (`anyRequest().authenticated()` — **không sửa `SecurityConfig`**)

**Rủi ro conflict:** Trung bình. `ThanhToanRepository.java` bị Người D cùng đụng → **Người B merge trước**, Người D rebase.

---

### 👤 Người C · UC016: Quản lý phí gửi xe

> **Lưu ý:** Migration hiện tại đã tới v11. File migration của task này phải là **v12**.

**File chạm đến (toàn bộ file mới):**

| File | Mô tả |
|------|-------|
| `model/XeDangKy.java` | Entity: `loaiXe` (`@Enumerated STRING`), `hoGiaDinh` (`@ManyToOne`), `soLuong`, `ngayDangKy` |
| `model/LoaiXe.java` | Enum mới: `XE_MAY`, `O_TO` (có `tenHienThi`, `donGia` mặc định) |
| `dao/XeDangKyRepository.java` | `findByHoGiaDinhId(Integer)`, `findByLoaiXe(LoaiXe)` |
| `service/XeDangKyService.java` | `tinhPhiGuiXe(HoGiaDinh, YearMonth)`, CRUD + audit log |
| `controller/XeDangKyController.java` | `/phi-gui-xe/**` |
| `templates/phi-gui-xe/list.html` | Danh sách xe, filter theo hộ/loại |
| `templates/phi-gui-xe/form.html` | Form đăng ký xe (chọn hộ, loại, số lượng) |
| `database/migration_v12_phi_gui_xe.sql` | Tạo bảng `xe_dang_ky` |

**Tiêu chuẩn hoàn thành (DoD):**
- [ ] `migration_v12` tạo bảng `xe_dang_ky`: `id`, `loai_xe VARCHAR(10)`, `id_ho INT FK`, `so_luong INT NOT NULL`, `ngay_dang_ky DATE NOT NULL`; thêm `spring.jpa.hibernate.ddl-auto=validate` sẽ pass
- [ ] `GET /phi-gui-xe` liệt kê tất cả xe, có filter theo hộ và loại xe
- [ ] `GET/POST /phi-gui-xe/them`: chọn hộ từ dropdown (dùng `HoGiaDinhService.findAll()`), chọn loại xe, nhập số lượng ≥ 1
- [ ] `POST /phi-gui-xe/xoa/{id}`: xóa với confirm dialog
- [ ] `tinhPhiGuiXe()` = `soLuong × donGia`; đơn giá đọc từ `application.properties` với `@Value(${phi.xe.may:70000})` và `@Value(${phi.o.to:200000})`
- [ ] Không tự tạo `ThanhToan` — chỉ hiển thị tính toán; kế toán tạo khoản thu thủ công
- [ ] Audit log qua `auditLogService` theo chuẩn hiện tại
- [ ] Thêm link "Phí gửi xe" vào sidebar dưới nhóm "Quản lý thu phí" *(giao snippet cho SM)*

**Rủi ro conflict:** Thấp nhất. Toàn bộ file mới, không đụng file hiện tại.

---

### 👤 Người D · UC012 + UC013: Thống kê & Xuất báo cáo

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/BaoCaoController.java` | Controller MỚI `/bao-cao/**` |
| `service/BaoCaoService.java` | Service MỚI — tổng hợp số liệu |
| `dao/ThanhToanRepository.java` | Thêm các `@Query` aggregate (rebase lên branch Người B) |
| `templates/bao-cao/thong-ke.html` | Trang thống kê + nút xuất |
| `pom.xml` | Thêm `apache-poi` (xlsx) — **merge trước Người E** |

**Tiêu chuẩn hoàn thành (DoD):**

UC012 — Thống kê:
- [ ] `GET /bao-cao/thong-ke?idKhoan=X`: hiển thị tổng phải thu, đã thu, còn nợ, số hộ đã đóng, số hộ chưa đóng
- [ ] Tính tiền dùng `getSoTienYeuCauHieuLuc()` — không dùng trực tiếp `khoanThu.soTien` (bỏ qua phí diện tích)
- [ ] Dropdown chọn khoản thu từ `KhoanThuService.findAll()`; filter thêm `?kyThu=yyyy-MM`
- [ ] Bảng chi tiết: Mã hộ · Chủ hộ · Số tiền yêu cầu · Đã nộp · Trạng thái (badge màu theo convention)
- [ ] Thêm link "Báo cáo" vào sidebar dưới nhóm "Quản lý thu phí" *(giao snippet cho SM)*

UC013 — Xuất Excel:
- [ ] `GET /bao-cao/xuat-excel?idKhoan=X` → `ResponseEntity<byte[]>` với `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, filename `baocao_{maKhoanThu}_{yyyy-MM-dd}.xlsx`
- [ ] Sheet Excel: hàng tiêu đề (tên khoản, kỳ thu, ngày xuất) + bảng chi tiết + dòng tổng cộng
- [ ] Hỗ trợ filter `?trangThai=CON_NO` để Người B có thể link sang (UC011 → UC013)
- [ ] Audit log: `"Xuất báo cáo"`, `"Báo cáo"`, `"khoan=" + maKhoanThu + ", format=xlsx"`, `currentUser()`

**Rủi ro conflict:** Trung bình. `ThanhToanRepository.java` cùng đụng Người B → **rebase lên branch B sau khi B merge**. `pom.xml` cùng đụng Người E → **Người D merge pom.xml trước**.

---

### 👤 Người E · UC017: Import Excel + Kiểm thử

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/ImportController.java` | Controller MỚI `/import/**` |
| `service/ImportService.java` | Service MỚI — parse + validate + save |
| `templates/import/form.html` | Form upload + link tải file mẫu |
| `templates/import/ket-qua.html` | Bảng kết quả: dòng thành công / dòng lỗi |
| `pom.xml` | `apache-poi` *(rebase sau Người D nếu D đã thêm)* |

**Tiêu chuẩn hoàn thành (DoD):**

UC017 — Import:
- [ ] File Excel mẫu tải về gồm 2 sheet: `HoGiaDinh` (soCanHo, chuHo, dienTich, tangKhuVuc, email) và `NhanKhau` (hoTen, ngaySinh `dd/MM/yyyy`, cccd, gioiTinh, quanHeChuHo, soCanHo)
- [ ] `POST /import/upload`: parse file, validate từng dòng, redirect sang trang kết quả (không lưu DB ở bước này)
- [ ] `POST /import/confirm`: lưu toàn bộ trong 1 `@Transactional` — nếu có exception thì rollback hết, không bắt exception im lặng
- [ ] Validate: CCCD pattern `^\d{9}$|^\d{12}$` · `soCanHo` không trùng với DB hiện tại · `email` đúng format nếu có · `ngaySinh` parse được
- [ ] Sau import thành công: gọi `KhoanThuService.autoApplyForNewHo()` cho từng hộ mới — đảm bảo khoản thu bắt buộc được áp dụng
- [ ] Kết quả hiển thị: tổng dòng · thành công · lỗi · bảng chi tiết lỗi từng dòng (dòng số, field, lý do)
- [ ] Cả `admin` và `staff` đều truy cập được

Kiểm thử:
- [ ] Viết tối thiểu 4 test case thủ công (ghi vào file `test/kiem-thu-sprint3.md`): đăng nhập → import hộ → tạo khoản thu bắt buộc → kiểm tra ThanhToan tự tạo → ghi nhận thanh toán → xem báo cáo
- [ ] Mỗi luồng: 1 happy path + 1 error case (CCCD trùng, email sai format, v.v.)

**Rủi ro conflict:** Thấp. Toàn bộ file mới. Chỉ cần coordinate `pom.xml` với Người D.
