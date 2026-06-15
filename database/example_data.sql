-- ============================================================
-- BlueMoon Seed Data
-- Chạy theo thứ tự:
--   mysql -u root -p bluemoon < database/bluemoon_schema.sql
--   mysql -u root -p bluemoon < database/seed_bluemoon.sql
-- KHÔNG đụng bảng nguoi_dung
-- ============================================================

USE bluemoon;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE hoa_don_thu_ho;
TRUNCATE TABLE lich_su_email;
TRUNCATE TABLE thanh_toan;
TRUNCATE TABLE phuong_tien;
TRUNCATE TABLE bien_dong;
TRUNCATE TABLE nhan_khau;
TRUNCATE TABLE khoan_thu;
TRUNCATE TABLE mau_khoan_thu;
TRUNCATE TABLE audit_log;
TRUNCATE TABLE ho_gia_dinh;
TRUNCATE TABLE loai_khoan_thu;
SET FOREIGN_KEY_CHECKS = 1;

-- Lấy staff ID từ nguoi_dung hiện có
SET @staff1 = (SELECT id FROM nguoi_dung WHERE vai_tro = 'staff' ORDER BY id LIMIT 1 OFFSET 0);
SET @staff2 = (SELECT id FROM nguoi_dung WHERE vai_tro = 'staff' ORDER BY id LIMIT 1 OFFSET 1);
SET @staff1 = COALESCE(@staff1, (SELECT id FROM nguoi_dung ORDER BY id LIMIT 1));
SET @staff2 = COALESCE(@staff2, @staff1);

-- ============================================================
-- SECTION 1: LOAI_KHOAN_THU
-- ============================================================
START TRANSACTION;
INSERT INTO loai_khoan_thu (id, ten_loai, loai_ap_dung, mo_ta) VALUES
(1, 'Phí dịch vụ',          'BAT_BUOC_DINH_KY', 'Phí dịch vụ chung cư hàng tháng'),
(2, 'Phí quản lý',          'BAT_BUOC_DINH_KY', 'Phí quản lý tòa nhà hàng tháng'),
(3, 'Phí gửi xe',           'BAT_BUOC_DINH_KY', 'Phí trông giữ xe hàng tháng'),
(4, 'Đóng góp tự nguyện',   'TU_NGUYEN',        'Các khoản đóng góp tự nguyện');
COMMIT;

-- ============================================================
-- SECTION 2: MAU_KHOAN_THU
-- ============================================================
START TRANSACTION;
INSERT INTO mau_khoan_thu (id, ten_mau, ma_mau_prefix, id_loai, so_tien, loai_tinh_phi, don_gia_per_m2, gia_xe_may, gia_oto, don_vi, so_ngay_han_nop, active, ngay_tao) VALUES
(1, 'Phí dịch vụ hàng tháng',  'DV',  1,      0, 'PER_M2',      7000,  NULL,   NULL,   'm²',    15, TRUE, '2026-01-01 00:00:00'),
(2, 'Phí quản lý hàng tháng',  'QL',  2, 200000, 'FIXED',        NULL,  NULL,   NULL,   'căn hộ',15, TRUE, '2026-01-01 00:00:00'),
(3, 'Phí gửi xe hàng tháng',   'GX',  3,      0, 'PER_XE',       NULL, 70000, 200000,  'xe',    15, TRUE, '2026-01-01 00:00:00'),
(4, 'Phí vệ sinh hàng tháng',  'VS',  1,  20000, 'PER_PERSON',   NULL,  NULL,   NULL,   'người', 15, TRUE, '2026-01-01 00:00:00'),
(5, 'Quỹ từ thiện',            'QTT', 4, 100000, 'FIXED',        NULL,  NULL,   NULL,   'căn hộ',15, TRUE, '2026-01-01 00:00:00');
COMMIT;

-- ============================================================
-- SECTION 3: HO_GIA_DINH (50 hộ)
-- ============================================================
START TRANSACTION;
INSERT INTO ho_gia_dinh (so_can_ho, chu_ho, dien_tich, so_dien_thoai, tang_khu_vuc, email, deleted_at) VALUES
-- Khu A (Tầng 3) — 45–65 m²
('A01', 'Nguyễn Văn Hùng',    48.5,  '0912345601', 'Tầng 3', 'nguyenvanhung.a01@gmail.com',   NULL),
('A02', 'Trần Thị Mai',        50.0,  '0923456702', 'Tầng 3', NULL,                             NULL),
('A03', 'Lê Minh Tuấn',        52.5,  '0934567803', 'Tầng 3', 'leminhtuan.a03@gmail.com',      NULL),
('A04', 'Phạm Thị Hoa',        55.0,  '0945678904', 'Tầng 3', 'phamthihoa.a04@gmail.com',      NULL),
('A05', 'Hoàng Văn Đức',       57.5,  '0956789005', 'Tầng 3', 'hoangvanduc.a05@gmail.com',     NULL),
('A06', 'Vũ Thị Lan',          60.0,  '0967890106', 'Tầng 3', 'vuthilan.a06@gmail.com',        NULL),
('A07', 'Đặng Văn Nam',        62.0,  '0978901207', 'Tầng 3', NULL,                             NULL),
('A08', 'Bùi Thị Thu',         58.5,  '0989012308', 'Tầng 3', 'buithithu.a08@gmail.com',       NULL),
('A09', 'Đỗ Văn Thắng',        63.5,  '0990123409', 'Tầng 3', 'dovanthang.a09@gmail.com',      NULL),
('A10', 'Ngô Thị Phương',      65.0,  '0901234510', 'Tầng 3', 'ngothiphuong.a10@gmail.com',    NULL),
-- Khu B (Tầng 5) — 60–80 m²
('B01', 'Đinh Văn Khoa',       62.0,  '0912345611', 'Tầng 5', 'dinhvankhoa.b01@gmail.com',     NULL),
('B02', 'Trịnh Thị Nhung',     64.5,  '0923456712', 'Tầng 5', NULL,                             NULL),
('B03', 'Lý Văn Tài',          67.0,  '0934567813', 'Tầng 5', 'lyvantai.b03@gmail.com',        NULL),
('B04', 'Phan Thị Dung',       70.0,  '0945678914', 'Tầng 5', 'phan.dung.b04@gmail.com',       NULL),
('B05', 'Cao Văn Bình',        72.5,  '0956789015', 'Tầng 5', 'caovanbinh.b05@gmail.com',      NULL),
('B06', 'Mai Thị Hạnh',        75.0,  '0967890116', 'Tầng 5', 'maithihanh.b06@gmail.com',      NULL),
('B07', 'Tô Văn Long',         77.0,  '0978901217', 'Tầng 5', 'tovanlong.b07@gmail.com',       NULL),
('B08', 'Dương Thị Yến',       79.5,  '0989012318', 'Tầng 5', NULL,                             NULL),
('B09', 'Hà Văn Cường',        68.0,  '0990123419', 'Tầng 5', 'havancuong.b09@gmail.com',      NULL),
('B10', 'Lưu Thị Kim',         80.0,  '0901234520', 'Tầng 5', 'luuthikim.b10@gmail.com',       NULL),
-- Khu C (Tầng 7) — 70–90 m²
('C01', 'Trương Văn Sơn',      72.0,  '0912345621', 'Tầng 7', 'truongvanson.c01@gmail.com',    NULL),
('C02', 'Võ Thị Nga',          75.0,  '0923456722', 'Tầng 7', NULL,                             NULL),
('C03', 'Nguyễn Thị Liên',     78.5,  '0934567823', 'Tầng 7', 'nguyenthilien.c03@gmail.com',   NULL),
('C04', 'Lê Văn Toàn',         80.0,  '0945678924', 'Tầng 7', 'levantoàn.c04@gmail.com',       NULL),
('C05', 'Phạm Văn Hiếu',       82.5,  '0956789025', 'Tầng 7', 'phamvanhieu.c05@gmail.com',     NULL),
('C06', 'Hoàng Thị Tâm',       85.0,  '0967890126', 'Tầng 7', 'hoangthitam.c06@gmail.com',     NULL),
('C07', 'Vũ Văn Dũng',         87.0,  '0978901227', 'Tầng 7', 'vuhandung.c07@gmail.com',       NULL),
('C08', 'Đặng Thị Hương',      89.5,  '0989012328', 'Tầng 7', 'dangthihuong.c08@gmail.com',    NULL),
('C09', 'Bùi Văn Quân',        76.0,  '0990123429', 'Tầng 7', 'buivanquan.c09@gmail.com',      NULL),
('C10', 'Đỗ Thị Thảo',         90.0,  '0901234530', 'Tầng 7', NULL,                             NULL),
-- Khu D (Tầng 9) — 80–100 m²
('D01', 'Ngô Văn Hải',         82.0,  '0912345631', 'Tầng 9', 'ngovanhai.d01@gmail.com',       NULL),
('D02', 'Đinh Thị Vân',        85.0,  '0923456732', 'Tầng 9', 'dinhthivan.d02@gmail.com',      NULL),
('D03', 'Trịnh Văn Minh',      88.5,  '0934567833', 'Tầng 9', 'trinhvanminh.d03@gmail.com',    NULL),
('D04', 'Lý Thị Ánh',          90.0,  '0945678934', 'Tầng 9', NULL,                             NULL),
('D05', 'Phan Văn Khánh',      92.5,  '0956789035', 'Tầng 9', 'phanvankhanh.d05@gmail.com',    NULL),
('D06', 'Cao Thị Bích',        95.0,  '0967890136', 'Tầng 9', 'caothibich.d06@gmail.com',      NULL),
('D07', 'Mai Văn Phúc',        97.0,  '0978901237', 'Tầng 9', 'maivanphuc.d07@gmail.com',      NULL),
('D08', 'Tô Thị Hằng',         99.5,  '0989012338', 'Tầng 9', 'tothihang.d08@gmail.com',       NULL),
('D09', 'Dương Văn Tùng',      86.0,  '0990123439', 'Tầng 9', 'duongvantung.d09@gmail.com',    NULL),
('D10', 'Hà Thị Loan',        100.0,  '0901234540', 'Tầng 9', 'hathiloan.d10@gmail.com',       NULL),
-- Khu E (Tầng 11) — 90–120 m²
('E01', 'Lưu Văn Đạt',         92.0,  '0912345641', 'Tầng 11','luuvandat.e01@gmail.com',       NULL),
('E02', 'Trương Thị Bảo',      95.0,  '0923456742', 'Tầng 11','truongthibao.e02@gmail.com',    NULL),
('E03', 'Võ Văn Thịnh',        98.5,  '0934567843', 'Tầng 11','vovanthinh.e03@gmail.com',      NULL),
('E04', 'Nguyễn Thị Cẩm',     102.0,  '0945678944', 'Tầng 11','nguyenthicam.e04@gmail.com',   NULL),
('E05', 'Lê Văn Phong',        105.5,  '0956789045', 'Tầng 11','levanphong.e05@gmail.com',     NULL),
('E06', 'Phạm Thị Diệu',       108.0,  '0967890146', 'Tầng 11',NULL,                            NULL),
('E07', 'Hoàng Văn Khải',      110.5,  '0978901247', 'Tầng 11','hoangvankhoi.e07@gmail.com',   NULL),
('E08', 'Vũ Thị Ngọc',         114.0,  '0989012348', 'Tầng 11','vuthingoc.e08@gmail.com',      NULL),
('E09', 'Đặng Văn Lộc',        117.5,  '0990123449', 'Tầng 11','dangvanloc.e09@gmail.com',     NULL),
('E10', 'Bùi Thị Thanh',       120.0,  '0901234550', 'Tầng 11','buithithanh.e10@gmail.com',    NULL);
COMMIT;

-- ============================================================
-- SECTION 4: NHAN_KHAU (~180 người)
-- ============================================================
START TRANSACTION;
INSERT INTO nhan_khau (ho_ten, ngay_sinh, gioi_tinh, cccd, so_dien_thoai, quan_he_chu_ho, tinh_trang, id_ho_gia_dinh) VALUES
-- A01 (1 người — độc thân)
('Nguyễn Văn Hùng',    '1988-03-15', 'Nam', '001088003151', '0912345601', 'Chủ hộ',  'THUONG_TRU', 1),
-- A02 (1 người — độc thân)
('Trần Thị Mai',        '1992-07-22', 'Nữ',  '001092007221', '0923456702', 'Chủ hộ',  'THUONG_TRU', 2),
-- A03 (2 người)
('Lê Minh Tuấn',        '1985-11-10', 'Nam', '001085011101', '0934567803', 'Chủ hộ',  'THUONG_TRU', 3),
('Lê Thị Bích',         '1987-04-25', 'Nữ',  '001087004251', '0934567804', 'Vợ',      'THUONG_TRU', 3),
-- A04 (2 người)
('Phạm Thị Hoa',        '1979-06-18', 'Nữ',  '001079006181', '0945678904', 'Chủ hộ',  'THUONG_TRU', 4),
('Phạm Văn Tú',         '1977-09-30', 'Nam', '001077009301', '0945678905', 'Chồng',   'THUONG_TRU', 4),
-- A05 (2 người)
('Hoàng Văn Đức',       '1983-02-14', 'Nam', '001083002141', '0956789005', 'Chủ hộ',  'THUONG_TRU', 5),
('Hoàng Thị Nhung',     '1985-08-20', 'Nữ',  '001085008201', '0956789006', 'Vợ',      'THUONG_TRU', 5),
-- A06 (2 người)
('Vũ Thị Lan',          '1990-12-05', 'Nữ',  '001090012051', '0967890106', 'Chủ hộ',  'THUONG_TRU', 6),
('Vũ Văn Tâm',          '1988-05-17', 'Nam', '001088005171', '0967890107', 'Chồng',   'THUONG_TRU', 6),
-- A07 (1 người — độc thân)
('Đặng Văn Nam',        '1995-03-08', 'Nam', '001095003081', '0978901207', 'Chủ hộ',  'THUONG_TRU', 7),
-- A08 (2 người)
('Bùi Thị Thu',         '1986-10-22', 'Nữ',  '001086010221', '0989012308', 'Chủ hộ',  'THUONG_TRU', 8),
('Bùi Văn Minh',        '1984-07-11', 'Nam', '001084007111', '0989012309', 'Chồng',   'THUONG_TRU', 8),
-- A09 (3 người)
('Đỗ Văn Thắng',        '1980-04-03', 'Nam', '001080004031', '0990123409', 'Chủ hộ',  'THUONG_TRU', 9),
('Nguyễn Thị Hồng',     '1982-09-15', 'Nữ',  '001082009151', '0990123410', 'Vợ',      'THUONG_TRU', 9),
('Đỗ Minh Quân',        '2010-01-20', 'Nam', '001010001201', NULL,          'Con',     'THUONG_TRU', 9),
-- A10 (3 người)
('Ngô Thị Phương',      '1975-11-28', 'Nữ',  '001075011281', '0901234510', 'Chủ hộ',  'THUONG_TRU', 10),
('Ngô Văn Sáng',        '1973-06-14', 'Nam', '001073006141', '0901234511', 'Chồng',   'THUONG_TRU', 10),
('Ngô Thị Linh',        '2005-03-07', 'Nữ',  '001005003071', NULL,          'Con',     'THUONG_TRU', 10),
-- B01 (1 người — độc thân)
('Đinh Văn Khoa',       '1993-08-19', 'Nam', '001093008191', '0912345611', 'Chủ hộ',  'THUONG_TRU', 11),
-- B02 (1 người — độc thân)
('Trịnh Thị Nhung',     '1991-02-27', 'Nữ',  '001091002271', '0923456712', 'Chủ hộ',  'THUONG_TRU', 12),
-- B03 (3 người)
('Lý Văn Tài',          '1978-07-04', 'Nam', '001078007041', '0934567813', 'Chủ hộ',  'THUONG_TRU', 13),
('Lý Thị Vân',          '1980-11-16', 'Nữ',  '001080011161', '0934567814', 'Vợ',      'THUONG_TRU', 13),
('Lý Minh Hiếu',        '2008-04-22', 'Nam', '001008004221', NULL,          'Con',     'THUONG_TRU', 13),
-- B04 (3 người)
('Phan Thị Dung',       '1982-05-30', 'Nữ',  '001082005301', '0945678914', 'Chủ hộ',  'THUONG_TRU', 14),
('Phan Văn Lâm',        '1980-12-08', 'Nam', '001080012081', '0945678915', 'Chồng',   'THUONG_TRU', 14),
('Phan Thị Ngân',       '2012-08-14', 'Nữ',  '001012008141', NULL,          'Con',     'THUONG_TRU', 14),
-- B05 (2 người)
('Cao Văn Bình',        '1987-09-25', 'Nam', '001087009251', '0956789015', 'Chủ hộ',  'THUONG_TRU', 15),
('Cao Thị Hải',         '1989-03-12', 'Nữ',  '001089003121', '0956789016', 'Vợ',      'THUONG_TRU', 15),
-- B06 (4 người)
('Mai Thị Hạnh',        '1976-01-19', 'Nữ',  '001076001191', '0967890116', 'Chủ hộ',  'THUONG_TRU', 16),
('Mai Văn Hùng',        '1974-06-23', 'Nam', '001074006231', '0967890117', 'Chồng',   'THUONG_TRU', 16),
('Mai Thị Thùy',        '2002-10-05', 'Nữ',  '001002010051', '0967890118', 'Con',     'THUONG_TRU', 16),
('Mai Văn Quý',         '2006-07-17', 'Nam', '001006007171', NULL,          'Con',     'THUONG_TRU', 16),
-- B07 (4 người)
('Tô Văn Long',         '1981-04-11', 'Nam', '001081004111', '0978901217', 'Chủ hộ',  'THUONG_TRU', 17),
('Tô Thị Phúc',         '1983-08-29', 'Nữ',  '001083008291', '0978901218', 'Vợ',      'THUONG_TRU', 17),
('Tô Minh Khải',        '2007-02-14', 'Nam', '001007002141', NULL,          'Con',     'THUONG_TRU', 17),
('Tô Thị Huyền',        '2009-11-30', 'Nữ',  '001009011301', NULL,          'Con',     'THUONG_TRU', 17),
-- B08 (3 người)
('Dương Thị Yến',       '1984-03-06', 'Nữ',  '001084003061', '0989012318', 'Chủ hộ',  'THUONG_TRU', 18),
('Dương Văn Hoà',       '1982-07-21', 'Nam', '001082007211', '0989012319', 'Chồng',   'TAM_VANG',   18),
('Dương Thị Như',       '2011-05-18', 'Nữ',  '001011005181', NULL,          'Con',     'THUONG_TRU', 18),
-- B09 (4 người)
('Hà Văn Cường',        '1977-10-14', 'Nam', '001077010141', '0990123419', 'Chủ hộ',  'THUONG_TRU', 19),
('Hà Thị Tuyết',        '1979-02-28', 'Nữ',  '001079002281', '0990123420', 'Vợ',      'THUONG_TRU', 19),
('Hà Minh Đức',         '2004-06-10', 'Nam', '001004006101', '0990123421', 'Con',     'THUONG_TRU', 19),
('Hà Thị Kim',          '2008-09-22', 'Nữ',  '001008009221', NULL,          'Con',     'THUONG_TRU', 19),
-- B10 (3 người)
('Lưu Thị Kim',         '1985-12-01', 'Nữ',  '001085012011', '0901234520', 'Chủ hộ',  'THUONG_TRU', 20),
('Lưu Văn Phát',        '1983-04-17', 'Nam', '001083004171', '0901234521', 'Chồng',   'THUONG_TRU', 20),
('Lưu Minh Tú',         '2013-08-09', 'Nam', '001013008091', NULL,          'Con',     'THUONG_TRU', 20),
-- C01 (1 người — độc thân)
('Trương Văn Sơn',      '1990-05-25', 'Nam', '001090005251', '0912345621', 'Chủ hộ',  'THUONG_TRU', 21),
-- C02 (2 người)
('Võ Thị Nga',          '1986-09-13', 'Nữ',  '001086009131', '0923456722', 'Chủ hộ',  'THUONG_TRU', 22),
('Võ Văn Khải',         '1984-01-07', 'Nam', '001084001071', '0923456723', 'Chồng',   'THUONG_TRU', 22),
-- C03 (4 người)
('Nguyễn Thị Liên',     '1974-07-20', 'Nữ',  '001074007201', '0934567823', 'Chủ hộ',  'THUONG_TRU', 23),
('Nguyễn Văn Bảo',      '1972-03-15', 'Nam', '001072003151', '0934567824', 'Chồng',   'THUONG_TRU', 23),
('Nguyễn Thị Quỳnh',    '2000-11-28', 'Nữ',  '001000011281', '0934567825', 'Con',     'THUONG_TRU', 23),
('Nguyễn Văn An',       '2003-06-04', 'Nam', '001003006041', NULL,          'Con',     'THUONG_TRU', 23),
-- C04 (3 người)
('Lê Văn Toàn',         '1979-08-11', 'Nam', '001079008111', '0945678924', 'Chủ hộ',  'THUONG_TRU', 24),
('Lê Thị Ngọc',         '1981-04-30', 'Nữ',  '001081004301', '0945678925', 'Vợ',      'THUONG_TRU', 24),
('Lê Minh Nhật',        '2009-12-19', 'Nam', '001009012191', NULL,          'Con',     'THUONG_TRU', 24),
-- C05 (4 người)
('Phạm Văn Hiếu',       '1976-02-22', 'Nam', '001076002221', '0956789025', 'Chủ hộ',  'THUONG_TRU', 25),
('Phạm Thị Giang',      '1978-10-08', 'Nữ',  '001078010081', '0956789026', 'Vợ',      'THUONG_TRU', 25),
('Phạm Minh Khôi',      '2005-07-14', 'Nam', '001005007141', '0956789027', 'Con',     'TAM_TRU',    25),
('Phạm Thị Diễm',       '2007-03-26', 'Nữ',  '001007003261', NULL,          'Con',     'THUONG_TRU', 25),
-- C06 (3 người)
('Hoàng Thị Tâm',       '1983-06-17', 'Nữ',  '001083006171', '0967890126', 'Chủ hộ',  'THUONG_TRU', 26),
('Hoàng Văn Thái',      '1981-11-05', 'Nam', '001081011051', '0967890127', 'Chồng',   'THUONG_TRU', 26),
('Hoàng Thị Ánh',       '2011-09-23', 'Nữ',  '001011009231', NULL,          'Con',     'THUONG_TRU', 26),
-- C07 (4 người)
('Vũ Văn Dũng',         '1978-04-09', 'Nam', '001078004091', '0978901227', 'Chủ hộ',  'THUONG_TRU', 27),
('Vũ Thị Phượng',       '1980-08-27', 'Nữ',  '001080008271', '0978901228', 'Vợ',      'THUONG_TRU', 27),
('Vũ Minh Hào',         '2004-01-13', 'Nam', '001004001131', '0978901229', 'Con',     'TAM_TRU',    27),
('Vũ Thị Lan',          '2008-05-31', 'Nữ',  '001008005311', NULL,          'Con',     'THUONG_TRU', 27),
-- C08 (4 người)
('Đặng Thị Hương',      '1975-12-14', 'Nữ',  '001075012141', '0989012328', 'Chủ hộ',  'THUONG_TRU', 28),
('Đặng Văn Hải',        '1973-05-29', 'Nam', '001073005291', '0989012329', 'Chồng',   'THUONG_TRU', 28),
('Đặng Minh Phương',    '2001-09-16', 'Nữ',  '001001009161', '0989012330', 'Con',     'THUONG_TRU', 28),
('Đặng Văn Kiên',       '2006-02-07', 'Nam', '001006002071', NULL,          'Con',     'THUONG_TRU', 28),
-- C09 (3 người)
('Bùi Văn Quân',        '1982-07-03', 'Nam', '001082007031', '0990123429', 'Chủ hộ',  'THUONG_TRU', 29),
('Bùi Thị Hoa',         '1984-11-21', 'Nữ',  '001084011211', '0990123430', 'Vợ',      'THUONG_TRU', 29),
('Bùi Minh Tuấn',       '2012-04-10', 'Nam', '001012004101', NULL,          'Con',     'THUONG_TRU', 29),
-- C10 (3 người)
('Đỗ Thị Thảo',         '1987-03-28', 'Nữ',  '001087003281', '0901234530', 'Chủ hộ',  'THUONG_TRU', 30),
('Đỗ Văn Hùng',         '1985-09-15', 'Nam', '001085009151', '0901234531', 'Chồng',   'THUONG_TRU', 30),
('Đỗ Thị Ngân',         '2014-07-02', 'Nữ',  '001014007021', NULL,          'Con',     'THUONG_TRU', 30),
-- D01 (4 người)
('Ngô Văn Hải',         '1977-01-16', 'Nam', '001077001161', '0912345631', 'Chủ hộ',  'THUONG_TRU', 31),
('Ngô Thị Lan',         '1979-06-04', 'Nữ',  '001079006041', '0912345632', 'Vợ',      'THUONG_TRU', 31),
('Ngô Minh Khoa',       '2003-10-20', 'Nam', '001003010201', '0912345633', 'Con',     'TAM_TRU',    31),
('Ngô Thị Bích',        '2007-04-08', 'Nữ',  '001007004081', NULL,          'Con',     'THUONG_TRU', 31),
-- D02 (3 người)
('Đinh Thị Vân',        '1984-08-25', 'Nữ',  '001084008251', '0923456732', 'Chủ hộ',  'THUONG_TRU', 32),
('Đinh Văn Sơn',        '1982-02-13', 'Nam', '001082002131', '0923456733', 'Chồng',   'THUONG_TRU', 32),
('Đinh Minh Anh',       '2010-11-07', 'Nữ',  '001010011071', NULL,          'Con',     'THUONG_TRU', 32),
-- D03 (5 người)
('Trịnh Văn Minh',      '1973-05-19', 'Nam', '001073005191', '0934567833', 'Chủ hộ',  'THUONG_TRU', 33),
('Trịnh Thị Hà',        '1975-10-31', 'Nữ',  '001075010311', '0934567834', 'Vợ',      'THUONG_TRU', 33),
('Trịnh Minh Khải',     '2000-03-14', 'Nam', '001000003141', '0934567835', 'Con',     'THUONG_TRU', 33),
('Trịnh Thị Thư',       '2004-08-26', 'Nữ',  '001004008261', NULL,          'Con',     'THUONG_TRU', 33),
('Trịnh Thị Bà',        '1948-12-10', 'Nữ',  '001048012101', NULL,          'Mẹ',     'THUONG_TRU', 33),
-- D04 (4 người)
('Lý Thị Ánh',          '1980-07-07', 'Nữ',  '001080007071', '0945678934', 'Chủ hộ',  'THUONG_TRU', 34),
('Lý Văn Phong',        '1978-01-23', 'Nam', '001078001231', '0945678935', 'Chồng',   'TAM_VANG',   34),
('Lý Minh Tú',          '2006-05-09', 'Nam', '001006005091', NULL,          'Con',     'THUONG_TRU', 34),
('Lý Thị Phúc',         '2009-10-17', 'Nữ',  '001009010171', NULL,          'Con',     'THUONG_TRU', 34),
-- D05 (4 người)
('Phan Văn Khánh',      '1976-09-14', 'Nam', '001076009141', '0956789035', 'Chủ hộ',  'THUONG_TRU', 35),
('Phan Thị Tú',         '1978-04-02', 'Nữ',  '001078004021', '0956789036', 'Vợ',      'THUONG_TRU', 35),
('Phan Minh Long',      '2004-12-28', 'Nam', '001004012281', '0956789037', 'Con',     'TAM_TRU',    35),
('Phan Thị Yến',        '2008-06-15', 'Nữ',  '001008006151', NULL,          'Con',     'THUONG_TRU', 35),
-- D06 (3 người)
('Cao Thị Bích',        '1985-03-21', 'Nữ',  '001085003211', '0967890136', 'Chủ hộ',  'THUONG_TRU', 36),
('Cao Văn Đông',        '1983-09-08', 'Nam', '001083009081', '0967890137', 'Chồng',   'THUONG_TRU', 36),
('Cao Minh Trang',      '2013-01-30', 'Nữ',  '001013001301', NULL,          'Con',     'THUONG_TRU', 36),
-- D07 (5 người)
('Mai Văn Phúc',        '1974-11-26', 'Nam', '001074011261', '0978901237', 'Chủ hộ',  'THUONG_TRU', 37),
('Mai Thị Linh',        '1976-05-13', 'Nữ',  '001076005131', '0978901238', 'Vợ',      'THUONG_TRU', 37),
('Mai Minh Đạt',        '2002-09-07', 'Nam', '001002009071', '0978901239', 'Con',     'THUONG_TRU', 37),
('Mai Thị Hân',         '2006-03-19', 'Nữ',  '001006003191', NULL,          'Con',     'THUONG_TRU', 37),
('Mai Văn Ông',         '1949-07-04', 'Nam', '001049007041', NULL,          'Cha',     'THUONG_TRU', 37),
-- D08 (4 người)
('Tô Thị Hằng',         '1981-08-17', 'Nữ',  '001081008171', '0989012338', 'Chủ hộ',  'THUONG_TRU', 38),
('Tô Văn Minh',         '1979-02-05', 'Nam', '001079002051', '0989012339', 'Chồng',   'THUONG_TRU', 38),
('Tô Minh Hưng',        '2005-06-23', 'Nam', '001005006231', '0989012340', 'Con',     'TAM_TRU',    38),
('Tô Thị Mai',          '2008-12-11', 'Nữ',  '001008012111', NULL,          'Con',     'THUONG_TRU', 38),
-- D09 (4 người)
('Dương Văn Tùng',      '1978-10-29', 'Nam', '001078010291', '0990123439', 'Chủ hộ',  'THUONG_TRU', 39),
('Dương Thị Oanh',      '1980-04-16', 'Nữ',  '001080004161', '0990123440', 'Vợ',      'THUONG_TRU', 39),
('Dương Minh Lộc',      '2006-08-03', 'Nam', '001006008031', NULL,          'Con',     'THUONG_TRU', 39),
('Dương Thị Xuân',      '2010-01-21', 'Nữ',  '001010001211', NULL,          'Con',     'THUONG_TRU', 39),
-- D10 (3 người)
('Hà Thị Loan',         '1983-06-08', 'Nữ',  '001083006081', '0901234540', 'Chủ hộ',  'THUONG_TRU', 40),
('Hà Văn Bắc',         '1981-11-24', 'Nam', '001081011241', '0901234541', 'Chồng',   'THUONG_TRU', 40),
('Hà Minh Châu',        '2012-03-12', 'Nữ',  '001012003121', NULL,          'Con',     'THUONG_TRU', 40),
-- E01 (5 người)
('Lưu Văn Đạt',         '1972-09-05', 'Nam', '001072009051', '0912345641', 'Chủ hộ',  'THUONG_TRU', 41),
('Lưu Thị Hà',          '1974-03-22', 'Nữ',  '001074003221', '0912345642', 'Vợ',      'THUONG_TRU', 41),
('Lưu Minh Quân',       '1999-07-18', 'Nam', '001099007181', '0912345643', 'Con',     'THUONG_TRU', 41),
('Lưu Thị Nguyệt',      '2003-11-06', 'Nữ',  '001003011061', NULL,          'Con',     'THUONG_TRU', 41),
('Lưu Văn Ông',         '1947-05-14', 'Nam', '001047005141', NULL,          'Cha',     'THUONG_TRU', 41),
-- E02 (5 người)
('Trương Thị Bảo',      '1976-01-30', 'Nữ',  '001076001301', '0923456742', 'Chủ hộ',  'THUONG_TRU', 42),
('Trương Văn Hiển',     '1974-07-17', 'Nam', '001074007171', '0923456743', 'Chồng',   'THUONG_TRU', 42),
('Trương Minh Khoa',    '2001-04-25', 'Nam', '001001004251', '0923456744', 'Con',     'THUONG_TRU', 42),
('Trương Thị Dung',     '2005-09-13', 'Nữ',  '001005009131', NULL,          'Con',     'THUONG_TRU', 42),
('Trương Thị Bà',       '1950-12-01', 'Nữ',  '001050012011', NULL,          'Mẹ',     'TAM_VANG',   42),
-- E03 (6 người)
('Võ Văn Thịnh',        '1971-06-20', 'Nam', '001071006201', '0934567843', 'Chủ hộ',  'THUONG_TRU', 43),
('Võ Thị Huyền',        '1973-12-08', 'Nữ',  '001073012081', '0934567844', 'Vợ',      'THUONG_TRU', 43),
('Võ Minh Tú',          '1998-04-26', 'Nam', '001098004261', '0934567845', 'Con',     'THUONG_TRU', 43),
('Võ Thị Thanh',        '2001-10-14', 'Nữ',  '001001010141', '0934567846', 'Con',     'THUONG_TRU', 43),
('Võ Văn Ông',          '1946-08-02', 'Nam', '001046008021', NULL,          'Cha',     'THUONG_TRU', 43),
('Võ Thị Bà',           '1948-02-19', 'Nữ',  '001048002191', NULL,          'Mẹ',     'THUONG_TRU', 43),
-- E04 (5 người)
('Nguyễn Thị Cẩm',     '1975-10-07', 'Nữ',  '001075010071', '0945678944', 'Chủ hộ',  'THUONG_TRU', 44),
('Nguyễn Văn Phú',      '1973-04-23', 'Nam', '001073004231', '0945678945', 'Chồng',   'THUONG_TRU', 44),
('Nguyễn Minh Đức',     '2000-08-11', 'Nam', '001000008111', '0945678946', 'Con',     'THUONG_TRU', 44),
('Nguyễn Thị Hoa',      '2004-01-29', 'Nữ',  '001004001291', NULL,          'Con',     'THUONG_TRU', 44),
('Nguyễn Thị Bà',       '1950-06-16', 'Nữ',  '001050006161', NULL,          'Mẹ',     'THUONG_TRU', 44),
-- E05 (6 người)
('Lê Văn Phong',        '1970-03-13', 'Nam', '001070003131', '0956789045', 'Chủ hộ',  'THUONG_TRU', 45),
('Lê Thị Nga',          '1972-09-01', 'Nữ',  '001072009011', '0956789046', 'Vợ',      'THUONG_TRU', 45),
('Lê Minh Hiệu',        '1997-05-19', 'Nam', '001097005191', '0956789047', 'Con',     'THUONG_TRU', 45),
('Lê Thị Phương',       '2000-11-07', 'Nữ',  '001000011071', '0956789048', 'Con',     'THUONG_TRU', 45),
('Lê Văn Ông',          '1945-07-25', 'Nam', '001045007251', NULL,          'Cha',     'THUONG_TRU', 45),
('Lê Thị Bà',           '1947-01-12', 'Nữ',  '001047001121', NULL,          'Mẹ',     'TAM_VANG',   45),
-- E06 (5 người)
('Phạm Thị Diệu',       '1977-08-04', 'Nữ',  '001077008041', '0967890146', 'Chủ hộ',  'THUONG_TRU', 46),
('Phạm Văn Vinh',       '1975-02-21', 'Nam', '001075002211', '0967890147', 'Chồng',   'THUONG_TRU', 46),
('Phạm Minh Hùng',      '2002-06-10', 'Nam', '001002006101', '0967890148', 'Con',     'TAM_TRU',    46),
('Phạm Thị Nhã',        '2005-12-27', 'Nữ',  '001005012271', NULL,          'Con',     'THUONG_TRU', 46),
('Phạm Thị Bà',         '1952-04-15', 'Nữ',  '001052004151', NULL,          'Mẹ',     'THUONG_TRU', 46),
-- E07 (6 người)
('Hoàng Văn Khải',      '1969-11-28', 'Nam', '001069011281', '0978901247', 'Chủ hộ',  'THUONG_TRU', 47),
('Hoàng Thị Cúc',       '1971-05-16', 'Nữ',  '001071005161', '0978901248', 'Vợ',      'THUONG_TRU', 47),
('Hoàng Minh Tài',      '1996-09-04', 'Nam', '001096009041', '0978901249', 'Con',     'THUONG_TRU', 47),
('Hoàng Thị Bình',      '1999-03-22', 'Nữ',  '001099003221', '0978901250', 'Con',     'THUONG_TRU', 47),
('Hoàng Văn Ông',       '1944-01-10', 'Nam', '001044001101', NULL,          'Cha',     'THUONG_TRU', 47),
('Hoàng Thị Bà',        '1946-07-28', 'Nữ',  '001046007281', NULL,          'Mẹ',     'THUONG_TRU', 47),
-- E08 (5 người)
('Vũ Thị Ngọc',         '1976-04-14', 'Nữ',  '001076004141', '0989012348', 'Chủ hộ',  'THUONG_TRU', 48),
('Vũ Văn Khải',         '1974-10-31', 'Nam', '001074010311', '0989012349', 'Chồng',   'THUONG_TRU', 48),
('Vũ Minh Phát',        '2001-02-18', 'Nam', '001001002181', '0989012350', 'Con',     'THUONG_TRU', 48),
('Vũ Thị Loan',         '2004-07-06', 'Nữ',  '001004007061', NULL,          'Con',     'THUONG_TRU', 48),
('Vũ Thị Bà',           '1951-11-23', 'Nữ',  '001051011231', NULL,          'Mẹ',     'TAM_VANG',   48),
-- E09 (6 người)
('Đặng Văn Lộc',        '1970-08-09', 'Nam', '001070008091', '0990123449', 'Chủ hộ',  'THUONG_TRU', 49),
('Đặng Thị Mỹ',         '1972-02-26', 'Nữ',  '001072002261', '0990123450', 'Vợ',      'THUONG_TRU', 49),
('Đặng Minh Sơn',       '1997-06-14', 'Nam', '001097006141', '0990123451', 'Con',     'THUONG_TRU', 49),
('Đặng Thị Hà',         '2000-12-02', 'Nữ',  '001000012021', '0990123452', 'Con',     'THUONG_TRU', 49),
('Đặng Văn Ông',        '1945-04-20', 'Nam', '001045004201', NULL,          'Cha',     'THUONG_TRU', 49),
('Đặng Thị Bà',         '1947-10-07', 'Nữ',  '001047010071', NULL,          'Mẹ',     'THUONG_TRU', 49),
-- E10 (5 người)
('Bùi Thị Thanh',       '1975-07-15', 'Nữ',  '001075007151', '0901234550', 'Chủ hộ',  'THUONG_TRU', 50),
('Bùi Văn Hưng',        '1973-01-03', 'Nam', '001073001031', '0901234551', 'Chồng',   'THUONG_TRU', 50),
('Bùi Minh Toàn',       '1999-05-21', 'Nam', '001099005211', '0901234552', 'Con',     'THUONG_TRU', 50),
('Bùi Thị Trang',       '2002-11-09', 'Nữ',  '001002011091', NULL,          'Con',     'THUONG_TRU', 50),
('Bùi Thị Bà',          '1950-03-27', 'Nữ',  '001050003271', NULL,          'Mẹ',     'THUONG_TRU', 50);
COMMIT;

-- ============================================================
-- SECTION 5: PHUONG_TIEN
-- ============================================================
START TRANSACTION;
INSERT INTO phuong_tien (loai_xe, bien_so, id_ho) VALUES
-- A01 (1 người) — 1 xe máy
('XEMAY', '29A-12301', 1),
-- A02 (1 người) — 1 xe máy
('XEMAY', '29A-12302', 2),
-- A03 (2 người) — 2 xe máy
('XEMAY', '29A-12303', 3), ('XEMAY', '30A-12303', 3),
-- A04 (2 người) — 1 xe máy
('XEMAY', '29A-12304', 4),
-- A05 (2 người) — 2 xe máy
('XEMAY', '29A-12305', 5), ('XEMAY', '30A-12305', 5),
-- A06 (2 người) — 1 xe máy
('XEMAY', '29A-12306', 6),
-- A07 (1 người) — 1 xe máy
('XEMAY', '29A-12307', 7),
-- A08 (2 người) — 2 xe máy
('XEMAY', '29A-12308', 8), ('XEMAY', '30A-12308', 8),
-- A09 (3 người) — 2 xe máy
('XEMAY', '29A-12309', 9), ('XEMAY', '30A-12309', 9),
-- A10 (3 người) — 2 xe máy, 1 ô tô
('XEMAY', '29A-12310', 10), ('XEMAY', '30A-12310', 10), ('OTO', '51G-12310', 10),
-- B01 (1 người) — 1 xe máy
('XEMAY', '29A-22301', 11),
-- B02 (1 người) — không xe
-- B03 (3 người) — 2 xe máy
('XEMAY', '29A-22303', 13), ('XEMAY', '30A-22303', 13),
-- B04 (3 người) — 2 xe máy, 1 ô tô
('XEMAY', '29A-22304', 14), ('XEMAY', '30A-22304', 14), ('OTO', '51G-22304', 14),
-- B05 (2 người) — 2 xe máy
('XEMAY', '29A-22305', 15), ('XEMAY', '30A-22305', 15),
-- B06 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-22306', 16), ('XEMAY', '30A-22306', 16), ('XEMAY', '29A-22316', 16), ('OTO', '51G-22306', 16),
-- B07 (4 người) — 2 xe máy, 1 ô tô
('XEMAY', '29A-22307', 17), ('XEMAY', '30A-22307', 17), ('OTO', '30A-22317', 17),
-- B08 (3 người) — 2 xe máy
('XEMAY', '29A-22308', 18), ('XEMAY', '30A-22308', 18),
-- B09 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-22309', 19), ('XEMAY', '30A-22309', 19), ('XEMAY', '29A-22319', 19), ('OTO', '51G-22309', 19),
-- B10 (3 người) — 2 xe máy
('XEMAY', '29A-22310', 20), ('XEMAY', '30A-22310', 20),
-- C01 (1 người) — 1 xe máy
('XEMAY', '29A-32301', 21),
-- C02 (2 người) — 2 xe máy
('XEMAY', '29A-32302', 22), ('XEMAY', '30A-32302', 22),
-- C03 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-32303', 23), ('XEMAY', '30A-32303', 23), ('XEMAY', '29A-32313', 23), ('OTO', '51G-32303', 23),
-- C04 (3 người) — 2 xe máy
('XEMAY', '29A-32304', 24), ('XEMAY', '30A-32304', 24),
-- C05 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-32305', 25), ('XEMAY', '30A-32305', 25), ('XEMAY', '29A-32315', 25), ('OTO', '51G-32305', 25),
-- C06 (3 người) — 2 xe máy
('XEMAY', '29A-32306', 26), ('XEMAY', '30A-32306', 26),
-- C07 (4 người) — 2 xe máy, 1 ô tô
('XEMAY', '29A-32307', 27), ('XEMAY', '30A-32307', 27), ('OTO', '30A-32317', 27),
-- C08 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-32308', 28), ('XEMAY', '30A-32308', 28), ('XEMAY', '29A-32318', 28), ('OTO', '51G-32308', 28),
-- C09 (3 người) — 2 xe máy
('XEMAY', '29A-32309', 29), ('XEMAY', '30A-32309', 29),
-- C10 (3 người) — 2 xe máy
('XEMAY', '29A-32310', 30), ('XEMAY', '30A-32310', 30),
-- D01 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-42301', 31), ('XEMAY', '30A-42301', 31), ('XEMAY', '29A-42311', 31), ('OTO', '51G-42301', 31),
-- D02 (3 người) — 2 xe máy
('XEMAY', '29A-42302', 32), ('XEMAY', '30A-42302', 32),
-- D03 (5 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-42303', 33), ('XEMAY', '30A-42303', 33), ('XEMAY', '29A-42313', 33), ('OTO', '51G-42303', 33),
-- D04 (4 người) — 2 xe máy, 1 ô tô
('XEMAY', '29A-42304', 34), ('XEMAY', '30A-42304', 34), ('OTO', '51G-42304', 34),
-- D05 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-42305', 35), ('XEMAY', '30A-42305', 35), ('XEMAY', '29A-42315', 35), ('OTO', '30A-42315', 35),
-- D06 (3 người) — 2 xe máy
('XEMAY', '29A-42306', 36), ('XEMAY', '30A-42306', 36),
-- D07 (5 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-42307', 37), ('XEMAY', '30A-42307', 37), ('XEMAY', '29A-42317', 37), ('OTO', '51G-42307', 37),
-- D08 (4 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-42308', 38), ('XEMAY', '30A-42308', 38), ('XEMAY', '29A-42318', 38), ('OTO', '51G-42308', 38),
-- D09 (4 người) — 2 xe máy, 1 ô tô
('XEMAY', '29A-42309', 39), ('XEMAY', '30A-42309', 39), ('OTO', '51G-42309', 39),
-- D10 (3 người) — 2 xe máy
('XEMAY', '29A-42310', 40), ('XEMAY', '30A-42310', 40),
-- E01 (5 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-52301', 41), ('XEMAY', '30A-52301', 41), ('XEMAY', '29A-52311', 41), ('OTO', '51G-52301', 41),
-- E02 (5 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-52302', 42), ('XEMAY', '30A-52302', 42), ('XEMAY', '29A-52312', 42), ('OTO', '51G-52302', 42),
-- E03 (6 người) — 4 xe máy, 1 ô tô
('XEMAY', '29A-52303', 43), ('XEMAY', '30A-52303', 43), ('XEMAY', '29A-52313', 43), ('XEMAY', '30A-52313', 43), ('OTO', '51G-52303', 43),
-- E04 (5 người) — 3 xe máy, 2 ô tô
('XEMAY', '29A-52304', 44), ('XEMAY', '30A-52304', 44), ('XEMAY', '29A-52314', 44), ('OTO', '51G-52304', 44), ('OTO', '30A-52314', 44),
-- E05 (6 người) — 4 xe máy, 2 ô tô
('XEMAY', '29A-52305', 45), ('XEMAY', '30A-52305', 45), ('XEMAY', '29A-52315', 45), ('XEMAY', '30A-52315', 45), ('OTO', '51G-52305', 45), ('OTO', '30A-52325', 45),
-- E06 (5 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-52306', 46), ('XEMAY', '30A-52306', 46), ('XEMAY', '29A-52316', 46), ('OTO', '51G-52306', 46),
-- E07 (6 người) — 4 xe máy, 2 ô tô
('XEMAY', '29A-52307', 47), ('XEMAY', '30A-52307', 47), ('XEMAY', '29A-52317', 47), ('XEMAY', '30A-52317', 47), ('OTO', '51G-52307', 47), ('OTO', '30A-52327', 47),
-- E08 (5 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-52308', 48), ('XEMAY', '30A-52308', 48), ('XEMAY', '29A-52318', 48), ('OTO', '51G-52308', 48),
-- E09 (6 người) — 4 xe máy, 2 ô tô
('XEMAY', '29A-52309', 49), ('XEMAY', '30A-52309', 49), ('XEMAY', '29A-52319', 49), ('XEMAY', '30A-52319', 49), ('OTO', '51G-52309', 49), ('OTO', '30A-52329', 49),
-- E10 (5 người) — 3 xe máy, 1 ô tô
('XEMAY', '29A-52310', 50), ('XEMAY', '30A-52310', 50), ('XEMAY', '29A-52320', 50), ('OTO', '51G-52310', 50);
COMMIT;

-- ============================================================
-- SECTION 6: KHOAN_THU (5 mẫu × 3 tháng = 15 khoản)
-- ============================================================
START TRANSACTION;
INSERT INTO khoan_thu (ma_khoan_thu, ten_khoan_thu, id_loai, so_tien, don_gia_per_m2, gia_xe_may, gia_oto, loai_tinh_phi, don_vi, ky_thu, han_nop, id_mau) VALUES
-- Tháng 4
('DV-2026-04', 'Phí dịch vụ tháng 4/2026',   1,      0, 7000,  NULL,   NULL,   'PER_M2',     'm²',     '2026-04-01', '2026-04-15', 1),
('QL-2026-04', 'Phí quản lý tháng 4/2026',    2, 200000, NULL,  NULL,   NULL,   'FIXED',      'căn hộ', '2026-04-01', '2026-04-15', 2),
('GX-2026-04', 'Phí gửi xe tháng 4/2026',     3,      0, NULL,  70000, 200000,  'PER_XE',     'xe',     '2026-04-01', '2026-04-15', 3),
('VS-2026-04', 'Phí vệ sinh tháng 4/2026',    1,  20000, NULL,  NULL,   NULL,   'PER_PERSON', 'người',  '2026-04-01', '2026-04-15', 4),
('QTT-2026-04','Quỹ từ thiện tháng 4/2026',   4, 100000, NULL,  NULL,   NULL,   'FIXED',      'căn hộ', '2026-04-01', '2026-04-15', 5),
-- Tháng 5
('DV-2026-05', 'Phí dịch vụ tháng 5/2026',   1,      0, 7000,  NULL,   NULL,   'PER_M2',     'm²',     '2026-05-01', '2026-05-15', 1),
('QL-2026-05', 'Phí quản lý tháng 5/2026',    2, 200000, NULL,  NULL,   NULL,   'FIXED',      'căn hộ', '2026-05-01', '2026-05-15', 2),
('GX-2026-05', 'Phí gửi xe tháng 5/2026',     3,      0, NULL,  70000, 200000,  'PER_XE',     'xe',     '2026-05-01', '2026-05-15', 3),
('VS-2026-05', 'Phí vệ sinh tháng 5/2026',    1,  20000, NULL,  NULL,   NULL,   'PER_PERSON', 'người',  '2026-05-01', '2026-05-15', 4),
('QTT-2026-05','Quỹ từ thiện tháng 5/2026',   4, 100000, NULL,  NULL,   NULL,   'FIXED',      'căn hộ', '2026-05-01', '2026-05-15', 5),
-- Tháng 6
('DV-2026-06', 'Phí dịch vụ tháng 6/2026',   1,      0, 7000,  NULL,   NULL,   'PER_M2',     'm²',     '2026-06-01', '2026-06-15', 1),
('QL-2026-06', 'Phí quản lý tháng 6/2026',    2, 200000, NULL,  NULL,   NULL,   'FIXED',      'căn hộ', '2026-06-01', '2026-06-15', 2),
('GX-2026-06', 'Phí gửi xe tháng 6/2026',     3,      0, NULL,  70000, 200000,  'PER_XE',     'xe',     '2026-06-01', '2026-06-15', 3),
('VS-2026-06', 'Phí vệ sinh tháng 6/2026',    1,  20000, NULL,  NULL,   NULL,   'PER_PERSON', 'người',  '2026-06-01', '2026-06-15', 4),
('QTT-2026-06','Quỹ từ thiện tháng 6/2026',   4, 100000, NULL,  NULL,   NULL,   'FIXED',      'căn hộ', '2026-06-01', '2026-06-15', 5);
COMMIT;

-- ============================================================
-- SECTION 7: THANH_TOAN
-- Công thức so_tien_yeu_cau:
--   DV (PER_M2):     dien_tich * 7000
--   QL (FIXED):      200000
--   GX (PER_XE):     so_xemay*70000 + so_oto*200000
--   VS (PER_PERSON): so_nhan_khau_tinh_phi * 20000
--   QTT (FIXED):     100000
-- Tháng 4: hộ 1–45 DA_DONG, hộ 46–50 CON_NO
-- Tháng 5: hộ 1–37 DA_DONG, hộ 38–50 CON_NO
-- Tháng 6: hộ 1–20 DA_DONG, hộ 21–50 không tạo record
-- ============================================================
START TRANSACTION;
-- Bảng tham chiếu nhanh (so_can_ho: dien_tich | xemay | oto | nk_tinh_phi):
-- A01:48.5|1|0|1  A02:50.0|1|0|1  A03:52.5|2|0|2  A04:55.0|1|0|2  A05:57.5|2|0|2
-- A06:60.0|1|0|2  A07:62.0|1|0|1  A08:58.5|2|0|2  A09:63.5|2|0|3  A10:65.0|2|1|3
-- B01:62.0|1|0|1  B02:64.5|0|0|1  B03:67.0|2|0|3  B04:70.0|2|1|3  B05:72.5|2|0|2
-- B06:75.0|3|1|4  B07:77.0|2|1|4  B08:79.5|2|0|3  B09:68.0|3|1|4  B10:80.0|2|0|3
-- C01:72.0|1|0|1  C02:75.0|2|0|2  C03:78.5|3|1|4  C04:80.0|2|0|3  C05:82.5|3|1|4
-- C06:85.0|2|0|3  C07:87.0|2|1|4  C08:89.5|3|1|4  C09:76.0|2|0|3  C10:90.0|2|0|3
-- D01:82.0|3|1|4  D02:85.0|2|0|3  D03:88.5|3|1|5  D04:90.0|2|1|4  D05:92.5|3|1|4
-- D06:95.0|2|0|3  D07:97.0|3|1|5  D08:99.5|3|1|4  D09:86.0|2|1|4  D10:100.0|2|0|3
-- E01:92.0|3|1|5  E02:95.0|3|1|5  E03:98.5|4|1|6  E04:102.0|3|2|5 E05:105.5|4|2|6
-- E06:108.0|3|1|5 E07:110.5|4|2|6 E08:114.0|3|1|5 E09:117.5|4|2|6 E10:120.0|3|1|5

-- FORMAT: (id_ho, id_khoan_thu, so_tien_da_nop, so_tien_yeu_cau, ngay_nop, nguoi_thu, phuong_thuc, trang_thai)
-- id_khoan_thu: DV04=1,QL04=2,GX04=3,VS04=4,QTT04=5, DV05=6,QL05=7,GX05=8,VS05=9,QTT05=10, DV06=11,QL06=12,GX06=13,VS06=14,QTT06=15

INSERT INTO thanh_toan (id_ho_gia_dinh, id_khoan_thu, so_tien_da_nop, so_tien_yeu_cau, ngay_nop, nguoi_thu, phuong_thuc, trang_thai) VALUES
-- ===== THÁNG 4 (id_khoan_thu 1–5) — DA_DONG hộ 1-45, CON_NO hộ 46-50 =====
-- A01 (dv=339500,ql=200000,gx=70000,vs=20000,qtt=100000)
(1,1,339500,339500,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(1,2,200000,200000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(1,3,70000,70000,'2026-04-09',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(1,4,20000,20000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(1,5,100000,100000,'2026-04-10',@staff2,'TIEN_MAT','DA_DONG'),
-- A02 (dv=350000,ql=200000,gx=70000,vs=20000,qtt=100000)
(2,1,350000,350000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(2,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(2,3,70000,70000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(2,4,20000,20000,'2026-04-08',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(2,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- A03 (dv=367500,ql=200000,gx=140000,vs=40000,qtt=100000)
(3,1,367500,367500,'2026-04-06',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(3,2,200000,200000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(3,3,140000,140000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(3,4,40000,40000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(3,5,100000,100000,'2026-04-08',@staff2,'CHUYEN_KHOAN','DA_DONG'),
-- A04 (dv=385000,ql=200000,gx=70000,vs=40000,qtt=100000)
(4,1,385000,385000,'2026-04-05',@staff2,'TIEN_MAT','DA_DONG'),
(4,2,200000,200000,'2026-04-05',@staff2,'TIEN_MAT','DA_DONG'),
(4,3,70000,70000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(4,4,40000,40000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(4,5,100000,100000,'2026-04-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
-- A05 (dv=402500,ql=200000,gx=140000,vs=40000,qtt=100000)
(5,1,402500,402500,'2026-04-04',@staff1,'TIEN_MAT','DA_DONG'),
(5,2,200000,200000,'2026-04-04',@staff1,'TIEN_MAT','DA_DONG'),
(5,3,140000,140000,'2026-04-05',@staff2,'TIEN_MAT','DA_DONG'),
(5,4,40000,40000,'2026-04-05',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(5,5,100000,100000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
-- A06 (dv=420000,ql=200000,gx=70000,vs=40000,qtt=100000)
(6,1,420000,420000,'2026-04-04',@staff2,'TIEN_MAT','DA_DONG'),
(6,2,200000,200000,'2026-04-04',@staff2,'TIEN_MAT','DA_DONG'),
(6,3,70000,70000,'2026-04-05',@staff1,'TIEN_MAT','DA_DONG'),
(6,4,40000,40000,'2026-04-05',@staff2,'TIEN_MAT','DA_DONG'),
(6,5,100000,100000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
-- A07 (dv=434000,ql=200000,gx=70000,vs=20000,qtt=100000)
(7,1,434000,434000,'2026-04-05',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(7,2,200000,200000,'2026-04-05',@staff1,'TIEN_MAT','DA_DONG'),
(7,3,70000,70000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(7,4,20000,20000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(7,5,100000,100000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
-- A08 (dv=409500,ql=200000,gx=140000,vs=40000,qtt=100000)
(8,1,409500,409500,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(8,2,200000,200000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(8,3,140000,140000,'2026-04-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(8,4,40000,40000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(8,5,100000,100000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
-- A09 (dv=444500,ql=200000,gx=140000,vs=60000,qtt=100000)
(9,1,444500,444500,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(9,2,200000,200000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(9,3,140000,140000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(9,4,60000,60000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(9,5,100000,100000,'2026-04-09',@staff2,'CHUYEN_KHOAN','DA_DONG'),
-- A10 (dv=455000,ql=200000,gx=340000,vs=60000,qtt=100000)
(10,1,455000,455000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(10,2,200000,200000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(10,3,340000,340000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(10,4,60000,60000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(10,5,100000,100000,'2026-04-10',@staff1,'TIEN_MAT','DA_DONG'),
-- B01 (dv=434000,ql=200000,gx=70000,vs=20000,qtt=100000)
(11,1,434000,434000,'2026-04-05',@staff1,'TIEN_MAT','DA_DONG'),
(11,2,200000,200000,'2026-04-05',@staff1,'TIEN_MAT','DA_DONG'),
(11,3,70000,70000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(11,4,20000,20000,'2026-04-06',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(11,5,100000,100000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
-- B02 (dv=451500,ql=200000,gx=0,vs=20000,qtt=100000)
(12,1,451500,451500,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(12,2,200000,200000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(12,3,0,0,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(12,4,20000,20000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(12,5,100000,100000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
-- B03 (dv=469000,ql=200000,gx=140000,vs=60000,qtt=100000)
(13,1,469000,469000,'2026-04-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(13,2,200000,200000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(13,3,140000,140000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(13,4,60000,60000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(13,5,100000,100000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
-- B04 (dv=490000,ql=200000,gx=340000,vs=60000,qtt=100000)
(14,1,490000,490000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(14,2,200000,200000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(14,3,340000,340000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(14,4,60000,60000,'2026-04-09',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(14,5,100000,100000,'2026-04-10',@staff1,'TIEN_MAT','DA_DONG'),
-- B05 (dv=507500,ql=200000,gx=140000,vs=40000,qtt=100000)
(15,1,507500,507500,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(15,2,200000,200000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(15,3,140000,140000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(15,4,40000,40000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(15,5,100000,100000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
-- B06 (dv=525000,ql=200000,gx=410000,vs=80000,qtt=100000)
(16,1,525000,525000,'2026-04-05',@staff2,'TIEN_MAT','DA_DONG'),
(16,2,200000,200000,'2026-04-05',@staff2,'TIEN_MAT','DA_DONG'),
(16,3,410000,410000,'2026-04-06',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(16,4,80000,80000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(16,5,100000,100000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
-- B07 (dv=539000,ql=200000,gx=340000,vs=80000,qtt=100000)
(17,1,539000,539000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(17,2,200000,200000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(17,3,340000,340000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(17,4,80000,80000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(17,5,100000,100000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
-- B08 (dv=556500,ql=200000,gx=140000,vs=60000,qtt=100000)
(18,1,556500,556500,'2026-04-07',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(18,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(18,3,140000,140000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(18,4,60000,60000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(18,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- B09 (dv=476000,ql=200000,gx=410000,vs=80000,qtt=100000)
(19,1,476000,476000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(19,2,200000,200000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(19,3,410000,410000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(19,4,80000,80000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(19,5,100000,100000,'2026-04-10',@staff2,'CHUYEN_KHOAN','DA_DONG'),
-- B10 (dv=560000,ql=200000,gx=140000,vs=60000,qtt=100000)
(20,1,560000,560000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(20,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(20,3,140000,140000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(20,4,60000,60000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(20,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- C01 (dv=504000,ql=200000,gx=70000,vs=20000,qtt=100000)
(21,1,504000,504000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(21,2,200000,200000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(21,3,70000,70000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(21,4,20000,20000,'2026-04-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(21,5,100000,100000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
-- C02 (dv=525000,ql=200000,gx=140000,vs=40000,qtt=100000)
(22,1,525000,525000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(22,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(22,3,140000,140000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(22,4,40000,40000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(22,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- C03 (dv=549500,ql=200000,gx=410000,vs=80000,qtt=100000)
(23,1,549500,549500,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(23,2,200000,200000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(23,3,410000,410000,'2026-04-09',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(23,4,80000,80000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(23,5,100000,100000,'2026-04-10',@staff2,'TIEN_MAT','DA_DONG'),
-- C04 (dv=560000,ql=200000,gx=140000,vs=60000,qtt=100000)
(24,1,560000,560000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(24,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(24,3,140000,140000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(24,4,60000,60000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(24,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- C05 (dv=577500,ql=200000,gx=410000,vs=80000,qtt=100000)
(25,1,577500,577500,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(25,2,200000,200000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(25,3,410000,410000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(25,4,80000,80000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(25,5,100000,100000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
-- C06 (dv=595000,ql=200000,gx=140000,vs=60000,qtt=100000)
(26,1,595000,595000,'2026-04-07',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(26,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(26,3,140000,140000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(26,4,60000,60000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(26,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- C07 (dv=609000,ql=200000,gx=340000,vs=80000,qtt=100000)
(27,1,609000,609000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(27,2,200000,200000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(27,3,340000,340000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(27,4,80000,80000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(27,5,100000,100000,'2026-04-10',@staff2,'TIEN_MAT','DA_DONG'),
-- C08 (dv=626500,ql=200000,gx=410000,vs=80000,qtt=100000)
(28,1,626500,626500,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(28,2,200000,200000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(28,3,410000,410000,'2026-04-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(28,4,80000,80000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(28,5,100000,100000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
-- C09 (dv=532000,ql=200000,gx=140000,vs=60000,qtt=100000)
(29,1,532000,532000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(29,2,200000,200000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(29,3,140000,140000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(29,4,60000,60000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(29,5,100000,100000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
-- C10 (dv=630000,ql=200000,gx=140000,vs=60000,qtt=100000)
(30,1,630000,630000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(30,2,200000,200000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(30,3,140000,140000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(30,4,60000,60000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(30,5,100000,100000,'2026-04-10',@staff1,'CHUYEN_KHOAN','DA_DONG'),
-- D01 (dv=574000,ql=200000,gx=410000,vs=80000,qtt=100000)
(31,1,574000,574000,'2026-04-05',@staff1,'TIEN_MAT','DA_DONG'),
(31,2,200000,200000,'2026-04-05',@staff1,'TIEN_MAT','DA_DONG'),
(31,3,410000,410000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(31,4,80000,80000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(31,5,100000,100000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
-- D02 (dv=595000,ql=200000,gx=140000,vs=60000,qtt=100000)
(32,1,595000,595000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(32,2,200000,200000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(32,3,140000,140000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(32,4,60000,60000,'2026-04-07',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(32,5,100000,100000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
-- D03 (dv=619500,ql=200000,gx=410000,vs=100000,qtt=100000)
(33,1,619500,619500,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(33,2,200000,200000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(33,3,410000,410000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(33,4,100000,100000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(33,5,100000,100000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
-- D04 (dv=630000,ql=200000,gx=340000,vs=80000,qtt=100000)
(34,1,630000,630000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(34,2,200000,200000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(34,3,340000,340000,'2026-04-09',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(34,4,80000,80000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(34,5,100000,100000,'2026-04-10',@staff1,'TIEN_MAT','DA_DONG'),
-- D05 (dv=647500,ql=200000,gx=410000,vs=80000,qtt=100000)
(35,1,647500,647500,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(35,2,200000,200000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(35,3,410000,410000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(35,4,80000,80000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(35,5,100000,100000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
-- D06 (dv=665000,ql=200000,gx=140000,vs=60000,qtt=100000)
(36,1,665000,665000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(36,2,200000,200000,'2026-04-06',@staff2,'TIEN_MAT','DA_DONG'),
(36,3,140000,140000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(36,4,60000,60000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(36,5,100000,100000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
-- D07 (dv=679000,ql=200000,gx=410000,vs=100000,qtt=100000)
(37,1,679000,679000,'2026-04-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(37,2,200000,200000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(37,3,410000,410000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(37,4,100000,100000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(37,5,100000,100000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
-- D08 (dv=696500,ql=200000,gx=410000,vs=80000,qtt=100000)
(38,1,696500,696500,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(38,2,200000,200000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(38,3,410000,410000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(38,4,80000,80000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(38,5,100000,100000,'2026-04-10',@staff1,'TIEN_MAT','DA_DONG'),
-- D09 (dv=602000,ql=200000,gx=340000,vs=80000,qtt=100000)
(39,1,602000,602000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(39,2,200000,200000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(39,3,340000,340000,'2026-04-08',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(39,4,80000,80000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(39,5,100000,100000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
-- D10 (dv=700000,ql=200000,gx=140000,vs=60000,qtt=100000)
(40,1,700000,700000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(40,2,200000,200000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(40,3,140000,140000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(40,4,60000,60000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(40,5,100000,100000,'2026-04-10',@staff1,'TIEN_MAT','DA_DONG'),
-- E01 (dv=644000,ql=200000,gx=410000,vs=100000,qtt=100000)
(41,1,644000,644000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(41,2,200000,200000,'2026-04-06',@staff1,'TIEN_MAT','DA_DONG'),
(41,3,410000,410000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(41,4,100000,100000,'2026-04-07',@staff1,'TIEN_MAT','DA_DONG'),
(41,5,100000,100000,'2026-04-08',@staff2,'CHUYEN_KHOAN','DA_DONG'),
-- E02 (dv=665000,ql=200000,gx=410000,vs=100000,qtt=100000)
(42,1,665000,665000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(42,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(42,3,410000,410000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(42,4,100000,100000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(42,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- E03 (dv=689500,ql=200000,gx=480000,vs=120000,qtt=100000)
(43,1,689500,689500,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(43,2,200000,200000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(43,3,480000,480000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(43,4,120000,120000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(43,5,100000,100000,'2026-04-10',@staff2,'TIEN_MAT','DA_DONG'),
-- E04 (dv=714000,ql=200000,gx=610000,vs=100000,qtt=100000)
(44,1,714000,714000,'2026-04-07',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(44,2,200000,200000,'2026-04-07',@staff2,'TIEN_MAT','DA_DONG'),
(44,3,610000,610000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(44,4,100000,100000,'2026-04-08',@staff2,'TIEN_MAT','DA_DONG'),
(44,5,100000,100000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
-- E05 (dv=738500,ql=200000,gx=680000,vs=120000,qtt=100000)
(45,1,738500,738500,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(45,2,200000,200000,'2026-04-08',@staff1,'TIEN_MAT','DA_DONG'),
(45,3,680000,680000,'2026-04-09',@staff2,'TIEN_MAT','DA_DONG'),
(45,4,120000,120000,'2026-04-09',@staff1,'TIEN_MAT','DA_DONG'),
(45,5,100000,100000,'2026-04-10',@staff2,'TIEN_MAT','DA_DONG'),
-- E06-E10: CON_NO tháng 4
(46,1,0,756000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(46,2,0,200000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(46,3,0,410000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(46,4,0,100000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(46,5,0,100000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(47,1,0,773500,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(47,2,0,200000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(47,3,0,680000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(47,4,0,120000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(47,5,0,100000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(48,1,0,798000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(48,2,0,200000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(48,3,0,410000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(48,4,0,100000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(48,5,0,100000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(49,1,0,822500,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(49,2,0,200000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(49,3,0,680000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(49,4,0,120000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(49,5,0,100000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(50,1,0,840000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(50,2,0,200000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(50,3,0,410000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO'),
(50,4,0,100000,'2026-04-14',@staff2,'TIEN_MAT','CON_NO'),
(50,5,0,100000,'2026-04-14',@staff1,'TIEN_MAT','CON_NO');
COMMIT;

START TRANSACTION;
-- ===== THÁNG 5 (id_khoan_thu 6–10) — DA_DONG hộ 1-37, CON_NO hộ 38-50 =====
INSERT INTO thanh_toan (id_ho_gia_dinh, id_khoan_thu, so_tien_da_nop, so_tien_yeu_cau, ngay_nop, nguoi_thu, phuong_thuc, trang_thai) VALUES
(1,6,339500,339500,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(1,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(1,8,70000,70000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(1,9,20000,20000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(1,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(2,6,350000,350000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(2,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(2,8,70000,70000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(2,9,20000,20000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(2,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(3,6,367500,367500,'2026-05-05',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(3,7,200000,200000,'2026-05-05',@staff1,'TIEN_MAT','DA_DONG'),
(3,8,140000,140000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(3,9,40000,40000,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(3,10,100000,100000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(4,6,385000,385000,'2026-05-04',@staff2,'TIEN_MAT','DA_DONG'),
(4,7,200000,200000,'2026-05-04',@staff2,'TIEN_MAT','DA_DONG'),
(4,8,70000,70000,'2026-05-05',@staff1,'TIEN_MAT','DA_DONG'),
(4,9,40000,40000,'2026-05-05',@staff2,'TIEN_MAT','DA_DONG'),
(4,10,100000,100000,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(5,6,402500,402500,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(5,7,200000,200000,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(5,8,140000,140000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(5,9,40000,40000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(5,10,100000,100000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(6,6,420000,420000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(6,7,200000,200000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(6,8,70000,70000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(6,9,40000,40000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(6,10,100000,100000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(7,6,434000,434000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(7,7,200000,200000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(7,8,70000,70000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(7,9,20000,20000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(7,10,100000,100000,'2026-05-10',@staff2,'TIEN_MAT','DA_DONG'),
(8,6,409500,409500,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(8,7,200000,200000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(8,8,140000,140000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(8,9,40000,40000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(8,10,100000,100000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(9,6,444500,444500,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(9,7,200000,200000,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(9,8,140000,140000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(9,9,60000,60000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(9,10,100000,100000,'2026-05-08',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(10,6,455000,455000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(10,7,200000,200000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(10,8,340000,340000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(10,9,60000,60000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(10,10,100000,100000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(11,6,434000,434000,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(11,7,200000,200000,'2026-05-06',@staff1,'TIEN_MAT','DA_DONG'),
(11,8,70000,70000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(11,9,20000,20000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(11,10,100000,100000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(12,6,451500,451500,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(12,7,200000,200000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(12,8,0,0,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(12,9,20000,20000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(12,10,100000,100000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(13,6,469000,469000,'2026-05-05',@staff1,'TIEN_MAT','DA_DONG'),
(13,7,200000,200000,'2026-05-05',@staff1,'TIEN_MAT','DA_DONG'),
(13,8,140000,140000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(13,9,60000,60000,'2026-05-06',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(13,10,100000,100000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(14,6,490000,490000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(14,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(14,8,340000,340000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(14,9,60000,60000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(14,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(15,6,507500,507500,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(15,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(15,8,140000,140000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(15,9,40000,40000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(15,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(16,6,525000,525000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(16,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(16,8,410000,410000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(16,9,80000,80000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(16,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(17,6,539000,539000,'2026-05-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(17,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(17,8,340000,340000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(17,9,80000,80000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(17,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(18,6,556500,556500,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(18,7,200000,200000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(18,8,140000,140000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(18,9,60000,60000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(18,10,100000,100000,'2026-05-10',@staff1,'TIEN_MAT','DA_DONG'),
(19,6,476000,476000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(19,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(19,8,410000,410000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(19,9,80000,80000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(19,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(20,6,560000,560000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(20,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(20,8,140000,140000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(20,9,60000,60000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(20,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(21,6,504000,504000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(21,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(21,8,70000,70000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(21,9,20000,20000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(21,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(22,6,525000,525000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(22,7,200000,200000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(22,8,140000,140000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(22,9,40000,40000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(22,10,100000,100000,'2026-05-10',@staff1,'TIEN_MAT','DA_DONG'),
(23,6,549500,549500,'2026-05-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(23,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(23,8,410000,410000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(23,9,80000,80000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(23,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(24,6,560000,560000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(24,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(24,8,140000,140000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(24,9,60000,60000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(24,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(25,6,577500,577500,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(25,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(25,8,410000,410000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(25,9,80000,80000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(25,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(26,6,595000,595000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(26,7,200000,200000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(26,8,140000,140000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(26,9,60000,60000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(26,10,100000,100000,'2026-05-10',@staff1,'TIEN_MAT','DA_DONG'),
(27,6,609000,609000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(27,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(27,8,340000,340000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(27,9,80000,80000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(27,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(28,6,626500,626500,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(28,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(28,8,410000,410000,'2026-05-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(28,9,80000,80000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(28,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(29,6,532000,532000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(29,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(29,8,140000,140000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(29,9,60000,60000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(29,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(30,6,630000,630000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(30,7,200000,200000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(30,8,140000,140000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(30,9,60000,60000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(30,10,100000,100000,'2026-05-10',@staff1,'TIEN_MAT','DA_DONG'),
(31,6,574000,574000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(31,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(31,8,410000,410000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(31,9,80000,80000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(31,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(32,6,595000,595000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(32,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(32,8,140000,140000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(32,9,60000,60000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(32,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(33,6,619500,619500,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(33,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(33,8,410000,410000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(33,9,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(33,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(34,6,630000,630000,'2026-05-08',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(34,7,200000,200000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(34,8,340000,340000,'2026-05-09',@staff1,'TIEN_MAT','DA_DONG'),
(34,9,80000,80000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(34,10,100000,100000,'2026-05-10',@staff1,'TIEN_MAT','DA_DONG'),
(35,6,647500,647500,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(35,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(35,8,410000,410000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(35,9,80000,80000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(35,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
(36,6,665000,665000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(36,7,200000,200000,'2026-05-06',@staff2,'TIEN_MAT','DA_DONG'),
(36,8,140000,140000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(36,9,60000,60000,'2026-05-07',@staff2,'TIEN_MAT','DA_DONG'),
(36,10,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(37,6,679000,679000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(37,7,200000,200000,'2026-05-07',@staff1,'TIEN_MAT','DA_DONG'),
(37,8,410000,410000,'2026-05-08',@staff2,'TIEN_MAT','DA_DONG'),
(37,9,100000,100000,'2026-05-08',@staff1,'TIEN_MAT','DA_DONG'),
(37,10,100000,100000,'2026-05-09',@staff2,'TIEN_MAT','DA_DONG'),
-- CON_NO tháng 5: hộ 38-50
(38,6,0,696500,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(38,7,0,200000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(38,8,0,410000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(38,9,0,80000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(38,10,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(39,6,0,602000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(39,7,0,200000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(39,8,0,340000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(39,9,0,80000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(39,10,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(40,6,0,700000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(40,7,0,200000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(40,8,0,140000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(40,9,0,60000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(40,10,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(41,6,0,644000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(41,7,0,200000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(41,8,0,410000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(41,9,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(41,10,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(42,6,0,665000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(42,7,0,200000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(42,8,0,410000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(42,9,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(42,10,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(43,6,0,689500,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(43,7,0,200000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(43,8,0,480000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(43,9,0,120000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(43,10,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(44,6,0,714000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(44,7,0,200000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(44,8,0,610000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(44,9,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(44,10,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(45,6,0,738500,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(45,7,0,200000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(45,8,0,680000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(45,9,0,120000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(45,10,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(46,6,0,756000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(46,7,0,200000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(46,8,0,410000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(46,9,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(46,10,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(47,6,0,773500,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(47,7,0,200000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(47,8,0,680000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(47,9,0,120000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(47,10,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(48,6,0,798000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(48,7,0,200000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(48,8,0,410000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(48,9,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(48,10,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(49,6,0,822500,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(49,7,0,200000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(49,8,0,680000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(49,9,0,120000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(49,10,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(50,6,0,840000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(50,7,0,200000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(50,8,0,410000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO'),
(50,9,0,100000,'2026-05-14',@staff2,'TIEN_MAT','CON_NO'),
(50,10,0,100000,'2026-05-14',@staff1,'TIEN_MAT','CON_NO');
COMMIT;

START TRANSACTION;
-- ===== THÁNG 6 (id_khoan_thu 11–15) — DA_DONG hộ 1-20 =====
INSERT INTO thanh_toan (id_ho_gia_dinh, id_khoan_thu, so_tien_da_nop, so_tien_yeu_cau, ngay_nop, nguoi_thu, phuong_thuc, trang_thai) VALUES
(1,11,339500,339500,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(1,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(1,13,70000,70000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(1,14,20000,20000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(1,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(2,11,350000,350000,'2026-06-04',@staff2,'TIEN_MAT','DA_DONG'),
(2,12,200000,200000,'2026-06-04',@staff2,'TIEN_MAT','DA_DONG'),
(2,13,70000,70000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(2,14,20000,20000,'2026-06-05',@staff2,'TIEN_MAT','DA_DONG'),
(2,15,100000,100000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(3,11,367500,367500,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(3,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(3,13,140000,140000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(3,14,40000,40000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(3,15,100000,100000,'2026-06-07',@staff2,'CHUYEN_KHOAN','DA_DONG'),
(4,11,385000,385000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(4,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(4,13,70000,70000,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(4,14,40000,40000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(4,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG'),
(5,11,402500,402500,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(5,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(5,13,140000,140000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(5,14,40000,40000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(5,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(6,11,420000,420000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(6,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(6,13,70000,70000,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(6,14,40000,40000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(6,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG'),
(7,11,434000,434000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(7,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(7,13,70000,70000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(7,14,20000,20000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(7,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(8,11,409500,409500,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(8,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(8,13,140000,140000,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(8,14,40000,40000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(8,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG'),
(9,11,444500,444500,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(9,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(9,13,140000,140000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(9,14,60000,60000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(9,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(10,11,455000,455000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(10,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(10,13,340000,340000,'2026-06-07',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(10,14,60000,60000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(10,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG'),
(11,11,434000,434000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(11,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(11,13,70000,70000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(11,14,20000,20000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(11,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(12,11,451500,451500,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(12,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(12,13,0,0,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(12,14,20000,20000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(12,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG'),
(13,11,469000,469000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(13,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(13,13,140000,140000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(13,14,60000,60000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(13,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(14,11,490000,490000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(14,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(14,13,340000,340000,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(14,14,60000,60000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(14,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG'),
(15,11,507500,507500,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(15,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(15,13,140000,140000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(15,14,40000,40000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(15,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(16,11,525000,525000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(16,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(16,13,410000,410000,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(16,14,80000,80000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(16,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG'),
(17,11,539000,539000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(17,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(17,13,340000,340000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(17,14,80000,80000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(17,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(18,11,556500,556500,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(18,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(18,13,140000,140000,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(18,14,60000,60000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(18,15,100000,100000,'2026-06-08',@staff1,'CHUYEN_KHOAN','DA_DONG'),
(19,11,476000,476000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(19,12,200000,200000,'2026-06-05',@staff1,'TIEN_MAT','DA_DONG'),
(19,13,410000,410000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(19,14,80000,80000,'2026-06-06',@staff1,'TIEN_MAT','DA_DONG'),
(19,15,100000,100000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(20,11,560000,560000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(20,12,200000,200000,'2026-06-06',@staff2,'TIEN_MAT','DA_DONG'),
(20,13,140000,140000,'2026-06-07',@staff1,'TIEN_MAT','DA_DONG'),
(20,14,60000,60000,'2026-06-07',@staff2,'TIEN_MAT','DA_DONG'),
(20,15,100000,100000,'2026-06-08',@staff1,'TIEN_MAT','DA_DONG');
-- Hộ 21-50: chưa có record tháng 6 (chưa nộp)
COMMIT;

-- ============================================================
-- SECTION 8: HOA_DON_THU_HO (50 hộ × 4 dịch vụ × 3 tháng = 600 records)
-- Tháng 4: DA_THANH_TOAN tất cả
-- Tháng 5: hộ 1-40 DA_THANH_TOAN, hộ 41-50 CHO_THANH_TOAN
-- Tháng 6: CHO_THANH_TOAN tất cả
-- ============================================================
START TRANSACTION;
INSERT INTO hoa_don_thu_ho (ma_hoa_don, id_ho_gia_dinh, loai_dich_vu, ky_thanh_toan, so_tien, han_thanh_toan, trang_thai, email_da_gui, ngay_tao, ngay_xac_nhan, id_nguoi_xac_nhan) VALUES
-- A01
('HD-DIEN-A01-2026-04',1,'DIEN','2026-04-01',320000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-12 09:00:00',@staff1),
('HD-NUOC-A01-2026-04',1,'NUOC','2026-04-01',85000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-12 09:05:00',@staff1),
('HD-INTERNET-A01-2026-04',1,'INTERNET','2026-04-01',220000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-12 09:10:00',@staff1),
('HD-GAS-A01-2026-04',1,'GAS','2026-04-01',130000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-12 09:15:00',@staff1),
('HD-DIEN-A01-2026-05',1,'DIEN','2026-05-01',295000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-13 09:00:00',@staff2),
('HD-NUOC-A01-2026-05',1,'NUOC','2026-05-01',92000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-13 09:05:00',@staff2),
('HD-INTERNET-A01-2026-05',1,'INTERNET','2026-05-01',220000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-13 09:10:00',@staff2),
('HD-GAS-A01-2026-05',1,'GAS','2026-05-01',145000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-13 09:15:00',@staff2),
('HD-DIEN-A01-2026-06',1,'DIEN','2026-06-01',310000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-A01-2026-06',1,'NUOC','2026-06-01',78000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-A01-2026-06',1,'INTERNET','2026-06-01',220000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-A01-2026-06',1,'GAS','2026-06-01',125000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- A02
('HD-DIEN-A02-2026-04',2,'DIEN','2026-04-01',180000,'2026-04-20','DA_THANH_TOAN',FALSE,'2026-04-01','2026-04-11 10:00:00',@staff1),
('HD-NUOC-A02-2026-04',2,'NUOC','2026-04-01',55000,'2026-04-20','DA_THANH_TOAN',FALSE,'2026-04-01','2026-04-11 10:05:00',@staff1),
('HD-INTERNET-A02-2026-04',2,'INTERNET','2026-04-01',200000,'2026-04-20','DA_THANH_TOAN',FALSE,'2026-04-01','2026-04-11 10:10:00',@staff1),
('HD-GAS-A02-2026-04',2,'GAS','2026-04-01',95000,'2026-04-20','DA_THANH_TOAN',FALSE,'2026-04-01','2026-04-11 10:15:00',@staff1),
('HD-DIEN-A02-2026-05',2,'DIEN','2026-05-01',165000,'2026-05-20','DA_THANH_TOAN',FALSE,'2026-05-01','2026-05-12 10:00:00',@staff1),
('HD-NUOC-A02-2026-05',2,'NUOC','2026-05-01',60000,'2026-05-20','DA_THANH_TOAN',FALSE,'2026-05-01','2026-05-12 10:05:00',@staff1),
('HD-INTERNET-A02-2026-05',2,'INTERNET','2026-05-01',200000,'2026-05-20','DA_THANH_TOAN',FALSE,'2026-05-01','2026-05-12 10:10:00',@staff1),
('HD-GAS-A02-2026-05',2,'GAS','2026-05-01',88000,'2026-05-20','DA_THANH_TOAN',FALSE,'2026-05-01','2026-05-12 10:15:00',@staff1),
('HD-DIEN-A02-2026-06',2,'DIEN','2026-06-01',172000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-A02-2026-06',2,'NUOC','2026-06-01',58000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-A02-2026-06',2,'INTERNET','2026-06-01',200000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-A02-2026-06',2,'GAS','2026-06-01',92000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- A03
('HD-DIEN-A03-2026-04',3,'DIEN','2026-04-01',420000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 11:00:00',@staff2),
('HD-NUOC-A03-2026-04',3,'NUOC','2026-04-01',110000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 11:05:00',@staff2),
('HD-INTERNET-A03-2026-04',3,'INTERNET','2026-04-01',240000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 11:10:00',@staff2),
('HD-GAS-A03-2026-04',3,'GAS','2026-04-01',160000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 11:15:00',@staff2),
('HD-DIEN-A03-2026-05',3,'DIEN','2026-05-01',395000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 11:00:00',@staff1),
('HD-NUOC-A03-2026-05',3,'NUOC','2026-05-01',118000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 11:05:00',@staff1),
('HD-INTERNET-A03-2026-05',3,'INTERNET','2026-05-01',240000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 11:10:00',@staff1),
('HD-GAS-A03-2026-05',3,'GAS','2026-05-01',155000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 11:15:00',@staff1),
('HD-DIEN-A03-2026-06',3,'DIEN','2026-06-01',410000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-A03-2026-06',3,'NUOC','2026-06-01',105000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-A03-2026-06',3,'INTERNET','2026-06-01',240000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-A03-2026-06',3,'GAS','2026-06-01',162000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- A04
('HD-DIEN-A04-2026-04',4,'DIEN','2026-04-01',380000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 09:30:00',@staff1),
('HD-NUOC-A04-2026-04',4,'NUOC','2026-04-01',95000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 09:35:00',@staff1),
('HD-INTERNET-A04-2026-04',4,'INTERNET','2026-04-01',220000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 09:40:00',@staff1),
('HD-GAS-A04-2026-04',4,'GAS','2026-04-01',140000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 09:45:00',@staff1),
('HD-DIEN-A04-2026-05',4,'DIEN','2026-05-01',355000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 09:30:00',@staff2),
('HD-NUOC-A04-2026-05',4,'NUOC','2026-05-01',102000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 09:35:00',@staff2),
('HD-INTERNET-A04-2026-05',4,'INTERNET','2026-05-01',220000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 09:40:00',@staff2),
('HD-GAS-A04-2026-05',4,'GAS','2026-05-01',135000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 09:45:00',@staff2),
('HD-DIEN-A04-2026-06',4,'DIEN','2026-06-01',368000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-A04-2026-06',4,'NUOC','2026-06-01',98000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-A04-2026-06',4,'INTERNET','2026-06-01',220000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-A04-2026-06',4,'GAS','2026-06-01',142000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- A05
('HD-DIEN-A05-2026-04',5,'DIEN','2026-04-01',450000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 14:00:00',@staff2),
('HD-NUOC-A05-2026-04',5,'NUOC','2026-04-01',120000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 14:05:00',@staff2),
('HD-INTERNET-A05-2026-04',5,'INTERNET','2026-04-01',260000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 14:10:00',@staff2),
('HD-GAS-A05-2026-04',5,'GAS','2026-04-01',175000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 14:15:00',@staff2),
('HD-DIEN-A05-2026-05',5,'DIEN','2026-05-01',428000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-09 14:00:00',@staff1),
('HD-NUOC-A05-2026-05',5,'NUOC','2026-05-01',115000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-09 14:05:00',@staff1),
('HD-INTERNET-A05-2026-05',5,'INTERNET','2026-05-01',260000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-09 14:10:00',@staff1),
('HD-GAS-A05-2026-05',5,'GAS','2026-05-01',168000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-09 14:15:00',@staff1),
('HD-DIEN-A05-2026-06',5,'DIEN','2026-06-01',440000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-A05-2026-06',5,'NUOC','2026-06-01',118000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-A05-2026-06',5,'INTERNET','2026-06-01',260000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-A05-2026-06',5,'GAS','2026-06-01',172000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- B01-E10: dùng pattern tương tự, số tiền thay đổi theo quy mô hộ
-- B01
('HD-DIEN-B01-2026-04',11,'DIEN','2026-04-01',280000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-11 08:00:00',@staff1),
('HD-NUOC-B01-2026-04',11,'NUOC','2026-04-01',72000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-11 08:05:00',@staff1),
('HD-INTERNET-B01-2026-04',11,'INTERNET','2026-04-01',220000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-11 08:10:00',@staff1),
('HD-GAS-B01-2026-04',11,'GAS','2026-04-01',110000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-11 08:15:00',@staff1),
('HD-DIEN-B01-2026-05',11,'DIEN','2026-05-01',265000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-12 08:00:00',@staff2),
('HD-NUOC-B01-2026-05',11,'NUOC','2026-05-01',68000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-12 08:05:00',@staff2),
('HD-INTERNET-B01-2026-05',11,'INTERNET','2026-05-01',220000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-12 08:10:00',@staff2),
('HD-GAS-B01-2026-05',11,'GAS','2026-05-01',105000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-12 08:15:00',@staff2),
('HD-DIEN-B01-2026-06',11,'DIEN','2026-06-01',272000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-B01-2026-06',11,'NUOC','2026-06-01',70000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-B01-2026-06',11,'INTERNET','2026-06-01',220000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-B01-2026-06',11,'GAS','2026-06-01',108000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- B06 (4 người — lớn hơn)
('HD-DIEN-B06-2026-04',16,'DIEN','2026-04-01',580000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 15:00:00',@staff1),
('HD-NUOC-B06-2026-04',16,'NUOC','2026-04-01',155000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 15:05:00',@staff1),
('HD-INTERNET-B06-2026-04',16,'INTERNET','2026-04-01',260000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 15:10:00',@staff1),
('HD-GAS-B06-2026-04',16,'GAS','2026-04-01',220000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-10 15:15:00',@staff1),
('HD-DIEN-B06-2026-05',16,'DIEN','2026-05-01',545000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 15:00:00',@staff2),
('HD-NUOC-B06-2026-05',16,'NUOC','2026-05-01',162000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 15:05:00',@staff2),
('HD-INTERNET-B06-2026-05',16,'INTERNET','2026-05-01',260000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 15:10:00',@staff2),
('HD-GAS-B06-2026-05',16,'GAS','2026-05-01',215000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-11 15:15:00',@staff2),
('HD-DIEN-B06-2026-06',16,'DIEN','2026-06-01',560000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-B06-2026-06',16,'NUOC','2026-06-01',158000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-B06-2026-06',16,'INTERNET','2026-06-01',260000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-B06-2026-06',16,'GAS','2026-06-01',218000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- D03 (5 người)
('HD-DIEN-D03-2026-04',33,'DIEN','2026-04-01',650000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 16:00:00',@staff1),
('HD-NUOC-D03-2026-04',33,'NUOC','2026-04-01',175000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 16:05:00',@staff1),
('HD-INTERNET-D03-2026-04',33,'INTERNET','2026-04-01',260000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 16:10:00',@staff1),
('HD-GAS-D03-2026-04',33,'GAS','2026-04-01',240000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-09 16:15:00',@staff1),
('HD-DIEN-D03-2026-05',33,'DIEN','2026-05-01',625000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 16:00:00',@staff2),
('HD-NUOC-D03-2026-05',33,'NUOC','2026-05-01',168000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 16:05:00',@staff2),
('HD-INTERNET-D03-2026-05',33,'INTERNET','2026-05-01',260000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 16:10:00',@staff2),
('HD-GAS-D03-2026-05',33,'GAS','2026-05-01',235000,'2026-05-20','DA_THANH_TOAN',TRUE,'2026-05-01','2026-05-10 16:15:00',@staff2),
('HD-DIEN-D03-2026-06',33,'DIEN','2026-06-01',638000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-D03-2026-06',33,'NUOC','2026-06-01',172000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-D03-2026-06',33,'INTERNET','2026-06-01',260000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-D03-2026-06',33,'GAS','2026-06-01',242000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- E01 (5 người)
('HD-DIEN-E01-2026-04',41,'DIEN','2026-04-01',680000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 09:00:00',@staff2),
('HD-NUOC-E01-2026-04',41,'NUOC','2026-04-01',180000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 09:05:00',@staff2),
('HD-INTERNET-E01-2026-04',41,'INTERNET','2026-04-01',280000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 09:10:00',@staff2),
('HD-GAS-E01-2026-04',41,'GAS','2026-04-01',250000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-08 09:15:00',@staff2),
('HD-DIEN-E01-2026-05',41,'DIEN','2026-05-01',655000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-NUOC-E01-2026-05',41,'NUOC','2026-05-01',172000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-INTERNET-E01-2026-05',41,'INTERNET','2026-05-01',280000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-GAS-E01-2026-05',41,'GAS','2026-05-01',245000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-DIEN-E01-2026-06',41,'DIEN','2026-06-01',668000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-E01-2026-06',41,'NUOC','2026-06-01',176000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-E01-2026-06',41,'INTERNET','2026-06-01',280000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-E01-2026-06',41,'GAS','2026-06-01',248000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
-- E05 (6 người — lớn nhất)
('HD-DIEN-E05-2026-04',45,'DIEN','2026-04-01',745000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-07 10:00:00',@staff1),
('HD-NUOC-E05-2026-04',45,'NUOC','2026-04-01',195000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-07 10:05:00',@staff1),
('HD-INTERNET-E05-2026-04',45,'INTERNET','2026-04-01',280000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-07 10:10:00',@staff1),
('HD-GAS-E05-2026-04',45,'GAS','2026-04-01',295000,'2026-04-20','DA_THANH_TOAN',TRUE,'2026-04-01','2026-04-07 10:15:00',@staff1),
('HD-DIEN-E05-2026-05',45,'DIEN','2026-05-01',720000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-NUOC-E05-2026-05',45,'NUOC','2026-05-01',188000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-INTERNET-E05-2026-05',45,'INTERNET','2026-05-01',280000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-GAS-E05-2026-05',45,'GAS','2026-05-01',288000,'2026-05-20','CHO_THANH_TOAN',TRUE,'2026-05-01',NULL,NULL),
('HD-DIEN-E05-2026-06',45,'DIEN','2026-06-01',732000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-NUOC-E05-2026-06',45,'NUOC','2026-06-01',192000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-INTERNET-E05-2026-06',45,'INTERNET','2026-06-01',280000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL),
('HD-GAS-E05-2026-06',45,'GAS','2026-06-01',292000,'2026-06-20','CHO_THANH_TOAN',FALSE,'2026-06-01',NULL,NULL);
COMMIT;

-- ============================================================
-- DONE
-- Kiểm tra nhanh:
--   SELECT COUNT(*) FROM ho_gia_dinh;    -- 50
--   SELECT COUNT(*) FROM nhan_khau;      -- ~180
--   SELECT COUNT(*) FROM phuong_tien;    -- ~130
--   SELECT COUNT(*) FROM khoan_thu;      -- 15
--   SELECT COUNT(*) FROM thanh_toan;     -- ~525
--   SELECT COUNT(*) FROM hoa_don_thu_ho; -- 156 (sample — không đủ 600)
-- ============================================================