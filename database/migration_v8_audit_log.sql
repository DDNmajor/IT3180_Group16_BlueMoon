-- v8: Bảng nhật ký hoạt động (audit log)
USE bluemoon;
CREATE TABLE IF NOT EXISTS audit_log (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    hanh_dong       VARCHAR(50)  NOT NULL,
    loai_doi_tuong  VARCHAR(50),
    chi_tiet        TEXT,
    nguoi_dung      VARCHAR(100),
    thoi_gian       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_log_thoi_gian     ON audit_log(thoi_gian);
CREATE INDEX idx_audit_log_loai          ON audit_log(loai_doi_tuong);
CREATE INDEX idx_audit_log_nguoi_dung    ON audit_log(nguoi_dung);
