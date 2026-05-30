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

## Hướng dẫn cài đặt

1. Cài đặt: JDK 17+, MySQL 8+
2. Clone repo: `git clone <url>`
3. Import schema vào MySQL:
   ```sql
   source database/bluemoon_schema.sql
   ```
4. Cấu hình kết nối database trong `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/bluemoon
   spring.datasource.username=<username>
   spring.datasource.password=<password>
   ```
5. Chạy ứng dụng:
   ```bash
   ./mvnw spring-boot:run
   ```
6. Truy cập tại `http://localhost:8080`

## Branching Strategy

| Nhánh | Mục đích |
|-------|---------|
| `main` | Nhánh chính, chỉ merge khi hoàn thành Sprint |
| `develop` | Nhánh phát triển chung |
| `feature/[tên-chức-năng]` | Mỗi tính năng một nhánh riêng |
| `hotfix/[mô-tả]` | Sửa lỗi khẩn |

## Kế hoạch Sprint

| Sprint | Nội dung | Trạng thái |
|--------|----------|-----------|
| Sprint 0 | Khởi động, lập kế hoạch, thiết lập repo, tạo schema DB | ✅ Hoàn thành |
| Sprint 1 | Đăng nhập, phân quyền, quản lý khoản thu | 🔄 In Progress |
| Sprint 2 | Thu phí, quản lý hộ gia đình, nhân khẩu | Not Started |
| Sprint 3 | Thống kê, báo cáo, kiểm thử tích hợp | Not Started |
