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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final KhoanThuRepository khoanThuRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final PhuongTienRepository phuongTienRepository;

    public byte[] exportBaoCaoThongKe(List<KhoanThu> listKt) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            createSheet1(workbook, listKt);
            createSheet2(workbook, listKt);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createSheet1(Workbook workbook, List<KhoanThu> listKt) {
        Sheet sheet = workbook.createSheet("Thông tin Khoản thu");
        
        CellStyle boldStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        boldStyle.setFont(font);

        String[] headers = {
                "Tên khoản thu", "Mã khoản thu", "Loại áp dụng", "Loại khoản thu",
                "Số tiền chung", "Đơn giá/m²", "Hạn nộp", "Ngày tạo"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int rowIndex = 1;
        for (KhoanThu kt : listKt) {
            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(kt.getTenKhoanThu());
            dataRow.createCell(1).setCellValue(kt.getMaKhoanThu());
            
            String loaiApDung = kt.getLoaiKhoanThu() != null && kt.getLoaiKhoanThu().getLoaiApDung() != null 
                    ? kt.getLoaiKhoanThu().getLoaiApDung().name() : "";
            dataRow.createCell(2).setCellValue(loaiApDung);
            
            String tenLoai = kt.getLoaiKhoanThu() != null ? kt.getLoaiKhoanThu().getTenLoai() : "";
            dataRow.createCell(3).setCellValue(tenLoai);
            
            dataRow.createCell(4).setCellValue(kt.getSoTien() != null ? kt.getSoTien().doubleValue() : 0);
            dataRow.createCell(5).setCellValue(kt.getDonGiaPerM2() != null ? kt.getDonGiaPerM2().doubleValue() : 0);
            dataRow.createCell(6).setCellValue(kt.getHanNop() != null ? kt.getHanNop().format(df) : "");
            dataRow.createCell(7).setCellValue(kt.getNgayTao() != null ? kt.getNgayTao().format(dtf) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSheet2(Workbook workbook, List<KhoanThu> listKt) {
        Sheet sheet = workbook.createSheet("Chi tiết từng hộ");
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        String[] headers = {
                "Số căn hộ", "Chủ hộ", "Số nhân khẩu", "Diện tích (m²)",
                "Yêu cầu (Bắt buộc)", "Đã nộp (Bắt buộc)", "Còn thiếu (Bắt buộc)", "Đã nộp (Tự nguyện)",
                "Trạng thái (Bắt buộc)", "Ngày nộp gần nhất"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<HoGiaDinh> tatCaHo = hoGiaDinhRepository.findAll();
        
        Map<Integer, List<ThanhToan>> ttMapByHo = new HashMap<>();
        for (KhoanThu kt : listKt) {
            List<ThanhToan> thanhToans = thanhToanRepository.findByKhoanThuIdOrderByNgayNopDesc(kt.getId());
            for (ThanhToan tt : thanhToans) {
                if (tt.getHoGiaDinh() != null) {
                    ttMapByHo.computeIfAbsent(tt.getHoGiaDinh().getId(), k -> new ArrayList<>()).add(tt);
                }
            }
        }

        int rowIndex = 1;

        long totalHo = tatCaHo.size();
        long hoDaDong = 0;
        long hoDongDu = 0;
        long hoConNo = 0;
        BigDecimal tongDaThuBatBuoc = BigDecimal.ZERO;
        BigDecimal tongConThieuBatBuoc = BigDecimal.ZERO;
        BigDecimal tongDaThuTuNguyen = BigDecimal.ZERO;

        for (HoGiaDinh ho : tatCaHo) {
            List<ThanhToan> ttList = ttMapByHo.getOrDefault(ho.getId(), Collections.emptyList());

            BigDecimal yeuCauBatBuoc = BigDecimal.ZERO;
            BigDecimal daNopBatBuoc = BigDecimal.ZERO;
            BigDecimal daNopTuNguyen = BigDecimal.ZERO;
            LocalDate ngayNopGanNhat = null;

            for (KhoanThu kt : listKt) {
                boolean isBatBuoc = kt.getLoaiKhoanThu() != null && "BAT_BUOC".equals(kt.getLoaiKhoanThu().getLoaiApDung().name());
                boolean isTuNguyen = kt.getLoaiKhoanThu() != null && "TU_NGUYEN".equals(kt.getLoaiKhoanThu().getLoaiApDung().name());

                ThanhToan tt = ttList.stream().filter(t -> t.getKhoanThu().getId().equals(kt.getId())).findFirst().orElse(null);

                BigDecimal yc = BigDecimal.ZERO;
                BigDecimal dn = BigDecimal.ZERO;

                if (tt != null) {
                    yc = tt.getSoTienYeuCauHieuLuc();
                    dn = tt.getSoTienDaNop() != null ? tt.getSoTienDaNop() : BigDecimal.ZERO;
                    if (tt.getNgayNop() != null) {
                        if (ngayNopGanNhat == null || tt.getNgayNop().isAfter(ngayNopGanNhat)) {
                            ngayNopGanNhat = tt.getNgayNop();
                        }
                    }
                } else {
                    if (isBatBuoc) {
                        yc = tinhSoTienYeuCau(kt, ho);
                    }
                }

                if (isBatBuoc) {
                    yeuCauBatBuoc = yeuCauBatBuoc.add(yc);
                    daNopBatBuoc = daNopBatBuoc.add(dn);
                } else if (isTuNguyen) {
                    daNopTuNguyen = daNopTuNguyen.add(dn);
                }
            }

            BigDecimal conThieuBatBuoc = yeuCauBatBuoc.subtract(daNopBatBuoc);
            if (conThieuBatBuoc.compareTo(BigDecimal.ZERO) < 0) {
                conThieuBatBuoc = BigDecimal.ZERO;
            }

            String trangThai = "CHƯA NỘP";
            if (yeuCauBatBuoc.compareTo(BigDecimal.ZERO) == 0) {
                trangThai = "KHÔNG CÓ PHÍ";
            } else if (daNopBatBuoc.compareTo(yeuCauBatBuoc) >= 0) {
                trangThai = "DA_DONG";
            } else if (daNopBatBuoc.compareTo(BigDecimal.ZERO) > 0) {
                trangThai = "DONG_DU";
            } else {
                trangThai = "CON_NO";
            }

            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(ho.getSoCanHo());
            row.createCell(1).setCellValue(ho.getChuHo());
            row.createCell(2).setCellValue(ho.getNhanKhaus() != null ? ho.getNhanKhaus().size() : 0);
            row.createCell(3).setCellValue(ho.getDienTich() != null ? ho.getDienTich().doubleValue() : 0);
            row.createCell(4).setCellValue(yeuCauBatBuoc.doubleValue());
            row.createCell(5).setCellValue(daNopBatBuoc.doubleValue());
            row.createCell(6).setCellValue(conThieuBatBuoc.doubleValue());
            row.createCell(7).setCellValue(daNopTuNguyen.doubleValue());
            row.createCell(8).setCellValue(trangThai);
            row.createCell(9).setCellValue(ngayNopGanNhat != null ? ngayNopGanNhat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");

            if ("DA_DONG".equals(trangThai)) hoDaDong++;
            else if ("DONG_DU".equals(trangThai)) hoDongDu++;
            else if ("CON_NO".equals(trangThai) || "CHƯA NỘP".equals(trangThai)) hoConNo++;

            tongDaThuBatBuoc = tongDaThuBatBuoc.add(daNopBatBuoc);
            tongConThieuBatBuoc = tongConThieuBatBuoc.add(conThieuBatBuoc);
            tongDaThuTuNguyen = tongDaThuTuNguyen.add(daNopTuNguyen);
        }

        rowIndex++;
        Row summaryRow = sheet.createRow(rowIndex);
        summaryRow.createCell(0).setCellValue("TỔNG CỘNG");
        summaryRow.getCell(0).setCellStyle(headerStyle);
        summaryRow.createCell(1).setCellValue(totalHo + " hộ");
        summaryRow.createCell(4).setCellValue("Đã đóng đủ BB: " + hoDaDong + " | Dư: " + hoDongDu + " | Nợ BB: " + hoConNo);
        summaryRow.createCell(5).setCellValue("Tổng thu BB: " + tongDaThuBatBuoc.doubleValue());
        summaryRow.createCell(6).setCellValue("Tổng nợ BB: " + tongConThieuBatBuoc.doubleValue());
        summaryRow.createCell(7).setCellValue("Tổng thu TN: " + tongDaThuTuNguyen.doubleValue());

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private BigDecimal tinhSoTienYeuCau(KhoanThu kt, HoGiaDinh ho) {
        LoaiTinhPhi ltp = kt.getLoaiTinhPhi() != null ? kt.getLoaiTinhPhi() : LoaiTinhPhi.FIXED;
        if (ltp == LoaiTinhPhi.PER_M2) {
            if (kt.getDonGiaPerM2() != null && ho.getDienTich() != null) {
                return ho.getDienTich().multiply(kt.getDonGiaPerM2());
            }
            return kt.getSoTien() != null ? kt.getSoTien() : BigDecimal.ZERO;
        }
        if (ltp == LoaiTinhPhi.PER_XE) {
            long soXeMay = phuongTienRepository.countByHoGiaDinhIdAndLoaiXe(ho.getId(), LoaiXe.XEMAY);
            long soOto   = phuongTienRepository.countByHoGiaDinhIdAndLoaiXe(ho.getId(), LoaiXe.OTO);
            long giaXeMay = kt.getGiaXeMay() != null ? kt.getGiaXeMay().longValue() : 70_000L;
            long giaOto   = kt.getGiaOto()   != null ? kt.getGiaOto().longValue()   : 1_200_000L;
            return BigDecimal.valueOf(soXeMay * giaXeMay + soOto * giaOto);
        }
        return kt.getSoTien() != null ? kt.getSoTien() : BigDecimal.ZERO;
    }
}
