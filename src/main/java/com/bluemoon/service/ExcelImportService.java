package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.NhanKhau;
import com.bluemoon.model.TinhTrangCuTru;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final NhanKhauRepository  nhanKhauRepository;
    private final KhoanThuService     khoanThuService;
    private final AuditLogService     auditLogService;

    private static final Pattern EMAIL_PATTERN   = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern CCCD_PATTERN    = Pattern.compile("^\\d{9}$|^\\d{12}$");
    private static final Pattern PHONE_PATTERN   = Pattern.compile("^0\\d{9}$");
    private static final DateTimeFormatter DF_NK = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public ImportResult importHoGiaDinh(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheetHo = workbook.getSheetAt(0);
            Sheet sheetNk = workbook.getNumberOfSheets() > 1 ? workbook.getSheetAt(1) : null;

            // ── Pass 1: Validate HoGiaDinh ──────────────────────────────────────
            Set<String> soCanHoTrongFile = new LinkedHashSet<>();
            for (int i = 1; i <= sheetHo.getLastRowNum(); i++) {
                Row row = sheetHo.getRow(i);
                if (row == null) continue;

                String soCanHo = getCellString(row.getCell(0));
                String chuHo   = getCellString(row.getCell(1));
                if (soCanHo.isBlank() && chuHo.isBlank()) continue;

                String prefix = "Sheet1 dòng " + (i + 1);
                if (soCanHo.isBlank()) errors.add(prefix + ": Số căn hộ không được để trống");
                if (chuHo.isBlank())   errors.add(prefix + ": Chủ hộ không được để trống");

                if (!soCanHo.isBlank()) {
                    if (soCanHoTrongFile.contains(soCanHo)) {
                        errors.add(prefix + ": Số căn hộ '" + soCanHo + "' bị trùng trong file");
                    }
                    soCanHoTrongFile.add(soCanHo);
                }

                String dienTichStr = getCellString(row.getCell(3));
                if (!dienTichStr.isBlank()) {
                    try {
                        BigDecimal dt = new BigDecimal(dienTichStr);
                        if (dt.compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add(prefix + ": Diện tích phải lớn hơn 0");
                        }
                    } catch (NumberFormatException e) {
                        errors.add(prefix + ": Diện tích không đúng định dạng số");
                    }
                }

                String email = getCellString(row.getCell(4));
                if (!email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
                    errors.add(prefix + ": Email không đúng định dạng");
                }
            }

            // ── Pass 1: Validate NhanKhau ────────────────────────────────────────
            // Tập soCanHo hợp lệ = DB hiện tại + những hộ sẽ được tạo/cập nhật từ sheet 1
            Set<String> allValidCanHo = new HashSet<>(soCanHoTrongFile);
            hoGiaDinhRepository.findAll().forEach(h -> allValidCanHo.add(h.getSoCanHo()));

            Set<String> cccdTrongFile = new HashSet<>();
            if (sheetNk != null) {
                for (int i = 1; i <= sheetNk.getLastRowNum(); i++) {
                    Row row = sheetNk.getRow(i);
                    if (row == null) continue;

                    String hoTen   = getCellString(row.getCell(0));
                    String soCanHo = getCellString(row.getCell(6));
                    if (hoTen.isBlank() && soCanHo.isBlank()) continue;

                    String prefix = "Sheet2 dòng " + (i + 1);
                    if (hoTen.isBlank())   errors.add(prefix + ": Họ tên không được để trống");
                    if (soCanHo.isBlank()) errors.add(prefix + ": Số căn hộ không được để trống");

                    if (!soCanHo.isBlank() && !allValidCanHo.contains(soCanHo)) {
                        errors.add(prefix + ": Số căn hộ '" + soCanHo + "' không tồn tại trong hệ thống hoặc file");
                    }

                    String ngaySinhStr = getCellString(row.getCell(1));
                    if (!ngaySinhStr.isBlank()) {
                        try {
                            LocalDate.parse(ngaySinhStr, DF_NK);
                        } catch (Exception e) {
                            errors.add(prefix + ": Ngày sinh '" + ngaySinhStr + "' không đúng định dạng dd/MM/yyyy");
                        }
                    }

                    String cccd = getCellString(row.getCell(3));
                    if (!cccd.isBlank()) {
                        if (!CCCD_PATTERN.matcher(cccd).matches()) {
                            errors.add(prefix + ": CCCD phải có 9 hoặc 12 chữ số");
                        } else if (cccdTrongFile.contains(cccd)) {
                            errors.add(prefix + ": CCCD '" + cccd + "' bị trùng trong file");
                        } else if (nhanKhauRepository.existsByCccd(cccd)) {
                            errors.add(prefix + ": CCCD '" + cccd + "' đã tồn tại trong hệ thống");
                        }
                        cccdTrongFile.add(cccd);
                    }

                    String sdt = getCellString(row.getCell(4));
                    if (!sdt.isBlank() && !PHONE_PATTERN.matcher(sdt).matches()) {
                        errors.add(prefix + ": Số điện thoại phải 10 số, bắt đầu bằng 0");
                    }
                }
            }

            if (!errors.isEmpty()) {
                throw new ImportValidationException(errors);
            }

            // ── Pass 2: Lưu HoGiaDinh ───────────────────────────────────────────
            int soHoMoi = 0, soHoCapNhat = 0;
            for (int i = 1; i <= sheetHo.getLastRowNum(); i++) {
                Row row = sheetHo.getRow(i);
                if (row == null) continue;

                String soCanHo    = getCellString(row.getCell(0));
                String chuHo      = getCellString(row.getCell(1));
                if (soCanHo.isBlank() && chuHo.isBlank()) continue;

                String tangKhuVuc = getCellString(row.getCell(2));
                String dienTichStr = getCellString(row.getCell(3));
                String email      = getCellString(row.getCell(4));
                String ghiChu     = getCellString(row.getCell(5));

                BigDecimal dienTich = dienTichStr.isBlank() ? null : new BigDecimal(dienTichStr);

                Optional<HoGiaDinh> existing = hoGiaDinhRepository.findBySoCanHo(soCanHo);
                HoGiaDinh ho = existing.orElseGet(HoGiaDinh::new);
                boolean isNew = (ho.getId() == null);
                BigDecimal dienTichCu = ho.getDienTich();

                ho.setSoCanHo(soCanHo);
                ho.setChuHo(chuHo);
                ho.setTangKhuVuc(tangKhuVuc.isBlank() ? ho.getTangKhuVuc() : tangKhuVuc);
                ho.setDienTich(dienTich);
                ho.setEmail(email.isBlank() ? ho.getEmail() : email);
                ho.setGhiChu(ghiChu.isBlank() ? ho.getGhiChu() : ghiChu);

                hoGiaDinhRepository.save(ho);

                if (isNew) {
                    khoanThuService.autoApplyForNewHo(ho);
                    soHoMoi++;
                } else {
                    if (dienTich != null && (dienTichCu == null || dienTich.compareTo(dienTichCu) != 0)) {
                        khoanThuService.recalculatePerM2ForHo(ho, dienTichCu);
                    }
                    soHoCapNhat++;
                }
            }

            // ── Pass 2: Lưu NhanKhau ────────────────────────────────────────────
            int soNkMoi = 0;
            if (sheetNk != null) {
                // Rebuild map soCanHo→HoGiaDinh sau khi đã lưu
                Map<String, HoGiaDinh> hoMap = new HashMap<>();
                hoGiaDinhRepository.findAll().forEach(h -> hoMap.put(h.getSoCanHo(), h));

                for (int i = 1; i <= sheetNk.getLastRowNum(); i++) {
                    Row row = sheetNk.getRow(i);
                    if (row == null) continue;

                    String hoTen       = getCellString(row.getCell(0));
                    String ngaySinhStr = getCellString(row.getCell(1));
                    String gioiTinh    = getCellString(row.getCell(2));
                    String cccd        = getCellString(row.getCell(3));
                    String sdt         = getCellString(row.getCell(4));
                    String quanHe      = getCellString(row.getCell(5));
                    String soCanHo     = getCellString(row.getCell(6));

                    if (hoTen.isBlank() && soCanHo.isBlank()) continue;

                    NhanKhau nk = new NhanKhau();
                    nk.setHoTen(hoTen);
                    nk.setNgaySinh(ngaySinhStr.isBlank() ? null : LocalDate.parse(ngaySinhStr, DF_NK));
                    nk.setGioiTinh(gioiTinh.isBlank() ? null : gioiTinh);
                    nk.setCccd(cccd.isBlank() ? null : cccd);
                    nk.setSoDienThoai(sdt.isBlank() ? null : sdt);
                    nk.setQuanHeChuHo(quanHe.isBlank() ? null : quanHe);
                    nk.setHoGiaDinh(hoMap.get(soCanHo));
                    nk.setTinhTrang(TinhTrangCuTru.THUONG_TRU);
                    nk.setNgayTao(LocalDateTime.now());

                    nhanKhauRepository.save(nk);
                    soNkMoi++;
                }
            }

            String user = currentUser();
            log.info("[AUDIT] Import Excel hộ gia đình: hoMoi={}, capNhat={}, nkMoi={}, user={}",
                    soHoMoi, soHoCapNhat, soNkMoi, user);
            auditLogService.log("Import", "Hộ gia đình",
                    "hoMoi=" + soHoMoi + ", capNhat=" + soHoCapNhat + ", nkMoi=" + soNkMoi, user);
            return new ImportResult(soHoMoi, soHoCapNhat, soNkMoi, Collections.emptyList());

        } catch (ImportValidationException e) {
            return new ImportResult(0, 0, 0, e.getErrors());
        } catch (Exception e) {
            return new ImportResult(0, 0, 0,
                    List.of("Lỗi hệ thống khi đọc file: " + e.getMessage()));
        }
    }

    public byte[] generateTemplate() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle bold = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            bold.setFont(f);

            Sheet s1 = wb.createSheet("HoGiaDinh");
            String[] h1 = {"soCanHo (*)", "chuHo (*)", "tangKhuVuc", "dienTich_m2", "email", "ghiChu"};
            Row r1 = s1.createRow(0);
            for (int i = 0; i < h1.length; i++) {
                Cell c = r1.createCell(i);
                c.setCellValue(h1[i]);
                c.setCellStyle(bold);
                s1.setColumnWidth(i, 5000);
            }

            Sheet s2 = wb.createSheet("NhanKhau");
            String[] h2 = {"hoTen (*)", "ngaySinh (dd/MM/yyyy)", "gioiTinh", "cccd", "soDienThoai", "quanHeChuHo", "soCanHo (*)"};
            Row r2 = s2.createRow(0);
            for (int i = 0; i < h2.length; i++) {
                Cell c = r2.createCell(i);
                c.setCellValue(h2[i]);
                c.setCellStyle(bold);
                s2.setColumnWidth(i, 5500);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DF_NK);
                }
                double d = cell.getNumericCellValue();
                // Trả về số nguyên nếu không có phần thập phân (tránh "50.0" → "50")
                yield (d == Math.floor(d) && !Double.isInfinite(d))
                        ? String.valueOf((long) d)
                        : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield String.valueOf((long) cell.getNumericCellValue()); }
                catch (Exception e) { yield cell.getStringCellValue().trim(); }
            }
            default -> "";
        };
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    public record ImportResult(int soHoMoi, int soHoCapNhat, int soNkMoi, List<String> errors) {
        public boolean hasErrors() { return !errors.isEmpty(); }
    }

    private static class ImportValidationException extends RuntimeException {
        private final List<String> errors;
        ImportValidationException(List<String> errors) {
            super("Validation failed");
            this.errors = errors;
        }
        List<String> getErrors() { return errors; }
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
