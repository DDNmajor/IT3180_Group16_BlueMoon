package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.NhanKhau;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý nghiệp vụ Import dữ liệu từ file Excel.
 * Đảm bảo tính toàn vẹn dữ liệu: Lỗi 1 dòng -> Rollback toàn bộ file.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final NhanKhauRepository nhanKhauRepository;
    private final ThanhToanService thanhToanService;
    private final AuditLogService auditLogService;

    /**
     * Đọc file Excel, Validate và Import dữ liệu vào DB.
     * Sử dụng @Transactional để đảm bảo ACID. Nếu ném ra RuntimeException, 
     * Spring sẽ tự động Rollback toàn bộ các câu lệnh INSERT trước đó.
     */
    @Transactional(rollbackFor = Exception.class)
    public String importData(MultipartFile file, String currentUser) throws Exception {
        List<HoGiaDinh> danhSachHo = new ArrayList<>();
        List<NhanKhau> danhSachNhanKhau = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            
            // 1. XỬ LÝ SHEET HỘ GIA ĐÌNH (Sheet 0)
            Sheet hoGiaDinhSheet = workbook.getSheetAt(0);
            for (Row row : hoGiaDinhSheet) {
                if (row.getRowNum() == 0) continue; // Bỏ qua dòng Header

                String soCanHo = getCellValue(row.getCell(0));
                if (soCanHo.isEmpty()) break; // Kết thúc dữ liệu

                // Validate: Kiểm tra trùng lặp mã căn hộ
                if (hoGiaDinhRepository.existsBySoCanHo(soCanHo)) {
                    throw new RuntimeException("Dòng " + (row.getRowNum() + 1) + 
                            ": Mã căn hộ " + soCanHo + " đã tồn tại trong hệ thống!");
                }

                HoGiaDinh ho = new HoGiaDinh();
                ho.setSoCanHo(soCanHo);
                ho.setChuHo(getCellValue(row.getCell(1)));
                ho.setDienTich(new java.math.BigDecimal(getCellValue(row.getCell(2))));
                ho.setEmail(getCellValue(row.getCell(3)));
                ho.setSoDienThoai(getCellValue(row.getCell(4)));
                
                danhSachHo.add(ho);
            }
            
            // Lưu danh sách hộ gia đình vào DB
            hoGiaDinhRepository.saveAll(danhSachHo);

            // 2. TRIGGER AUTO-APPLY: Tự động gán các khoản nợ bắt buộc cho hộ mới
            for (HoGiaDinh ho : danhSachHo) {
                thanhToanService.autoApplyForNewHo(ho);
            }

            // Ghi Audit Log thành công
            auditLogService.log("Import Excel", "Hệ thống", 
                "Import thành công " + danhSachHo.size() + " hộ gia đình", currentUser);

            return "Import thành công " + danhSachHo.size() + " hộ gia đình. Hệ thống đã tự động gán nợ bắt buộc.";

        } catch (Exception e) {
            log.error("[IMPORT ERROR] Lỗi khi đọc file Excel: {}", e.getMessage());
            // Ném exception để trigger @Transactional Rollback
            throw new RuntimeException("Lỗi cấu trúc file Excel: " + e.getMessage()); 
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}