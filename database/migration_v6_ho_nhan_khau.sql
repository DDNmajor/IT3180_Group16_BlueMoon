USE bluemoon;

ALTER TABLE ho_gia_dinh
    ADD COLUMN tang_khu_vuc VARCHAR(50) NULL AFTER so_dien_thoai;

ALTER TABLE nhan_khau
    ADD COLUMN tinh_trang VARCHAR(30) NOT NULL DEFAULT 'THUONG_TRU' AFTER quan_he_chu_ho;

CREATE TABLE bien_dong (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_nhan_khau INT NOT NULL,
    loai_bien_dong VARCHAR(30) NOT NULL,
    ngay_bien_dong DATE NOT NULL,
    ghi_chu VARCHAR(255),
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bd_nk FOREIGN KEY (id_nhan_khau) REFERENCES nhan_khau(id) ON DELETE CASCADE
);
