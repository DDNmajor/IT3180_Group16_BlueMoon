# CLAUDE.md — BlueMoon Fee Management System

## Quy tắc bắt buộc

1. **Luôn bắt đầu mọi câu trả lời bằng "Kon-Kanata"** — không ngoại lệ.
2. **Luôn cập nhật CLAUDE.md** sau khi thêm tính năng, entity/service/controller mới.
3. **Comment** chỉ dùng `// nội dung ngắn gọn`; chỉ khi WHY không hiển nhiên; xóa comment giải thích WHAT.

---

## Tech stack

| Thành phần | Chi tiết |
|-----------|---------|
| Framework | Spring Boot **4.0.6** (webmvc — không phải webflux) |
| Persistence | Spring Data JPA + Hibernate + **MySQL 8** |
| Security | Spring Security 6 — form login, BCrypt, role-based |
| Template | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| Validation | `spring-boot-starter-validation` (jakarta.validation) |
| Boilerplate | Lombok (`@Getter/@Setter/@RequiredArgsConstructor` trên mọi class) |
| Frontend | Bootstrap 5.3.2 + Bootstrap Icons 1.11.3 (CDN) |
| Build | Maven Wrapper (`mvnw.cmd` / `./mvnw`) |
| Java | 17 |
| Mail | `spring-boot-starter-mail` — Brevo SMTP (`smtp-relay.brevo.com:587`), gửi @Async |

## Chạy project

```bash
mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run     # macOS/Linux
```

http://localhost:8080 · DB config: `src/main/resources/application.properties` (gitignored)

## Cấu trúc package

```
com.bluemoon/
├── controller/   @Controller, @Valid + BindingResult
│   ├── HomeController, AuthController, ErrorPageController
│   ├── NguoiDungController, HoGiaDinhController (+ POST /import Excel)
│   ├── NhanKhauController, KhoanThuController, LoaiKhoanThuController
│   ├── MauKhoanThuController, ThanhToanController, AuditLogController
│   ├── PhuongTienController   (/ho-gia-dinh/{idHo}/phuong-tien/**)
│   ├── ThanhToanBaoCaoController  (/thanh-toan/thong-ke, /no-phi, /thong-ke/export)
│   ├── HoaDonThuHoController  (/thu-ho — tạo/gen/xác nhận/hủy/gửi email hóa đơn điện-nước-internet-gas)
│   ├── ThungRacController     (/thung-rac — admin only, soft delete restore + permanent delete)
│   ├── LichSuEmailController  (/lich-su-email — standalone, không link sidebar, dùng qua AuditLogController tab)
│   └── AuditLogController     (/audit-log?tab=nhat-ky|email — 2 tab: nhật ký hệ thống + lịch sử email)
├── service/      @Service
│   ├── CustomUserDetailsService, EmailService (@Async + sync), AuditLogService
│   ├── NguoiDungService, HoGiaDinhService, NhanKhauService, BienDongService
│   ├── KhoanThuService, LoaiKhoanThuService, MauKhoanThuService, ThanhToanService
│   ├── PhuongTienService      (CRUD xe + recalculatePerXeForHo)
│   ├── LichSuEmailService     (ghiLichSu + findWithFilter — 1000 bản ghi gần nhất)
│   ├── HoaDonThuHoService     (taoHoaDon, genHangLoat, guiEmailHangLoat, xacNhanThanhToan)
│   ├── BaoCaoThanhToanService (thống kê, nợ phí, DTO aggregation)
│   ├── ExcelExportService     (xuất báo cáo .xlsx — apache-poi)
│   ├── ExcelImportService     (nhập HoGiaDinh + NhanKhau từ .xlsx; generateTemplate(); trả ImportResult record)
│   └── EmailSchedulerService  (3 @Scheduled: nhacNoHangTuan Mon 8:00, nhacGanHan daily 8:00, nhacVuaQuaHan daily 9:00)
├── model/enums+  LoaiEmail (THONG_BAO_KHOAN_THU, CHAO_MUNG_HO_MOI, NHAC_NO_TU_DONG, NHAC_NO_THU_CONG, THU_HO_THONG_BAO, THU_HO_XAC_NHAN)
│              LoaiTinhPhi + THU_HO — số tiền nhập riêng từng hộ qua /thanh-toan/nhap-thu-ho/{idKhoan}
│              LoaiDichVuThuHo (DIEN, NUOC, INTERNET, GAS) + TrangThaiHoaDonThuHo (CHO_THANH_TOAN, DA_THANH_TOAN, DA_HUY)
│              LoaiTinhPhi chỉ còn: FIXED, PER_M2, PER_XE, PER_PERSON (THU_HO đã xóa — dùng HoaDonThuHo thay thế)
├── dao/          extends JpaRepository + @Query
├── dto/          NoPhiChiTietDto (+ hanNop), NoPhiHoDto, ThongKeKhoanThuDto (+ maKhoanThu, kyThu)
├── model/        @Entity + Lombok; @Enumerated(EnumType.STRING)
└── util/         SecurityConfig (@EnableWebSecurity @EnableAsync), DataInitializer
```

→ Entities, enums, fields, URL→template map: **`docs/claude/entities-enums.md`**

---

## Database & Security

Schema: `database/bluemoon_schema.sql` — chạy để init/reset. `ddl-auto=validate`.  
Data mẫu: `database/mock_data.sql` (tùy chọn, chạy sau schema).

**Phân quyền:**
- `/nguoi-dung/**`, `/audit-log/**`, `/thung-rac/**` → chỉ `admin`
- Mọi route khác → bất kỳ user đã đăng nhập
- Lỗi: `/error/403`, `/error/404`

**BCrypt:** `NguoiDungService.save()` tự encode nếu chưa bắt đầu `$2a$`. Blank password = giữ hash cũ.

## Quy ước đặt tên

- **Java fields**: camelCase tiếng Việt — `hoTen`, `soCanHo`, `ngayTao`
- **URL / template**: kebab-case — `/khoan-thu`, `/ho-gia-dinh`
- **Model attribute**: camelCase — `danhSach`, `tongHoGiaDinh`
- **Flash**: `successMsg` (thành công) · `errorMsg` (lỗi)

---

## UI — Tổng quan

CSS: `moona-theme.css` (dark) + `kanata-theme.css` (light — **default**) + `base-theme.css`.  
Toggle qua `#themeToggleBtn` (`onclick="toggleKanataTheme()"`), persist `localStorage[bluemoon-theme]`.  
`toggleKanataTheme()` + `updateThemeButton()` định nghĩa trong **`<head>` fragment** (anti-FOUC script) để đảm bảo luôn available, dù scripts fragment ở cuối body có lỗi.  
→ Palette, fonts, sticky CSS, sidebar keys, theme gotchas: **`docs/claude/ui-theme.md`**

**Layout skeleton** (mọi trang trừ login/error):
```
<navbar .moon-navbar>  (sticky top, z-index 1030)
<div class="container-fluid">
  <div class="row">
    <sidebar col-md-2>   (sticky, height: calc(100vh - var(--navbar-h)))
    <main class="col-md-10 p-4">
```

**Fragment usage:**
```html
<head th:replace="~{fragments/layout :: head('Title')}"></head>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<div th:replace="~{fragments/layout :: sidebar('module-key')}"></div>
<div th:replace="~{fragments/layout :: alerts}"></div>
<div th:replace="~{fragments/layout :: scripts}"></div>
```
`successMsg` → `alert-success` · `errorMsg` → `alert-danger` · `undoMsg` → `alert-success` với HTML (th:utext) — dùng cho xóa có undo link

→ List/Form/Detail/Modal HTML patterns, Thymeleaf gotchas, validation: **`docs/claude/ui-patterns.md`**

**Badge conventions:**

| Context | Giá trị → Badge class |
|---------|----------------------|
| TrangThaiThanhToan | DA_DONG→`bg-success` · CON_NO→`bg-warning text-dark` · DONG_DU→`bg-info text-dark` |
| TinhTrangCuTru | THUONG_TRU→`bg-success` · TAM_TRU→`bg-info text-dark` · TAM_VANG→`bg-warning text-dark` · CHUYEN_DI→`bg-secondary` |
| VaiTro | admin→`bg-danger` · staff→`bg-secondary` |
| AuditLog | Xóa→`bg-danger` · Tạo→`bg-success` · Sửa→`bg-warning text-dark` · Hết hạn/Auto-apply→`bg-secondary` · khác→`bg-primary` |

**Buttons:** Thêm `btn-primary` · Sửa `btn-warning` · Xóa `btn-danger` · Chi tiết `btn-info text-white` · Back `btn-outline-secondary`

---

## Business Logic (tóm tắt)

→ Chi tiết đầy đủ: **`docs/claude/business-logic.md`**

**LoaiTinhPhi:** `FIXED` (null→fallback soTien) · `PER_M2` (dienTich×donGia) · `PER_XE` (tổng xe×đơn giá, fallback 70k/1.2M) · `PER_PERSON` (soNguoi×donGia — đếm NhanKhau không phải CHUYEN_DI; hộ 0 người bị bỏ qua khi auto-apply)  
Luôn dùng `ThanhToan.getSoTienYeuCauHieuLuc()` khi so sánh tiền.

**Auto-apply:** KhoanThu bắt buộc mới → ThanhToan CON_NO cho mọi hộ. Hộ mới → ThanhToan cho mọi KhoanThu bắt buộc. `phuongThuc` PHẢI set trước khi save bằng code. `autoApplyForNewHo` bỏ qua PER_XE.

**PhuongTien (xe):** Thêm/sửa/xóa xe → `recalculatePerXeForHo()` tự cập nhật ThanhToan PER_XE tháng hiện tại. Form có radio `lyDo`: `DANG_KY_THEM` / `NHAP_SAI` / `BOT_XE`. Xem chi tiết trong `docs/claude/business-logic.md`.

**Excel Import:** `POST /ho-gia-dinh/import` — upload `.xlsx`, `ExcelImportService` xử lý 2 sheet:
- Sheet 1 `HoGiaDinh`: soCanHo(*), chuHo(*), tangKhuVuc, dienTich_m2, email, ghiChu → upsert + auto-apply KhoanThu bắt buộc + recalculatePerM2 nếu diện tích thay đổi
- Sheet 2 `NhanKhau` (tùy chọn): hoTen(*), ngaySinh(dd/MM/yyyy), gioiTinh, cccd, soDienThoai, quanHeChuHo, soCanHo(*)
- Validate: trùng soCanHo trong file, trùng CCCD trong file/DB, format email/phone/CCCD/ngày sinh
- Reject **toàn bộ file** nếu có bất kỳ lỗi nào (rollback @Transactional)
- Trả `ImportResult(soHoMoi, soHoCapNhat, soNkMoi, errors)` — không dùng String return
- `GET /ho-gia-dinh/import/mau` → tải file `.xlsx` mẫu 2 sheet

**Excel Export:** `GET /thanh-toan/thong-ke/export?thang=yyyy-MM&loai=ALL` — `ExcelExportService` (inject `KhoanThuService` để dùng `tinhSoTienYeuCau()` — method là `public`):
- Sheet 1 `Thông tin Khoản thu`: tên, mã, **kỳ thu**, loại áp dụng (tenHienThi), **loại tính phí** (tenHienThi), loại KT, số tiền, đơn giá/m², hạn nộp, ngày tạo, **số hộ áp dụng**, tổng yêu cầu, đã đóng, còn thiếu
- Sheet 2 `Chi tiết từng hộ`: căn hộ, chủ hộ, **tầng/khu vực**, **email**, số NK, diện tích, **xe máy**, **ô tô**, yêu cầu BB, đã nộp BB, còn thiếu BB, đã nộp TN, trạng thái (tiếng Việt), ngày nộp gần nhất
- Trạng thái tiếng Việt nhất quán: `"Đã đóng đủ"` / `"Đóng dư"` / `"Nộp một phần"` / `"Còn nợ"` / `"Không có phí bắt buộc"`
- Dòng tổng cộng: mỗi thống kê trong ô riêng (không ghép text vào 1 ô)

**Báo cáo / Nợ phí:** `BaoCaoThanhToanService` tổng hợp DTO:
- `NoPhiChiTietDto`: idKhoanThu, tenKhoanThu, soTienYeuCau, soTienDaNop, conThieu, **hanNop**
- `ThongKeKhoanThuDto`: các field cũ + **maKhoanThu**, **kyThu**
- `no-phi.html`: cột Hạn nộp + badge `bg-danger "Quá hạn"` nếu `hanNop < today`
- Email nhắc nợ: bao gồm hạn nộp và chú thích `(ĐÃ QUÁ HẠN)` nếu cần

**Thu hộ (HoaDonThuHo) — `/thu-ho`:**
- Quản lý hóa đơn dịch vụ: `DIEN` (EVN), `NUOC` (Hawaco), `INTERNET`, `GAS`
- Tạo thủ công hoặc bulk gen theo kỳ tháng + loại dịch vụ
- Unique per hộ / loại dịch vụ / kỳ tháng
- Luồng: Tạo → Gửi email thông báo → Xác nhận → Gửi email biên nhận / Hủy
- Email: `THU_HO_THONG_BAO` (thông báo) + `THU_HO_XAC_NHAN` (biên nhận)
- `emailDaGui = true` sau khi gửi email thành công

**EmailService — các method:**
- `guiThongBaoKhoanThu(ho, kt, soTienYeuCau)` @Async — thông báo 1 khoản thu mới
- `guiThongBaoKhoanThuTongHop(ho, ktMoi, soTienYeuCau, tatCaConNo)` @Async — khoản mới + liệt kê toàn bộ CON_NO hiện tại
- `guiEmailChaoMungHoMoi(ho, danhSachKhoan)` @Async — chào mừng hộ mới + danh sách phí áp dụng
- `guiEmailNhacNoAsync(toEmail, soCanHo, chuHo, danhSachNo, tieuDe)` @Async — nhắc nợ từ scheduler
- `guiEmailThuHoThongBao(hoaDon)` @Async — thông báo hóa đơn thu hộ
- `guiEmailThuHoXacNhan(hoaDon)` @Async — biên nhận xác nhận đã thu
- `guiEmailNhacNo(toEmail, soCanHo, subject, body)` đồng bộ — nhắc nợ thủ công từ web (ném exception nếu lỗi)

**ThanhToanService — các method bổ sung:**
- `nopThem(id, soTienThem, nguoiThu)` — cộng dồn vào record CON_NO, cập nhật trạng thái
- `baoDaHoanTien(id, nguoiThu)` — reset soTienDaNop = soTienYeuCau, chuyển sang DA_DONG (xử lý DONG_DU)
- `delete(id)` — khoản bắt buộc: reset về CON_NO (trả `false`); tự nguyện: xóa hẳn (trả `true`)
- `findConNo(idHo, idKhoan)` — tìm record CON_NO để nộp dồn thay vì tạo mới

**ThanhToanController — các route bổ sung:**
- `GET /thanh-toan/theo-doi?thang=yyyy-MM&idKhoanThu=` — xem tiến độ thu theo kỳ (template `thanh-toan/theo-doi`)
- `POST /thanh-toan/nop-them/{id}` — cộng thêm tiền vào thanh toán đang CON_NO
- `POST /thanh-toan/hoan-tien/{id}` — ghi nhận đã hoàn tiền thừa (DONG_DU → DA_DONG)
- `POST /thanh-toan/xoa/{id}` — xóa hoặc reset về CON_NO tùy loại khoản
**ThanhToanRepository — queries bổ sung:**
- `hardDeleteByHoGiaDinhId` native DELETE — dùng khi xóa vĩnh viễn hộ gia đình (bypass @SQLRestriction)
- `findByTrangThai`, `findByTrangThaiAndKhoanThuHanNopBetween`, `findByTrangThaiAndKhoanThuHanNop` — dùng bởi EmailSchedulerService

**Ràng buộc xóa:** HoGiaDinh chặn nếu còn NhanKhau hoặc CON_NO. KhoanThu chặn nếu có soTienDaNop > 0.

**Soft Delete / Thùng rác (`/thung-rac` — admin only):**
- `HoGiaDinh` + `KhoanThu` có cột `deleted_at DATETIME NULL` + `@SQLRestriction("deleted_at IS NULL")` (Hibernate 6).
- `delete()` trong service: set `deletedAt = now()` thay vì `deleteById` → gửi flash `undoMsg` có link `/thung-rac`.
- Restore: native `UPDATE ... SET deleted_at = NULL` (bypass @SQLRestriction).
- Permanent delete: native `DELETE` — với KhoanThu cần xóa ThanhToan trước bằng `hardDeleteByKhoanThuId` (native query).
- `@SQLRestriction` ảnh hưởng mọi JPQL/HQL join → các query liên quan cần dùng native query khi thao tác với deleted records.
- **⚠ FK null trong template:** khi `HoGiaDinh`/`KhoanThu` bị soft-delete, JPA load `ThanhToan.hoGiaDinh` / `ThanhToan.khoanThu` trả về `null` (bị lọc bởi @SQLRestriction). Template Thymeleaf truy cập `tt.hoGiaDinh.id` sẽ ném NullPointerException → luôn thêm `th:if="${tt.hoGiaDinh != null and tt.khoanThu != null}"` trên `<tr>` trong list thanh toán.
- DB migration: nếu DB đã tồn tại, chạy: `ALTER TABLE ho_gia_dinh ADD COLUMN deleted_at DATETIME NULL DEFAULT NULL; ALTER TABLE khoan_thu ADD COLUMN deleted_at DATETIME NULL DEFAULT NULL;`

**MauKhoanThu THU_HO:**
- Mẫu loại `THU_HO` được hỗ trợ: soTien = 0, không auto-apply ThanhToan.
- Khi tạo kỳ thủ công (`POST /{id}/tao-ky`): redirect sang `/thanh-toan/nhap-thu-ho/{ktId}` để nhập tiền từng hộ.
- Khi auto-tạo (cron 28/tháng): KhoanThu THU_HO được tạo, admin cần vào mau-khoan-thu/list để nhập tiền (nút bút bên cạnh kỳ gần nhất).

**Scheduled jobs:**
- MauKhoanThu: `0 0 8 28 * *` — tạo kỳ tháng tới; startup bù nếu bỏ.
- BienDong hết hạn: `0 0 1 * * *` — TAM_VANG→THUONG_TRU; TAM_TRU→CHUYEN_DI.
- EmailSchedulerService (3 jobs):
  - `nhacNoHangTuan`: `0 0 8 * * MON` — nhắc tất cả hộ CON_NO, gom theo hộ (`guiNhacTheoNhom`)
  - `nhacGanHan`: `0 0 8 * * *` — hanNop trong 1–3 ngày tới
  - `nhacVuaQuaHan`: `0 0 9 * * *` — hanNop đúng ngày hôm qua

---

## Audit Log

Mọi hành động quan trọng ghi **hai nơi song song**: `log.info("[AUDIT] ...")` + bảng `audit_log`.

```java
auditLogService.log(hanhDong, loaiDoiTuong, chiTiet, currentUser());

private String currentUser() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null ? auth.getName() : "system";
}
```

Services ghi audit: `HoGiaDinhService`, `NhanKhauService`, `NguoiDungService`, `KhoanThuService`, `ThanhToanService`, `BienDongService`, `MauKhoanThuService`, `EmailService`, `PhuongTienService`, `ExcelImportService`.

---

## Application properties (gitignored)

| Key | Ghi chú |
|-----|--------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/bluemoon` |
| `spring.jpa.hibernate.ddl-auto` | `validate` |
| `app.admin.password` | **Bắt buộc** (DataInitializer đọc khi startup) |
| `spring.mail.host` / `port` | `smtp-relay.brevo.com` / `587` |
| `bluemoon.mail.from` | Địa chỉ email gửi |

## Dashboard

`GET /dashboard` → model: `tongHoGiaDinh`, `tongNhanKhau`, `tongKhoanThu`, `tongTienThangNay`.
