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
                "Tên khoản thu", "Số căn hộ", "Chủ hộ", "Số nhân khẩu", "Diện tích (m²)",
                "Số tiền yêu cầu", "Số tiền đã nộp", "Còn thiếu",
                "Trạng thái", "Phương thức", "Người thu", "Ngày nộp gần nhất"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<HoGiaDinh> tatCaHo = hoGiaDinhRepository.findAll();
        int rowIndex = 1;

        long totalHo = 0;
        long hoDaDong = 0;
        long hoDongDu = 0;
        long hoConNo = 0;
        BigDecimal tongDaThu = BigDecimal.ZERO;
        BigDecimal tongConThieu = BigDecimal.ZERO;

        for (KhoanThu kt : listKt) {
            List<ThanhToan> thanhToans = thanhToanRepository.findByKhoanThuIdOrderByNgayNopDesc(kt.getId());
            Map<Integer, ThanhToan> ttMap = thanhToans.stream()
                    .filter(t -> t.getHoGiaDinh() != null)
                    .collect(Collectors.toMap(
                            t -> t.getHoGiaDinh().getId(),
                            t -> t,
                            (t1, t2) -> t1
                    ));

            boolean isTuNguyen = kt.getLoaiKhoanThu() != null 
                    && kt.getLoaiKhoanThu().getLoaiApDung() != null 
                    && kt.getLoaiKhoanThu().getLoaiApDung().name().contains("TU_NGUYEN");

            for (HoGiaDinh ho : tatCaHo) {
                ThanhToan tt = ttMap.get(ho.getId());
                
                if (isTuNguyen && tt == null) {
                    continue; // Skip hộ tự nguyện chưa đóng
                }

                totalHo++;
                Row row = sheet.createRow(rowIndex++);
                
                row.createCell(0).setCellValue(kt.getTenKhoanThu());
                row.createCell(1).setCellValue(ho.getSoCanHo());
                row.createCell(2).setCellValue(ho.getChuHo());
                row.createCell(3).setCellValue(ho.getNhanKhaus() != null ? ho.getNhanKhaus().size() : 0);
                row.createCell(4).setCellValue(ho.getDienTich() != null ? ho.getDienTich().doubleValue() : 0);

                BigDecimal soTienYeuCau;
                BigDecimal soTienDaNop = BigDecimal.ZERO;
                String trangThai = "CHƯA NỘP";
                String phuongThuc = "";
                String nguoiThu = "";
                String ngayNop = "";

                if (tt != null) {
                    soTienYeuCau = tt.getSoTienYeuCauHieuLuc();
                    soTienDaNop = tt.getSoTienDaNop() != null ? tt.getSoTienDaNop() : BigDecimal.ZERO;
                    trangThai = tt.getTrangThai() != null ? tt.getTrangThai().name() : "";
                    phuongThuc = tt.getPhuongThuc() != null ? tt.getPhuongThuc().name() : "";
                    nguoiThu = tt.getNguoiThu() != null ? tt.getNguoiThu().getHoTen() : "";
                    ngayNop = tt.getNgayNop() != null ? tt.getNgayNop().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                } else {
                    soTienYeuCau = tinhSoTienYeuCau(kt, ho);
                    trangThai = "CON_NO";
                }

                BigDecimal conThieu = soTienYeuCau.subtract(soTienDaNop);
                if (conThieu.compareTo(BigDecimal.ZERO) < 0) {
                    conThieu = BigDecimal.ZERO;
                }

                row.createCell(5).setCellValue(soTienYeuCau.doubleValue());
                row.createCell(6).setCellValue(soTienDaNop.doubleValue());
                row.createCell(7).setCellValue(conThieu.doubleValue());
                row.createCell(8).setCellValue(trangThai);
                row.createCell(9).setCellValue(phuongThuc);
                row.createCell(10).setCellValue(nguoiThu);
                row.createCell(11).setCellValue(ngayNop);

                if ("DA_DONG".equals(trangThai)) hoDaDong++;
                else if ("DONG_DU".equals(trangThai)) hoDongDu++;
                else if ("CON_NO".equals(trangThai) || "CHƯA NỘP".equals(trangThai)) hoConNo++;

                tongDaThu = tongDaThu.add(soTienDaNop);
                tongConThieu = tongConThieu.add(conThieu);
            }
        }

        // Dòng tổng kết
        rowIndex++;
        Row summaryRow = sheet.createRow(rowIndex);
        summaryRow.createCell(0).setCellValue("TỔNG CỘNG");
        summaryRow.getCell(0).setCellStyle(headerStyle);
        summaryRow.createCell(1).setCellValue(totalHo + " lượt");
        summaryRow.createCell(5).setCellValue("Đã đóng: " + hoDaDong + " | Dư: " + hoDongDu + " | Nợ: " + hoConNo);
        summaryRow.createCell(6).setCellValue("Tổng thu: " + tongDaThu.doubleValue());
        summaryRow.createCell(7).setCellValue("Tổng nợ: " + tongConThieu.doubleValue());

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
