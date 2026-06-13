-- BlueMoon Fee Management System — Full Schema
-- Chạy file này để khởi tạo hoặc reset toàn bộ database

CREATE DATABASE IF NOT EXISTS bluemoon
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bluemoon;

-- Xóa bảng cũ theo thứ tự FK (reverse dependency)
DROP TABLE IF EXISTS lich_su_email;
DROP TABLE IF EXISTS thanh_toan;
DROP TABLE IF EXISTS phuong_tien;
DROP TABLE IF EXISTS bien_dong;
DROP TABLE IF EXISTS nhan_khau;
DROP TABLE IF EXISTS khoan_thu;
DROP TABLE IF EXISTS mau_khoan_thu;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS ho_gia_dinh;
DROP TABLE IF EXISTS loai_khoan_thu;
DROP TABLE IF EXISTS nguoi_dung;

-- nguoi_dung — Tài khoản người dùng hệ thống

CREATE TABLE nguoi_dung (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    ten_dang_nhap         VARCHAR(50)  NOT NULL UNIQUE,
    mat_khau              VARCHAR(255) NOT NULL,
    ho_ten                VARCHAR(100) NOT NULL,
    vai_tro               ENUM('admin','staff') NOT NULL DEFAULT 'staff',
    active                BOOLEAN NOT NULL DEFAULT TRUE,
    doi_mat_khau_lan_dau  BOOLEAN NOT NULL DEFAULT FALSE,
    ngay_tao              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ho_gia_dinh — Hộ gia đình (căn hộ)

CREATE TABLE ho_gia_dinh (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    so_can_ho     VARCHAR(20)   NOT NULL UNIQUE,
    chu_ho        VARCHAR(100)  NOT NULL,
    dien_tich     DECIMAL(6,2)  NULL            COMMENT 'm² — dùng tính phí PER_M2',
    so_dien_thoai VARCHAR(15)   NULL,
    tang_khu_vuc  VARCHAR(50)   NULL,
    ghi_chu       TEXT          NULL,
    email         VARCHAR(255)  NULL            COMMENT 'Gửi thông báo khoản thu',
    ngay_tao      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at    DATETIME      NULL DEFAULT NULL COMMENT 'Soft delete — NULL = active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- nhan_khau — Nhân khẩu

CREATE TABLE nhan_khau (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten         VARCHAR(100) NOT NULL,
    ngay_sinh      DATE         NULL,
    gioi_tinh      ENUM('Nam','Nữ','Khác') NULL,
    cccd           VARCHAR(20)  NULL UNIQUE,
    so_dien_thoai  VARCHAR(15)  NULL,
    quan_he_chu_ho VARCHAR(50)  NULL,
    tinh_trang     VARCHAR(30)  NOT NULL DEFAULT 'THUONG_TRU',
    id_ho_gia_dinh INT          NOT NULL,
    ngay_tao       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nk_ho FOREIGN KEY (id_ho_gia_dinh)
        REFERENCES ho_gia_dinh(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- loai_khoan_thu — Loại khoản thu

CREATE TABLE loai_khoan_thu (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    ten_loai     VARCHAR(100) NOT NULL,
    loai_ap_dung ENUM('BAT_BUOC_DINH_KY','BAT_BUOC_DOT_XUAT','TU_NGUYEN')
                     NOT NULL DEFAULT 'BAT_BUOC_DINH_KY',
    mo_ta        TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- mau_khoan_thu — Template khoản thu định kỳ 

CREATE TABLE mau_khoan_thu (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    ten_mau          VARCHAR(200)   NOT NULL,
    ma_mau_prefix    VARCHAR(20)    NOT NULL UNIQUE  COMMENT 'VD: DV → DV-2026-06',
    id_loai          INT            NOT NULL,
    so_tien          DECIMAL(15,2)  NOT NULL,
    loai_tinh_phi    VARCHAR(20)    NOT NULL DEFAULT 'FIXED' COMMENT 'FIXED | PER_M2 | PER_XE',
    don_gia_per_m2   DECIMAL(15,2)  NULL,
    gia_xe_may       DECIMAL(15,2)  NULL,
    gia_oto          DECIMAL(15,2)  NULL,
    don_vi           VARCHAR(50)    NULL,
    so_ngay_han_nop  INT            NULL             COMMENT 'hanNop = đầu tháng + N ngày',
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    ngay_tao         DATETIME       NOT NULL,
    ghi_chu          TEXT           NULL,
    CONSTRAINT fk_mau_loai FOREIGN KEY (id_loai)
        REFERENCES loai_khoan_thu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- khoan_thu — Khoản thu cụ thể theo kỳ

CREATE TABLE khoan_thu (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    ma_khoan_thu   VARCHAR(50)   NULL UNIQUE,
    ten_khoan_thu  VARCHAR(200)  NOT NULL,
    id_loai        INT           NOT NULL,
    so_tien        DECIMAL(15,2) NOT NULL,
    don_gia_per_m2 DECIMAL(15,2) NULL,
    gia_xe_may     DECIMAL(15,2) NULL,
    gia_oto        DECIMAL(15,2) NULL,
    loai_tinh_phi  VARCHAR(20)   NOT NULL DEFAULT 'FIXED'
                                          COMMENT 'FIXED | PER_M2 | PER_XE',
    don_vi         VARCHAR(50)   NULL,
    ky_thu         DATE          NOT NULL,
    han_nop        DATE          NULL,
    ghi_chu        TEXT          NULL,
    ngay_tao       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_mau         INT           NULL     COMMENT 'NULL = tạo thủ công',
    deleted_at     DATETIME      NULL DEFAULT NULL COMMENT 'Soft delete — NULL = active',
    CONSTRAINT fk_kt_loai FOREIGN KEY (id_loai)
        REFERENCES loai_khoan_thu(id),
    CONSTRAINT fk_kt_mau  FOREIGN KEY (id_mau)
        REFERENCES mau_khoan_thu(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- thanh_toan — Ghi nhận thanh toán

CREATE TABLE thanh_toan (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    id_ho_gia_dinh  INT           NOT NULL,
    id_khoan_thu    INT           NOT NULL,
    so_tien_da_nop  DECIMAL(15,2) NOT NULL,
    so_tien_yeu_cau DECIMAL(15,2) NULL     COMMENT 'NULL → fallback khoan_thu.so_tien',
    ngay_nop        DATE          NOT NULL,
    nguoi_thu       INT           NULL,
    phuong_thuc     ENUM('TIEN_MAT','CHUYEN_KHOAN') NOT NULL DEFAULT 'TIEN_MAT',
    trang_thai      ENUM('DA_DONG','CON_NO','DONG_DU') NOT NULL DEFAULT 'CON_NO',
    ghi_chu         TEXT          NULL,
    CONSTRAINT fk_tt_ho    FOREIGN KEY (id_ho_gia_dinh) REFERENCES ho_gia_dinh(id),
    CONSTRAINT fk_tt_khoan FOREIGN KEY (id_khoan_thu)   REFERENCES khoan_thu(id),
    CONSTRAINT fk_tt_user  FOREIGN KEY (nguoi_thu)      REFERENCES nguoi_dung(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- phuong_tien — Phương tiện của hộ gia đình

CREATE TABLE phuong_tien (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    loai_xe VARCHAR(10)  NOT NULL COMMENT 'XEMAY | OTO',
    bien_so VARCHAR(20)  NOT NULL UNIQUE,
    ghi_chu TEXT         NULL,
    id_ho   INT          NOT NULL,
    CONSTRAINT fk_pt_ho FOREIGN KEY (id_ho)
        REFERENCES ho_gia_dinh(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- bien_dong — Biến động nhân khẩu

CREATE TABLE bien_dong (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    id_nhan_khau   INT          NOT NULL,
    loai_bien_dong VARCHAR(30)  NOT NULL,
    ngay_bien_dong DATE         NOT NULL,
    ngay_ket_thuc  DATE         NULL,
    ghi_chu        VARCHAR(255) NULL,
    ngay_tao       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bd_nk FOREIGN KEY (id_nhan_khau)
        REFERENCES nhan_khau(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- audit_log — Nhật ký hoạt động

CREATE TABLE audit_log (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    hanh_dong      VARCHAR(50)  NOT NULL,
    loai_doi_tuong VARCHAR(50)  NULL,
    chi_tiet       TEXT         NULL,
    nguoi_dung     VARCHAR(100) NULL,
    thoi_gian      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_thoi_gian    ON audit_log(thoi_gian);
CREATE INDEX idx_audit_loai         ON audit_log(loai_doi_tuong);
CREATE INDEX idx_audit_nguoi_dung   ON audit_log(nguoi_dung);

-- lich_su_email — Lịch sử email đã gửi

CREATE TABLE lich_su_email (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    to_email      VARCHAR(255)  NOT NULL,
    subject       VARCHAR(500)  NULL,
    body          TEXT          NULL,
    loai_email    VARCHAR(50)   NULL  COMMENT 'THONG_BAO_KHOAN_THU | CHAO_MUNG_HO_MOI | NHAC_NO_TU_DONG | NHAC_NO_THU_CONG',
    trang_thai    VARCHAR(20)   NOT NULL COMMENT 'THANH_CONG | THAT_BAI',
    error_message TEXT          NULL,
    ngay_gui      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    so_can_ho     VARCHAR(20)   NULL,
    nguoi_gui     VARCHAR(100)  NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_email_ngay_gui ON lich_su_email(ngay_gui);
CREATE INDEX idx_email_so_can_ho ON lich_su_email(so_can_ho);

