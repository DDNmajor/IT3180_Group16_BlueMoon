-- Migration v9: Area-based fee calculation + auto-apply for new households
-- Run after migration_v8_audit_log.sql

-- 1. Add don_gia_per_m2 to khoan_thu
--    NULL  = phí cố định (dùng so_tien)
--    NOT NULL = phí theo diện tích (so_tien_yeu_cau = dien_tich × don_gia_per_m2)
ALTER TABLE khoan_thu
    ADD COLUMN don_gia_per_m2 DECIMAL(15, 2) NULL
        COMMENT 'Đơn giá/m². NULL = phí cố định; NOT NULL = tính theo diện tích hộ.';

-- 2. Add so_tien_yeu_cau to thanh_toan
--    Lưu số tiền yêu cầu riêng cho từng hộ (tính từ diện tích × đơn giá).
--    NULL = dùng khoan_thu.so_tien làm fallback.
ALTER TABLE thanh_toan
    ADD COLUMN so_tien_yeu_cau DECIMAL(15, 2) NULL
        COMMENT 'Số tiền yêu cầu riêng cho hộ. NULL → fallback khoan_thu.so_tien.';
