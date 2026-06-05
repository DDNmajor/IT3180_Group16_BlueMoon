USE bluemoon;

ALTER TABLE bien_dong
    ADD COLUMN ngay_ket_thuc DATE NULL AFTER ngay_bien_dong;
