-- Migration: Thêm bảng lich_su_email (lịch sử email đã gửi)
-- Chạy file này nếu DB đã tồn tại và không muốn reset toàn bộ schema

USE bluemoon;

CREATE TABLE IF NOT EXISTS lich_su_email (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    to_email      VARCHAR(255)  NOT NULL,
    subject       VARCHAR(500)  NULL,
    body          TEXT          NULL,
    loai_email    VARCHAR(50)   NULL,
    trang_thai    VARCHAR(20)   NOT NULL,
    error_message TEXT          NULL,
    ngay_gui      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    so_can_ho     VARCHAR(20)   NULL,
    nguoi_gui     VARCHAR(100)  NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_email_ngay_gui  ON lich_su_email(ngay_gui);
CREATE INDEX idx_email_so_can_ho ON lich_su_email(so_can_ho);
