package com.bluemoon.service;

import com.bluemoon.dao.HoaDonThuHoRepository;
import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoaDonThuHoService {

    private final HoaDonThuHoRepository hoaDonRepo;
    private final HoGiaDinhRepository   hoRepo;
    private final SimulationDataService  simService;
    private final EmailService           emailService;
    private final AuditLogService        auditLogService;
    private final NguoiDungService       nguoiDungService;

    public List<HoaDonThuHo> findAll() {
        return hoaDonRepo.findAllByOrderByNgayTaoDesc();
    }

    public HoaDonThuHo findById(Integer id) {
        return hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn id=" + id));
    }

    public List<HoaDonThuHo> findWithFilter(Integer idHo, LoaiDichVuThuHo loaiDichVu,
                                             TrangThaiHoaDonThuHo trangThai, YearMonth thang) {
        List<HoaDonThuHo> list = hoaDonRepo.findAllByOrderByNgayTaoDesc();

        if (idHo != null) {
            list = list.stream().filter(hd -> hd.getHoGiaDinh().getId().equals(idHo))
                       .collect(Collectors.toList());
        }
        if (loaiDichVu != null) {
            list = list.stream().filter(hd -> hd.getLoaiDichVu() == loaiDichVu)
                       .collect(Collectors.toList());
        }
        if (trangThai != null) {
            list = list.stream().filter(hd -> hd.getTrangThai() == trangThai)
                       .collect(Collectors.toList());
        }
        if (thang != null) {
            LocalDate first = thang.atDay(1);
            LocalDate last  = thang.atEndOfMonth();
            list = list.stream()
                       .filter(hd -> hd.getKyThanhToan() != null
                               && !hd.getKyThanhToan().isBefore(first)
                               && !hd.getKyThanhToan().isAfter(last))
                       .collect(Collectors.toList());
        }
        return list;
    }

    // tạo hóa đơn cho 1 hộ (nhập tay hoặc gen hàng loạt)
    @Transactional
    public HoaDonThuHo taoHoaDon(Integer idHo, LoaiDichVuThuHo loaiDichVu,
                                  YearMonth ky, BigDecimal soTien,
                                  LocalDate hanThanhToan, String ghiChu) {
        HoGiaDinh ho = hoRepo.findById(idHo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hộ id=" + idHo));

        LocalDate kyDate = ky.atDay(1);
        if (hoaDonRepo.existsByHoGiaDinhIdAndLoaiDichVuAndKyThanhToan(idHo, loaiDichVu, kyDate)) {
            throw new IllegalStateException(
                    "Đã tồn tại hóa đơn " + loaiDichVu.getTenHienThi()
                    + " cho căn hộ " + ho.getSoCanHo()
                    + " kỳ " + ky.format(DateTimeFormatter.ofPattern("MM/yyyy")));
        }

        HoaDonThuHo hd = new HoaDonThuHo();
        hd.setHoGiaDinh(ho);
        hd.setLoaiDichVu(loaiDichVu);
        hd.setKyThanhToan(kyDate);
        hd.setSoTien(soTien);
        hd.setHanThanhToan(hanThanhToan);
        hd.setGhiChu(ghiChu);
        hd.setTrangThai(TrangThaiHoaDonThuHo.CHO_THANH_TOAN);
        hd.setMaHoaDon(genMaHoaDon(loaiDichVu, ho.getSoCanHo(), ky));

        HoaDonThuHo saved = hoaDonRepo.save(hd);
        String user = currentUser();
        log.info("[AUDIT] Tạo hóa đơn thu hộ: ma={}, canHo={}, dv={}, soTien={}, user={}",
                saved.getMaHoaDon(), ho.getSoCanHo(), loaiDichVu.getTenHienThi(), soTien, user);
        auditLogService.log("Tạo", "HoaDonThuHo",
                "ma=" + saved.getMaHoaDon() + ", canHo=" + ho.getSoCanHo()
                + ", dv=" + loaiDichVu.getTenHienThi() + ", soTien=" + soTien, user);
        return saved;
    }

    // gen hóa đơn mô phỏng cho tất cả hộ — bỏ qua hộ đã có cùng kỳ
    @Transactional
    public int genHangLoat(LoaiDichVuThuHo loaiDichVu, YearMonth ky, LocalDate hanThanhToan) {
        List<HoGiaDinh> tatCaHo = hoRepo.findAll();
        LocalDate kyDate = ky.atDay(1);
        int count = 0;

        for (HoGiaDinh ho : tatCaHo) {
            if (hoaDonRepo.existsByHoGiaDinhIdAndLoaiDichVuAndKyThanhToan(ho.getId(), loaiDichVu, kyDate)) {
                continue;
            }
            BigDecimal soTien = simService.sinhSoTienMoPhong(ho, loaiDichVu, ky.getYear(), ky.getMonthValue());
            HoaDonThuHo hd = new HoaDonThuHo();
            hd.setHoGiaDinh(ho);
            hd.setLoaiDichVu(loaiDichVu);
            hd.setKyThanhToan(kyDate);
            hd.setSoTien(soTien);
            hd.setHanThanhToan(hanThanhToan);
            hd.setTrangThai(TrangThaiHoaDonThuHo.CHO_THANH_TOAN);
            hd.setMaHoaDon(genMaHoaDon(loaiDichVu, ho.getSoCanHo(), ky));
            hoaDonRepo.save(hd);
            count++;
        }

        String user = currentUser();
        log.info("[AUDIT] Gen hàng loạt hóa đơn thu hộ: dv={}, ky={}, soHoaDon={}, user={}",
                loaiDichVu.getTenHienThi(), ky.format(DateTimeFormatter.ofPattern("MM/yyyy")), count, user);
        auditLogService.log("Gen hàng loạt", "HoaDonThuHo",
                "dv=" + loaiDichVu.getTenHienThi()
                + ", ky=" + ky.format(DateTimeFormatter.ofPattern("MM/yyyy"))
                + ", soHoaDon=" + count, user);
        return count;
    }

    // gửi email thông báo, cập nhật emailDaGui=true
    @Transactional
    public void guiEmailThongBao(Integer id) {
        HoaDonThuHo hd = findById(id);
        if (hd.getTrangThai() != TrangThaiHoaDonThuHo.CHO_THANH_TOAN) {
            throw new IllegalStateException("Chỉ gửi email cho hóa đơn đang chờ thanh toán.");
        }
        emailService.guiEmailThuHoThongBao(hd);
        hd.setEmailDaGui(true);
        hoaDonRepo.save(hd);
        log.info("[AUDIT] Gửi email thông báo hóa đơn thu hộ: ma={}, canHo={}, user={}",
                hd.getMaHoaDon(), hd.getHoGiaDinh().getSoCanHo(), currentUser());
        auditLogService.log("Gửi email", "HoaDonThuHo",
                "ma=" + hd.getMaHoaDon() + ", canHo=" + hd.getHoGiaDinh().getSoCanHo(), currentUser());
    }

    // gửi email hàng loạt cho hóa đơn CHO_THANH_TOAN chưa gửi
    @Transactional
    public int guiEmailHangLoat(LoaiDichVuThuHo loaiDichVu, YearMonth ky) {
        List<HoaDonThuHo> list = findWithFilter(null, loaiDichVu,
                TrangThaiHoaDonThuHo.CHO_THANH_TOAN, ky);
        List<HoaDonThuHo> chuaGui = list.stream()
                .filter(hd -> !hd.isEmailDaGui())
                .collect(Collectors.toList());
        for (HoaDonThuHo hd : chuaGui) {
            emailService.guiEmailThuHoThongBao(hd);
            hd.setEmailDaGui(true);
            hoaDonRepo.save(hd);
        }
        log.info("[AUDIT] Gửi email hàng loạt hóa đơn thu hộ: dv={}, ky={}, soEmail={}, user={}",
                loaiDichVu != null ? loaiDichVu.getTenHienThi() : "tất cả",
                ky != null ? ky.format(DateTimeFormatter.ofPattern("MM/yyyy")) : "tất cả",
                chuaGui.size(), currentUser());
        auditLogService.log("Gửi email hàng loạt", "HoaDonThuHo",
                "dv=" + (loaiDichVu != null ? loaiDichVu.getTenHienThi() : "tất cả")
                + ", ky=" + (ky != null ? ky.format(DateTimeFormatter.ofPattern("MM/yyyy")) : "tất cả")
                + ", soEmail=" + chuaGui.size(), currentUser());
        return chuaGui.size();
    }

    // xác nhận đã thu → DA_THANH_TOAN + email biên nhận
    @Transactional
    public void xacNhanThanhToan(Integer id, String tenDangNhap) {
        HoaDonThuHo hd = findById(id);
        if (hd.getTrangThai() != TrangThaiHoaDonThuHo.CHO_THANH_TOAN) {
            throw new IllegalStateException("Hóa đơn không ở trạng thái chờ thanh toán.");
        }
        NguoiDung nguoiXacNhan = null;
        try { nguoiXacNhan = nguoiDungService.findByTenDangNhap(tenDangNhap); } catch (Exception ignored) {}

        hd.setTrangThai(TrangThaiHoaDonThuHo.DA_THANH_TOAN);
        hd.setNgayXacNhan(java.time.LocalDateTime.now());
        hd.setNguoiXacNhan(nguoiXacNhan);
        hoaDonRepo.save(hd);

        emailService.guiEmailThuHoXacNhan(hd);
        log.info("[AUDIT] Xác nhận thanh toán hóa đơn thu hộ: ma={}, canHo={}, user={}",
                hd.getMaHoaDon(), hd.getHoGiaDinh().getSoCanHo(), tenDangNhap);
        auditLogService.log("Xác nhận thanh toán", "HoaDonThuHo",
                "ma=" + hd.getMaHoaDon() + ", canHo=" + hd.getHoGiaDinh().getSoCanHo(), tenDangNhap);
    }

    // khôi phục hóa đơn DA_HUY → CHO_THANH_TOAN
    @Transactional
    public void khoiPhucHoaDon(Integer id) {
        HoaDonThuHo hd = findById(id);
        if (hd.getTrangThai() != TrangThaiHoaDonThuHo.DA_HUY) {
            throw new IllegalStateException("Chỉ có thể khôi phục hóa đơn đã hủy.");
        }
        hd.setTrangThai(TrangThaiHoaDonThuHo.CHO_THANH_TOAN);
        hoaDonRepo.save(hd);
        log.info("[AUDIT] Khôi phục hóa đơn thu hộ: ma={}, canHo={}, user={}",
                hd.getMaHoaDon(), hd.getHoGiaDinh().getSoCanHo(), currentUser());
        auditLogService.log("Khôi phục", "HoaDonThuHo",
                "ma=" + hd.getMaHoaDon() + ", canHo=" + hd.getHoGiaDinh().getSoCanHo(), currentUser());
    }

    // xóa vĩnh viễn (chỉ CHO_THANH_TOAN hoặc DA_HUY)
    @Transactional
    public void xoaHoaDon(Integer id) {
        HoaDonThuHo hd = findById(id);
        if (hd.getTrangThai() == TrangThaiHoaDonThuHo.DA_THANH_TOAN) {
            throw new IllegalStateException("Không thể xóa hóa đơn đã thanh toán.");
        }
        log.info("[AUDIT] Xóa hóa đơn thu hộ: ma={}, canHo={}, trangThai={}, user={}",
                hd.getMaHoaDon(), hd.getHoGiaDinh().getSoCanHo(),
                hd.getTrangThai().getTenHienThi(), currentUser());
        auditLogService.log("Xóa", "HoaDonThuHo",
                "ma=" + hd.getMaHoaDon() + ", canHo=" + hd.getHoGiaDinh().getSoCanHo()
                + ", trangThai=" + hd.getTrangThai().getTenHienThi(), currentUser());
        hoaDonRepo.deleteById(id);
    }

    // hủy hóa đơn (chỉ CHO_THANH_TOAN)
    @Transactional
    public void huyHoaDon(Integer id, String ghiChuHuy) {
        HoaDonThuHo hd = findById(id);
        if (hd.getTrangThai() != TrangThaiHoaDonThuHo.CHO_THANH_TOAN) {
            throw new IllegalStateException("Chỉ có thể hủy hóa đơn đang chờ thanh toán.");
        }
        hd.setTrangThai(TrangThaiHoaDonThuHo.DA_HUY);
        if (ghiChuHuy != null && !ghiChuHuy.isBlank()) {
            hd.setGhiChu((hd.getGhiChu() != null ? hd.getGhiChu() + " | " : "") + "Hủy: " + ghiChuHuy);
        }
        hoaDonRepo.save(hd);
        log.info("[AUDIT] Hủy hóa đơn thu hộ: ma={}, canHo={}, lyDo={}, user={}",
                hd.getMaHoaDon(), hd.getHoGiaDinh().getSoCanHo(), ghiChuHuy, currentUser());
        auditLogService.log("Hủy hóa đơn", "HoaDonThuHo",
                "ma=" + hd.getMaHoaDon() + ", canHo=" + hd.getHoGiaDinh().getSoCanHo()
                + ", lyDo=" + ghiChuHuy, currentUser());
    }

    private String genMaHoaDon(LoaiDichVuThuHo loai, String soCanHo, YearMonth ky) {
        String kyStr = ky.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String canHoClean = soCanHo.replaceAll("[^A-Za-z0-9]", "");
        return "HD-" + loai.name() + "-" + canHoClean + "-" + kyStr;
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
