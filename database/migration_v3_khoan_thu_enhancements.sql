USE bluemoon;

-- ── Bước 1: Thêm cột loai_ap_dung vào loai_khoan_thu ──────────
ALTER TABLE loai_khoan_thu
    ADD COLUMN loai_ap_dung ENUM('BAT_BUOC_DINH_KY','BAT_BUOC_DOT_XUAT','TU_NGUYEN')
        NOT NULL DEFAULT 'BAT_BUOC_DINH_KY'
        AFTER ten_loai;

-- Migrate dữ liệu cũ từ bat_buoc
SET SQL_SAFE_UPDATES = 0;
UPDATE loai_khoan_thu SET loai_ap_dung = 'TU_NGUYEN'        WHERE bat_buoc = FALSE;
UPDATE loai_khoan_thu SET loai_ap_dung = 'BAT_BUOC_DINH_KY' WHERE bat_buoc = TRUE;
SET SQL_SAFE_UPDATES = 1;

-- Xóa cột bat_buoc cũ
ALTER TABLE loai_khoan_thu DROP COLUMN bat_buoc;

-- ── Bước 2: Thêm cột ma_khoan_thu vào khoan_thu ───────────────
ALTER TABLE khoan_thu
    ADD COLUMN ma_khoan_thu VARCHAR(50) UNIQUE
        AFTER id;
