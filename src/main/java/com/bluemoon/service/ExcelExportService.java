package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.PhuongTienRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final KhoanThuRepository  khoanThuRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final PhuongTienRepository phuongTienRepository;
    private final KhoanThuService     khoanThuService;

    private static final DateTimeFormatter DF  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportBaoCaoThongKe(List<KhoanThu> listKt) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle boldStyle = buildBoldStyle(workbook);
            createSheet1(workbook, listKt, boldStyle);
            createSheet2(workbook, listKt, boldStyle);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ── Sheet 1: Tổng hợp khoản thu ──────────────────────────────────────────

    private void createSheet1(Workbook workbook, List<KhoanThu> listKt, CellStyle boldStyle) {
        Sheet sheet = workbook.createSheet("Thông tin Khoản thu");

        String[] headers = {
                "Tên khoản thu", "Mã khoản thu", "Kỳ thu", "Loại áp dụng",
                "Loại tính phí", "Loại khoản thu", "Số tiền chung (đ)",
                "Đơn giá/m²", "Hạn nộp", "Ngày tạo",
                "Số hộ áp dụng", "Tổng yêu cầu (đ)", "Đã đóng (đ)", "Còn thiếu (đ)"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }

        int rowIndex = 1;
        for (KhoanThu kt : listKt) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(kt.getTenKhoanThu());
            row.createCell(1).setCellValue(kt.getMaKhoanThu() != null ? kt.getMaKhoanThu() : "");
            row.createCell(2).setCellValue(kt.getKyThu() != null ? kt.getKyThu().format(DF) : "");

            String loaiApDung = "";
            if (kt.getLoaiKhoanThu() != null && kt.getLoaiKhoanThu().getLoaiApDung() != null) {
                loaiApDung = kt.getLoaiKhoanThu().getLoaiApDung().getTenHienThi();
            }
            row.createCell(3).setCellValue(loaiApDung);

            String loaiTinhPhi = kt.getLoaiTinhPhi() != null ? kt.getLoaiTinhPhi().getTenHienThi() : "Cố định";
            row.createCell(4).setCellValue(loaiTinhPhi);

            String tenLoai = kt.getLoaiKhoanThu() != null ? kt.getLoaiKhoanThu().getTenLoai() : "";
            row.createCell(5).setCellValue(tenLoai);

            row.createCell(6).setCellValue(kt.getSoTien() != null ? kt.getSoTien().doubleValue() : 0);
            row.createCell(7).setCellValue(kt.getDonGiaPerM2() != null ? kt.getDonGiaPerM2().doubleValue() : 0);
            row.createCell(8).setCellValue(kt.getHanNop() != null ? kt.getHanNop().format(DF) : "");
            row.createCell(9).setCellValue(kt.getNgayTao() != null ? kt.getNgayTao().format(DTF) : "");

            List<ThanhToan> tts = thanhToanRepository.findByKhoanThuIdOrderByNgayNopDesc(kt.getId());

            long soHoApDung = tts.stream()
                    .filter(t -> t.getHoGiaDinh() != null)
                    .map(t -> t.getHoGiaDinh().getId())
                    .distinct()
                    .count();
            row.createCell(10).setCellValue(soHoApDung);

            boolean isTuNguyen = kt.getLoaiKhoanThu() != null
                    && kt.getLoaiKhoanThu().getLoaiApDung() != null
                    && !kt.getLoaiKhoanThu().getLoaiApDung().isBatBuoc();

            BigDecimal tongYeuCau = BigDecimal.ZERO;
            BigDecimal daDong     = BigDecimal.ZERO;
            for (ThanhToan t : tts) {
                if (!isTuNguyen && t.getSoTienYeuCauHieuLuc() != null) {
                    tongYeuCau = tongYeuCau.add(t.getSoTienYeuCauHieuLuc());
                }
                if (t.getSoTienDaNop() != null) {
                    daDong = daDong.add(t.getSoTienDaNop());
                }
            }

            BigDecimal conThieu = tongYeuCau.subtract(daDong);
            if (conThieu.compareTo(BigDecimal.ZERO) < 0) conThieu = BigDecimal.ZERO;

            row.createCell(11).setCellValue(tongYeuCau.doubleValue());
            row.createCell(12).setCellValue(daDong.doubleValue());
            row.createCell(13).setCellValue(conThieu.doubleValue());
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    // ── Sheet 2: Chi tiết từng hộ ─────────────────────────────────────────────

    private void createSheet2(Workbook workbook, List<KhoanThu> listKt, CellStyle boldStyle) {
        Sheet sheet = workbook.createSheet("Chi tiết từng hộ");

        String[] headers = {
                "Số căn hộ", "Chủ hộ", "Tầng/Khu vực", "Email",
                "Số nhân khẩu", "Diện tích (m²)", "Xe máy", "Ô tô",
                "Yêu cầu (Bắt buộc)", "Đã nộp (Bắt buộc)", "Còn thiếu (Bắt buộc)",
                "Đã nộp (Tự nguyện)", "Trạng thái", "Ngày nộp gần nhất"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }

        List<HoGiaDinh> tatCaHo = hoGiaDinhRepository.findAll();

        // Load tất cả ThanhToan cho các KhoanThu trong filter, group theo idHo
        Map<Integer, List<ThanhToan>> ttMapByHo = new HashMap<>();
        for (KhoanThu kt : listKt) {
            for (ThanhToan tt : thanhToanRepository.findByKhoanThuIdOrderByNgayNopDesc(kt.getId())) {
                if (tt.getHoGiaDinh() != null) {
                    ttMapByHo.computeIfAbsent(tt.getHoGiaDinh().getId(), k -> new ArrayList<>()).add(tt);
                }
            }
        }

        // Tạo lookup Set<Integer id KhoanThu> cho từng loại
        Set<Integer> idsBatBuoc = listKt.stream()
                .filter(kt -> kt.getLoaiKhoanThu() != null
                        && kt.getLoaiKhoanThu().getLoaiApDung() != null
                        && kt.getLoaiKhoanThu().getLoaiApDung().isBatBuoc())
                .map(KhoanThu::getId).collect(Collectors.toSet());

        int rowIndex = 1;
        long totalHo = tatCaHo.size();
        long hoDaDong = 0, hoDongDu = 0, hoConNo = 0;
        BigDecimal tongDaThuBB = BigDecimal.ZERO;
        BigDecimal tongConThieuBB = BigDecimal.ZERO;
        BigDecimal tongDaThuTN = BigDecimal.ZERO;

        for (HoGiaDinh ho : tatCaHo) {
            List<ThanhToan> ttList = ttMapByHo.getOrDefault(ho.getId(), Collections.emptyList());

            BigDecimal yeuCauBB = BigDecimal.ZERO;
            BigDecimal daNopBB  = BigDecimal.ZERO;
            BigDecimal daNopTN  = BigDecimal.ZERO;
            LocalDate ngayNopGanNhat = null;

            for (KhoanThu kt : listKt) {
                boolean isBB = idsBatBuoc.contains(kt.getId());

                ThanhToan tt = ttList.stream()
                        .filter(t -> t.getKhoanThu().getId().equals(kt.getId()))
                        .findFirst().orElse(null);

                BigDecimal yc = BigDecimal.ZERO;
                BigDecimal dn = BigDecimal.ZERO;

                if (tt != null) {
                    yc = tt.getSoTienYeuCauHieuLuc();
                    dn = tt.getSoTienDaNop() != null ? tt.getSoTienDaNop() : BigDecimal.ZERO;
                    if (tt.getNgayNop() != null
                            && (ngayNopGanNhat == null || tt.getNgayNop().isAfter(ngayNopGanNhat))) {
                        ngayNopGanNhat = tt.getNgayNop();
                    }
                } else if (isBB) {
                    BigDecimal calc = khoanThuService.tinhSoTienYeuCau(kt, ho);
                    yc = calc != null ? calc : (kt.getSoTien() != null ? kt.getSoTien() : BigDecimal.ZERO);
                }

                if (isBB) {
                    yeuCauBB = yeuCauBB.add(yc);
                    daNopBB  = daNopBB.add(dn);
                } else {
                    daNopTN = daNopTN.add(dn);
                }
            }

            BigDecimal conThieuBB = yeuCauBB.subtract(daNopBB);
            if (conThieuBB.compareTo(BigDecimal.ZERO) < 0) conThieuBB = BigDecimal.ZERO;

            String trangThai;
            if (yeuCauBB.compareTo(BigDecimal.ZERO) == 0) {
                trangThai = "Không có phí bắt buộc";
            } else if (daNopBB.compareTo(yeuCauBB) > 0) {
                trangThai = "Đóng dư";
            } else if (daNopBB.compareTo(yeuCauBB) == 0) {
                trangThai = "Đã đóng đủ";
            } else if (daNopBB.compareTo(BigDecimal.ZERO) > 0) {
                trangThai = "Nộp một phần";
            } else {
                trangThai = "Còn nợ";
            }

            long soXeMay = phuongTienRepository.countByHoGiaDinhIdAndLoaiXe(ho.getId(), LoaiXe.XEMAY);
            long soOto   = phuongTienRepository.countByHoGiaDinhIdAndLoaiXe(ho.getId(), LoaiXe.OTO);

            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(ho.getSoCanHo());
            row.createCell(1).setCellValue(ho.getChuHo());
            row.createCell(2).setCellValue(ho.getTangKhuVuc() != null ? ho.getTangKhuVuc() : "");
            row.createCell(3).setCellValue(ho.getEmail() != null ? ho.getEmail() : "");
            row.createCell(4).setCellValue(ho.getNhanKhaus() != null ? ho.getNhanKhaus().size() : 0);
            row.createCell(5).setCellValue(ho.getDienTich() != null ? ho.getDienTich().doubleValue() : 0);
            row.createCell(6).setCellValue(soXeMay);
            row.createCell(7).setCellValue(soOto);
            row.createCell(8).setCellValue(yeuCauBB.doubleValue());
            row.createCell(9).setCellValue(daNopBB.doubleValue());
            row.createCell(10).setCellValue(conThieuBB.doubleValue());
            row.createCell(11).setCellValue(daNopTN.doubleValue());
            row.createCell(12).setCellValue(trangThai);
            row.createCell(13).setCellValue(ngayNopGanNhat != null ? ngayNopGanNhat.format(DF) : "");

            if ("Đã đóng đủ".equals(trangThai))         hoDaDong++;
            else if ("Đóng dư".equals(trangThai))        hoDongDu++;
            else if ("Còn nợ".equals(trangThai)
                    || "Nộp một phần".equals(trangThai)) hoConNo++;

            tongDaThuBB   = tongDaThuBB.add(daNopBB);
            tongConThieuBB = tongConThieuBB.add(conThieuBB);
            tongDaThuTN   = tongDaThuTN.add(daNopTN);
        }

        // Dòng tổng cộng
        rowIndex++;
        Row sumRow = sheet.createRow(rowIndex);
        sumRow.createCell(0).setCellValue("TỔNG CỘNG");
        sumRow.getCell(0).setCellStyle(boldStyle);
        sumRow.createCell(1).setCellValue(totalHo);
        sumRow.createCell(8).setCellValue(tongDaThuBB.add(tongConThieuBB).doubleValue()); // tổng yêu cầu BB
        sumRow.createCell(9).setCellValue(tongDaThuBB.doubleValue());
        sumRow.createCell(10).setCellValue(tongConThieuBB.doubleValue());
        sumRow.createCell(11).setCellValue(tongDaThuTN.doubleValue());
        sumRow.createCell(12).setCellValue(
                "Đủ: " + hoDaDong + " | Dư: " + hoDongDu + " | Nợ: " + hoConNo);

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private CellStyle buildBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
