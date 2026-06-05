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

---

## Phân công Sprint 3

> **Nguyên tắc merge `fragments/layout.html`:** File này bị đụng bởi nhiều người — SM (Trần Khánh Linh) là người duy nhất commit thay đổi layout.html. Các member khác giao HTML snippet qua PR comment, không tự sửa file.
>
> **pom.xml (Apache POI):** Người D thêm dependency trước, Người E rebase sau khi D merge.
>
> **`ThanhToanRepository.java`:** Người B merge query trước, Người D rebase lên branch của B trước khi merge vào develop.

---

### 👤 Người A — Trần Thị Nhật Linh · UC014: Đổi mật khẩu

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/NguoiDungController.java` | Thêm `GET/POST /doi-mat-khau` |
| `service/NguoiDungService.java` | Thêm `doiMatKhau()` |
| `templates/nguoi-dung/doi-mat-khau.html` | Template mới |
| `fragments/layout.html` | Thêm link "Đổi mật khẩu" vào navbar *(giao snippet cho SM)* |

**Tiêu chuẩn hoàn thành (DoD):**
- [ ] `GET /doi-mat-khau` hiển thị form 3 trường: mật khẩu cũ, mật khẩu mới, xác nhận mới
- [ ] Validation: mật khẩu cũ sai → lỗi tại field; mật khẩu mới < 6 ký tự → lỗi; 2 trường mới không khớp → lỗi; mật khẩu mới trùng mật khẩu cũ → lỗi
- [ ] `NguoiDungService.doiMatKhau()` dùng `passwordEncoder.matches()` để kiểm tra mật khẩu cũ trước khi encode mới
- [ ] Sau khi thành công: set `doiMatKhauLanDau = false`, `HttpSession.invalidate()` + `SecurityContextHolder.clearContext()`, redirect `/login?logout`
- [ ] Audit log: `[AUDIT] Đổi mật khẩu: user=X`
- [ ] Form theo đúng pattern Bootstrap 5: field + `invalid-feedback`

**Rủi ro conflict:** Thấp. `/doi-mat-khau` đã được phép cho mọi user đã đăng nhập theo rule `anyRequest().authenticated()` — **không cần sửa SecurityConfig**.

---

### 👤 Người B — Đoàn Văn Thắng · UC011: Tra cứu nợ phí

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/ThanhToanController.java` | Thêm `tracuuNo()` |
| `service/ThanhToanService.java` | Thêm `findDanhSachNo()` |
| `dao/ThanhToanRepository.java` | Thêm `@Query` tìm `CON_NO` theo filter |
| `templates/thanh-toan/tra-cuu-no.html` | Template mới |

**Tiêu chuẩn hoàn thành (DoD):**
- [ ] `GET /thanh-toan/tra-cuu-no` liệt kê `ThanhToan` có `trangThai = CON_NO`
- [ ] Filter `?kyThu=yyyy-MM` lọc theo `KhoanThu.kyThu` — dùng `@Query` với `YEAR()` + `MONTH()` (không thể dùng method name tự sinh)
- [ ] Filter `?idKhoan=X` lọc theo khoản thu cụ thể
- [ ] Mỗi dòng hiển thị: Mã căn hộ · Tên chủ hộ · Tên khoản thu · Số tiền còn nợ (`soTienYeuCau - soTienDaNop`) · Ngày tạo
- [ ] Khi không có nợ → thông báo "Không có khoản nợ nào"
- [ ] Chỉ `admin` truy cập — thêm rule vào `SecurityConfig` (thông báo team vì file dùng chung)
- [ ] Nút "Xuất báo cáo" link sang `?exportNo=true` (kết nối với UC013 của Người D)
- [ ] Thêm link "Tra cứu nợ phí" vào sidebar *(giao snippet cho SM)*

**Rủi ro conflict:** Trung bình. `ThanhToanRepository.java` bị Người D cùng đụng → **Người B merge trước**, Người D rebase.

---

### 👤 Người C — Đặng Hải Đăng · UC016: Quản lý phí gửi xe

**File chạm đến (toàn bộ file mới):**

| File | Mô tả |
|------|-------|
| `model/XeDangKy.java` | Entity: `loaiXe ENUM`, `idHo FK`, `soLuong`, `ngayDangKy` |
| `dao/XeDangKyRepository.java` | `JpaRepository` mới |
| `service/XeDangKyService.java` | `tinhPhiGuiXe(idHo, kyThu)` |
| `controller/XeDangKyController.java` | `/phi-gui-xe/**` |
| `templates/phi-gui-xe/list.html` | Danh sách xe theo hộ |
| `templates/phi-gui-xe/form.html` | Form đăng ký xe |
| `database/migration_v7_phi_gui_xe.sql` | Tạo bảng `xe_dang_ky` |

**Tiêu chuẩn hoàn thành (DoD):**
- [ ] Migration v7 tạo bảng `xe_dang_ky` (`id`, `loai_xe ENUM('XE_MAY','O_TO')`, `id_ho FK`, `so_luong INT`, `ngay_dang_ky`)
- [ ] `GET /phi-gui-xe` liệt kê xe đăng ký, lọc được theo loại xe
- [ ] `GET/POST /phi-gui-xe/them`: chọn hộ từ dropdown, chọn loại xe, nhập số lượng
- [ ] `POST /phi-gui-xe/xoa/{id}`: xóa có confirm dialog
- [ ] `tinhPhiGuiXe()` tính = số xe máy × đơn giá + số ô tô × đơn giá; đơn giá đọc từ `application.properties` với `@Value` có giá trị mặc định (`phi.xe.may=70000`, `phi.o.to=200000`)
- [ ] Không tự động tạo `ThanhToan` — tính phí hiển thị thông tin, kế toán tạo khoản thu thủ công
- [ ] Audit log theo chuẩn `[AUDIT]`
- [ ] Thêm link "Phí gửi xe" vào sidebar *(giao snippet cho SM)*

**Rủi ro conflict:** Thấp nhất. Toàn bộ file mới.

---

### 👤 Người D — Lê Quang Huy · UC012 + UC013: Thống kê & Xuất báo cáo

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/BaoCaoController.java` | Controller MỚI `/bao-cao` |
| `service/BaoCaoService.java` | Service MỚI |
| `dao/ThanhToanRepository.java` | Thêm `@Query` thống kê tổng hợp |
| `templates/bao-cao/thong-ke.html` | Template thống kê |
| `templates/bao-cao/xuat.html` | Template xuất (hoặc gộp vào thong-ke) |
| `pom.xml` | Thêm Apache POI + iText/Flying Saucer *(thêm trước Người E)* |

**Tiêu chuẩn hoàn thành (DoD):**

UC012 — Thống kê:
- [ ] `GET /bao-cao/thong-ke?idKhoan=X`: Tổng phải thu · Tổng đã thu · Tổng còn nợ · Số hộ đã đóng · Số hộ chưa đóng
- [ ] Dropdown chọn khoản thu (tái dụng `KhoanThuService.findAll()`)
- [ ] Bảng chi tiết: Mã hộ · Chủ hộ · Số tiền yêu cầu · Số tiền đã nộp · Trạng thái
- [ ] Khi không có dữ liệu → "Chưa có dữ liệu cho kỳ này"

UC013 — Xuất báo cáo:
- [ ] `GET /bao-cao/xuat-excel?idKhoan=X` → `ResponseEntity<byte[]>`, `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, tên file `baocao_{tenKhoan}_{ngay}.xlsx`
- [ ] `GET /bao-cao/xuat-pdf?idKhoan=X` → PDF (Thymeleaf → HTML → PDF hoặc iText)
- [ ] File Excel: header thông tin khoản thu + bảng chi tiết từng hộ + dòng tổng cộng
- [ ] Audit log: `[AUDIT] Xuất báo cáo: khoan=X, format=xlsx/pdf, user=Y`
- [ ] Thêm link "Báo cáo" vào sidebar *(giao snippet cho SM)*

**Rủi ro conflict:** Trung bình. `ThanhToanRepository.java` bị Người B cùng đụng → rebase lên branch của B. `pom.xml` bị Người E cùng đụng → Người D merge trước.

---

### 👤 Người E — Trần Khánh Linh · UC017: Import Excel + Kiểm thử + Refactor

**File chạm đến:**

| File | Thay đổi |
|------|---------|
| `controller/ImportController.java` | Controller MỚI `/import` |
| `service/ImportService.java` | Service MỚI |
| `templates/import/form.html` | Form upload + link tải template mẫu |
| `templates/import/preview.html` | Bảng preview dòng hợp lệ / lỗi |
| `pom.xml` | Apache POI *(rebase sau Người D)* |

**Tiêu chuẩn hoàn thành (DoD):**

UC017 — Import:
- [ ] Template Excel mẫu: 2 sheet — `HoGiaDinh` (soCanHo, chuHo, tangKhuVuc) và `NhanKhau` (hoTen, ngaySinh, cccd, gioiTinh, soCanHo_reference)
- [ ] `POST /import/preview`: validate từng dòng, trả về danh sách hợp lệ/lỗi
- [ ] `POST /import/confirm`: lưu trong 1 `@Transactional` — rollback toàn bộ nếu có exception (không bắt exception im lặng)
- [ ] Validate: CCCD đúng 9 hoặc 12 số · soCanHo không trùng nếu hộ mới · ngày sinh đúng định dạng
- [ ] Báo cáo kết quả: "Thành công X dòng / Lỗi Y dòng" kèm chi tiết từng dòng lỗi
- [ ] Cả `admin` và `nhanvien` đều dùng được

Kiểm thử tích hợp:
- [ ] 4 luồng: đăng nhập → tạo khoản thu → thu phí → thống kê
- [ ] Mỗi luồng: 1 happy path + 1 error case

Refactor:
- [ ] Tất cả controller/service đều có `@Slf4j` và format `[AUDIT]` nhất quán
- [ ] Không có magic string trong service layer
- [ ] Review tên method trong các `@Query` của DAO đảm bảo rõ ràng

**Rủi ro conflict:** Thấp. Toàn bộ file mới. Chỉ cần coordinate `pom.xml` với Người D.
