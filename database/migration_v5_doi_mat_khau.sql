USE bluemoon;

-- Thêm cột đánh dấu tài khoản mới cần đổi mật khẩu lần đầu
ALTER TABLE nguoi_dung
    ADD COLUMN doi_mat_khau_lan_dau BOOLEAN NOT NULL DEFAULT FALSE AFTER active;
