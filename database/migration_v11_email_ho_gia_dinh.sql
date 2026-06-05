-- v11: thêm cột email cho bảng ho_gia_dinh (dùng để gửi thông báo khoản thu)
ALTER TABLE ho_gia_dinh ADD COLUMN email VARCHAR(255) NULL;
