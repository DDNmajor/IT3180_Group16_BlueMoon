package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.model.HoGiaDinh;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final KhoanThuService khoanThuService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Transactional
    public String importHoGiaDinh(MultipartFile file) {
        int successCount = 0;
        int errorCount = 0;
        List<String> errorMessages = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Pass 1: Đọc và Validate toàn bộ dữ liệu
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String soCanHo = getCellStringValue(row.getCell(0));
                String chuHo = getCellStringValue(row.getCell(1));
                String dienTichStr = getCellStringValue(row.getCell(3));
                String email = getCellStringValue(row.getCell(4));

                // Bỏ qua dòng trống
                if (soCanHo.isBlank() && chuHo.isBlank()) continue;

                List<String> rowErrors = new ArrayList<>();
                if (soCanHo.isBlank()) rowErrors.add("Số căn hộ không được để trống");
                if (chuHo.isBlank()) rowErrors.add("Chủ hộ không được để trống");
                
                try {
                    if (!dienTichStr.isBlank()) {
                        BigDecimal dienTich = new BigDecimal(dienTichStr);
                        if (dienTich.compareTo(BigDecimal.ZERO) <= 0) {
                            rowErrors.add("Diện tích phải lớn hơn 0");
                        }
                    }
                } catch (NumberFormatException e) {
                    rowErrors.add("Diện tích không đúng định dạng số");
                }

                if (!email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
                    rowErrors.add("Email không đúng định dạng");
                }

                if (!rowErrors.isEmpty()) {
                    errorCount++;
                    errorMessages.add("- Dòng " + (i + 1) + ": " + String.join(", ", rowErrors));
                }
            }

            // Nếu có lỗi, từ chối import toàn bộ
            if (!errorMessages.isEmpty()) {
                throw new RuntimeException("Phát hiện " + errorCount + " dòng lỗi. Từ chối import toàn bộ file:\n<br>" + String.join("\n<br>", errorMessages));
            }

            // Pass 2: Không có lỗi -> Xử lý Insert / Update
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String soCanHo = getCellStringValue(row.getCell(0));
                String chuHo = getCellStringValue(row.getCell(1));
                String tangKhuVuc = getCellStringValue(row.getCell(2));
                String dienTichStr = getCellStringValue(row.getCell(3));
                String email = getCellStringValue(row.getCell(4));
                String ghiChu = getCellStringValue(row.getCell(5));

                if (soCanHo.isBlank() && chuHo.isBlank()) continue;

                BigDecimal dienTich = null;
                if (!dienTichStr.isBlank()) {
                    dienTich = new BigDecimal(dienTichStr);
                }

                Optional<HoGiaDinh> existing = hoGiaDinhRepository.findBySoCanHo(soCanHo);
                HoGiaDinh ho = existing.orElseGet(HoGiaDinh::new);
                boolean isNew = (ho.getId() == null);
                BigDecimal dienTichCu = ho.getDienTich(); // Lấy diện tích cũ trước khi ghi đè

                ho.setSoCanHo(soCanHo);
                ho.setChuHo(chuHo);
                ho.setTangKhuVuc(tangKhuVuc);
                ho.setDienTich(dienTich);
                ho.setEmail(email);
                ho.setGhiChu(ghiChu);

                hoGiaDinhRepository.save(ho);
                
                if (isNew) {
                    // Áp dụng tự động các khoản thu bắt buộc cho hộ mới
                    khoanThuService.autoApplyForNewHo(ho);
                } else if (dienTich != null && (dienTichCu == null || dienTich.compareTo(dienTichCu) != 0)) {
                    // Nếu diện tích thay đổi, tính lại các phí PER_M2 bằng cách truyền vào diện tích CŨ
                    khoanThuService.recalculatePerM2ForHo(ho, dienTichCu);
                }

                successCount++;
            }
        } catch (RuntimeException e) {
            return "Lỗi đọc file Excel: " + e.getMessage();
        } catch (Exception e) {
            return "Lỗi đọc file Excel: Có lỗi hệ thống xảy ra (" + e.getMessage() + ")";
        }

        return "Import hoàn tất. Thành công: " + successCount + " dòng.";
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue().trim();
                }
            default:
                return "";
        }
    }
}
