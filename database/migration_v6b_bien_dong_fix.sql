USE bluemoon;

-- Thêm cột tinh_trang vào nhan_khau nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'bluemoon'
      AND TABLE_NAME   = 'nhan_khau'
      AND COLUMN_NAME  = 'tinh_trang'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE nhan_khau ADD COLUMN tinh_trang VARCHAR(30) NOT NULL DEFAULT ''THUONG_TRU'' AFTER quan_he_chu_ho',
    'SELECT ''tinh_trang da ton tai, bo qua'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tạo bảng bien_dong nếu chưa có
CREATE TABLE IF NOT EXISTS bien_dong (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    id_nhan_khau    INT NOT NULL,
    loai_bien_dong  VARCHAR(30) NOT NULL,
    ngay_bien_dong  DATE NOT NULL,
    ghi_chu         VARCHAR(255),
    ngay_tao        DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bd_nk FOREIGN KEY (id_nhan_khau)
        REFERENCES nhan_khau(id) ON DELETE CASCADE
);
