USE bluemoon;

ALTER TABLE thanh_toan
    MODIFY COLUMN ngay_nop  DATE NOT NULL,
    ADD COLUMN phuong_thuc  ENUM('TIEN_MAT', 'CHUYEN_KHOAN') NOT NULL DEFAULT 'TIEN_MAT'
        AFTER nguoi_thu,
    ADD COLUMN trang_thai   ENUM('DA_DONG', 'CON_NO', 'DONG_DU') NOT NULL DEFAULT 'DA_DONG'
        AFTER phuong_thuc;
