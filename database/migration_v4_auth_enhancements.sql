USE bluemoon;

-- Thêm cột active vào nguoi_dung (mặc định TRUE — tất cả tài khoản hiện có vẫn hoạt động)
ALTER TABLE nguoi_dung
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE AFTER vai_tro;
