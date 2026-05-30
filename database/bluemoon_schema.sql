-- BlueMoon Fee Management System
-- MySQL Schema
-- Chạy file này để khởi tạo hoặc reset toàn bộ database

CREATE DATABASE IF NOT EXISTS bluemoon
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bluemoon;

-- ============================================================
-- Xóa bảng cũ (theo thứ tự FK để tránh lỗi)
-- ============================================================
DROP TABLE IF EXISTS thanh_toan;
DROP TABLE IF EXISTS khoan_thu;
DROP TABLE IF EXISTS loai_khoan_thu;
DROP TABLE IF EXISTS nhan_khau;
DROP TABLE IF EXISTS ho_gia_dinh;
DROP TABLE IF EXISTS nguoi_dung;

-- ============================================================
-- nguoi_dung — Tài khoản người dùng hệ thống
-- ============================================================
CREATE TABLE nguoi_dung (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    ten_dang_nhap VARCHAR(50)  NOT NULL UNIQUE,
    mat_khau     VARCHAR(255) NOT NULL,
    ho_ten       VARCHAR(100) NOT NULL,
    vai_tro      ENUM('admin', 'staff') NOT NULL DEFAULT 'staff',
    ngay_tao     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ho_gia_dinh — Hộ gia đình (căn hộ)
-- ============================================================
CREATE TABLE ho_gia_dinh (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    so_can_ho     VARCHAR(20)    NOT NULL UNIQUE,
    chu_ho        VARCHAR(100)   NOT NULL,
    dien_tich     DECIMAL(6,2),
    so_dien_thoai VARCHAR(15),
    ghi_chu       TEXT,
    ngay_tao      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- nhan_khau — Nhân khẩu (thành viên hộ gia đình)
-- ============================================================
CREATE TABLE nhan_khau (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten           VARCHAR(100) NOT NULL,
    ngay_sinh        DATE,
    gioi_tinh        ENUM('Nam', 'Nữ', 'Khác'),
    cccd             VARCHAR(20) UNIQUE,
    so_dien_thoai    VARCHAR(15),
    quan_he_chu_ho   VARCHAR(50),
    id_ho_gia_dinh   INT NOT NULL,
    ngay_tao         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_ho_gia_dinh) REFERENCES ho_gia_dinh(id) ON DELETE CASCADE
);

-- ============================================================
-- loai_khoan_thu — Loại khoản thu (phí dịch vụ, phí quản lý,...)
-- ============================================================
CREATE TABLE loai_khoan_thu (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    ten_loai  VARCHAR(100) NOT NULL,
    mo_ta     TEXT,
    bat_buoc  BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- khoan_thu — Khoản thu cụ thể theo kỳ
-- ============================================================
CREATE TABLE khoan_thu (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    ten_khoan_thu VARCHAR(200)    NOT NULL,
    id_loai       INT            NOT NULL,
    so_tien       DECIMAL(15,2)  NOT NULL,
    don_vi        VARCHAR(50),
    ky_thu        DATE           NOT NULL,
    han_nop       DATE,
    ghi_chu       TEXT,
    ngay_tao      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_loai) REFERENCES loai_khoan_thu(id)
);

-- ============================================================
-- thanh_toan — Ghi nhận thanh toán của hộ gia đình
-- ============================================================
CREATE TABLE thanh_toan (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    id_ho_gia_dinh   INT           NOT NULL,
    id_khoan_thu     INT           NOT NULL,
    so_tien_da_nop   DECIMAL(15,2) NOT NULL,
    ngay_nop         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nguoi_thu        INT,
    ghi_chu          TEXT,
    FOREIGN KEY (id_ho_gia_dinh) REFERENCES ho_gia_dinh(id),
    FOREIGN KEY (id_khoan_thu)   REFERENCES khoan_thu(id),
    FOREIGN KEY (nguoi_thu)      REFERENCES nguoi_dung(id)
);

-- ============================================================
-- Tài khoản admin được tạo tự động khi app khởi động lần đầu
-- thông qua DataInitializer, với mật khẩu từ application.properties
-- (app.admin.password). KHÔNG lưu mật khẩu plaintext ở đây.
-- ============================================================
