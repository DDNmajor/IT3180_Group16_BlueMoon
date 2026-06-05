-- Migration v10: Mẫu khoản thu định kỳ (recurring fee templates)
-- Run after migration_v9_dien_tich_fee.sql

-- 1. Bảng mau_khoan_thu: blueprint để auto-tạo KhoanThu hàng tháng
--    Chỉ dùng cho loại BAT_BUOC_DINH_KY.
CREATE TABLE mau_khoan_thu (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    ten_mau           VARCHAR(200) NOT NULL            COMMENT 'Tên hiển thị, VD: Phí dịch vụ hàng tháng',
    ma_mau_prefix     VARCHAR(20)  NOT NULL UNIQUE     COMMENT 'Prefix mã kỳ, VD: DV → DV-2026-06',
    id_loai           INT NOT NULL,
    so_tien           DECIMAL(15, 2) NOT NULL          COMMENT 'Phí cố định (fallback khi không có don_gia_per_m2)',
    don_gia_per_m2    DECIMAL(15, 2) NULL              COMMENT 'Đơn giá/m² (từ v9); NULL = phí cố định',
    don_vi            VARCHAR(50)  NULL,
    so_ngay_han_nop   INT          NULL                COMMENT 'hanNop = đầu tháng + N ngày; NULL = không có hạn',
    active            BOOLEAN NOT NULL DEFAULT TRUE    COMMENT 'FALSE = tạm dừng, job bỏ qua',
    ngay_tao          DATETIME NOT NULL,
    ghi_chu           TEXT NULL,
    CONSTRAINT fk_mau_loai FOREIGN KEY (id_loai) REFERENCES loai_khoan_thu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Liên kết khoan_thu → mau_khoan_thu
--    ON DELETE SET NULL: xóa template không xóa các KhoanThu đã tạo, chỉ nullify FK
ALTER TABLE khoan_thu
    ADD COLUMN id_mau INT NULL COMMENT 'NULL = tạo thủ công; NOT NULL = auto-tạo từ template',
    ADD CONSTRAINT fk_khoan_thu_mau
        FOREIGN KEY (id_mau) REFERENCES mau_khoan_thu(id) ON DELETE SET NULL;
