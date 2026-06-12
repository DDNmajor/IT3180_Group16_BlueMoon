-- Tạm thời vô hiệu hóa foreign key checks để tránh lỗi nếu clear
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu cũ (tùy chọn)
TRUNCATE TABLE thanh_toan;
TRUNCATE TABLE khoan_thu;
TRUNCATE TABLE loai_khoan_thu;
TRUNCATE TABLE ho_gia_dinh;

SET FOREIGN_KEY_CHECKS = 1;

-- Thêm Hộ gia đình
INSERT INTO ho_gia_dinh (id, chu_ho, dien_tich, email, ghi_chu, so_can_ho, so_dien_thoai, tang_khu_vuc) 
VALUES 
(1, 'Nguyen Van A', 75.5, 'a@gmail.com', '', '101', '0123456789', 'Tầng 1'),
(2, 'Tran Thi B', 80.0, 'b@gmail.com', '', '102', '0987654321', 'Tầng 1'),
(3, 'Le Van C', 65.0, 'c@gmail.com', '', '103', '0111222333', 'Tầng 1');

-- Thêm Loại khoản thu
INSERT INTO loai_khoan_thu (id, loai_ap_dung, ten_loai)
VALUES 
(1, 'BAT_BUOC_DINH_KY', 'Phí quản lý'),
(2, 'TU_NGUYEN', 'Ủng hộ từ thiện');

-- Thêm Khoản thu (giả sử kỳ thu là tháng hiện tại để hiển thị trên màn hình mặc định)
INSERT INTO khoan_thu (id, don_gia_per_m2, han_nop, ky_thu, loai_tinh_phi, ma_khoan_thu, ngay_tao, so_tien, ten_khoan_thu, id_loai)
VALUES 
(1, 6000.00, '2026-06-30', '2026-06-01', 'PER_M2', 'PQL-062026', NOW(), 0, 'Phí quản lý tháng 6/2026', 1),
(2, null, '2026-06-30', '2026-06-01', 'FIXED', 'UNG-062026', NOW(), 100000, 'Quyên góp tháng 6', 2);

-- Thêm Thanh toán
-- Khoản 1: Phí quản lý (Bắt buộc)
-- Hộ 1: 75.5 * 6000 = 453,000 (Đã đóng)
INSERT INTO thanh_toan (ngay_nop, phuong_thuc, so_tien_da_nop, so_tien_yeu_cau, trang_thai, id_ho_gia_dinh, id_khoan_thu, nguoi_thu)
VALUES 
('2026-06-10', 'TIEN_MAT', 453000, 453000, 'DA_DONG', 1, 1, null);

-- Hộ 2: 80.0 * 6000 = 480,000 (Còn nợ)
INSERT INTO thanh_toan (ngay_nop, phuong_thuc, so_tien_da_nop, so_tien_yeu_cau, trang_thai, id_ho_gia_dinh, id_khoan_thu, nguoi_thu)
VALUES 
(null, null, 0, 480000, 'CON_NO', 2, 1, null);

-- Hộ 3: 65.0 * 6000 = 390,000 (Đã đóng một nửa)
INSERT INTO thanh_toan (ngay_nop, phuong_thuc, so_tien_da_nop, so_tien_yeu_cau, trang_thai, id_ho_gia_dinh, id_khoan_thu, nguoi_thu)
VALUES 
('2026-06-11', 'CHUYEN_KHOAN', 190000, 390000, 'CON_NO', 3, 1, null);

-- Khoản 2: Từ thiện (Tự nguyện)
-- Chỉ hộ 1 nộp 100k
INSERT INTO thanh_toan (ngay_nop, phuong_thuc, so_tien_da_nop, so_tien_yeu_cau, trang_thai, id_ho_gia_dinh, id_khoan_thu, nguoi_thu)
VALUES 
('2026-06-11', 'CHUYEN_KHOAN', 100000, 100000, 'DA_DONG', 1, 2, null);
