package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThanhToanService {

    private final ThanhToanRepository  thanhToanRepository;
    private final HoGiaDinhRepository  hoGiaDinhRepository;
    private final AuditLogService      auditLogService;

    public List<ThanhToan> findAll() {
        return thanhToanRepository.findAll();
    }

    public ThanhToan findById(Integer id) {
        return thanhToanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán id=" + id));
    }

    public List<ThanhToan> findByHoGiaDinh(Integer idHoGiaDinh) {
        return thanhToanRepository.findByHoGiaDinhIdOrderByNgayNopDesc(idHoGiaDinh);
    }

    public List<ThanhToan> findByKhoanThu(Integer idKhoanThu) {
        return thanhToanRepository.findByKhoanThuIdOrderByNgayNopDesc(idKhoanThu);
    }

    public List<ThanhToan> findByHoGiaDinhAndKhoanThu(Integer idHo, Integer idKhoan) {
        return thanhToanRepository.findByHoGiaDinhIdAndKhoanThuIdOrderByNgayNopDesc(idHo, idKhoan);
    }

    public Optional<ThanhToan> findConNo(Integer idHo, Integer idKhoan) {
        return thanhToanRepository.findFirstByHoGiaDinhIdAndKhoanThuIdAndTrangThai(
                idHo, idKhoan, TrangThaiThanhToan.CON_NO);
    }

    public boolean daDongHoanTat(Integer idHoGiaDinh, Integer idKhoanThu) {
        return thanhToanRepository.existsByHoGiaDinhIdAndKhoanThuIdAndTrangThaiIn(
                idHoGiaDinh, idKhoanThu,
                java.util.List.of(TrangThaiThanhToan.DA_DONG, TrangThaiThanhToan.DONG_DU));
    }

    public TrangThaiThanhToan tinhTrangThai(BigDecimal soTienDaNop, BigDecimal soTienYeuCau) {
        int cmp = soTienDaNop.compareTo(soTienYeuCau);
        if (cmp == 0) return TrangThaiThanhToan.DA_DONG;
        if (cmp < 0)  return TrangThaiThanhToan.CON_NO;
        return TrangThaiThanhToan.DONG_DU;
    }

    @Transactional
    public ThanhToan save(ThanhToan thanhToan) {
        if (thanhToan.getSoTienYeuCau() == null
                && thanhToan.getKhoanThu() != null
                && thanhToan.getKhoanThu().getDonGiaPerM2() != null
                && thanhToan.getHoGiaDinh() != null
                && thanhToan.getHoGiaDinh().getDienTich() != null) {
            thanhToan.setSoTienYeuCau(
                    thanhToan.getHoGiaDinh().getDienTich()
                            .multiply(thanhToan.getKhoanThu().getDonGiaPerM2()));
        }
        if (thanhToan.getNgayNop() == null) {
            thanhToan.setNgayNop(LocalDate.now());
        }
        if (thanhToan.getKhoanThu() != null && thanhToan.getSoTienDaNop() != null) {
            thanhToan.setTrangThai(
                    tinhTrangThai(thanhToan.getSoTienDaNop(), thanhToan.getSoTienYeuCauHieuLuc()));
        }
        ThanhToan saved = thanhToanRepository.save(thanhToan);
        String canHo    = saved.getHoGiaDinh() != null ? saved.getHoGiaDinh().getSoCanHo()   : "?";
        String khoanThu = saved.getKhoanThu()  != null ? saved.getKhoanThu().getTenKhoanThu() : "?";
        String nguoiThu = saved.getNguoiThu()  != null ? saved.getNguoiThu().getTenDangNhap() : "?";
        log.info("[AUDIT] Ghi nhận thanh toán: id={}, canHo={}, khoanThu={}, soTien={}, trangThai={}, phuongThuc={}, nguoiThu={}",
                saved.getId(), canHo, khoanThu, saved.getSoTienDaNop(),
                saved.getTrangThai(), saved.getPhuongThuc(), nguoiThu);
        auditLogService.log("Ghi nhận", "Thanh toán",
                "id=" + saved.getId() + ", canHo=" + canHo + ", khoanThu=" + khoanThu
                + ", soTien=" + saved.getSoTienDaNop() + ", trangThai=" + saved.getTrangThai()
                + ", nguoiThu=" + nguoiThu, currentUser());
        return saved;
    }

    @Transactional
    public ThanhToan nopThem(Integer id, BigDecimal soTienThem, NguoiDung nguoiThu) {
        ThanhToan tt = findById(id);
        BigDecimal tongMoi = tt.getSoTienDaNop().add(soTienThem);
        tt.setSoTienDaNop(tongMoi);
        tt.setTrangThai(tinhTrangThai(tongMoi, tt.getSoTienYeuCauHieuLuc()));
        if (nguoiThu != null) tt.setNguoiThu(nguoiThu);
        ThanhToan saved = thanhToanRepository.save(tt);
        String canHo2    = tt.getHoGiaDinh() != null ? tt.getHoGiaDinh().getSoCanHo()   : "?";
        String nguoiThu2 = saved.getNguoiThu() != null ? saved.getNguoiThu().getTenDangNhap() : "?";
        log.info("[AUDIT] Nộp thêm: id={}, canHo={}, soTienThem={}, tongMoi={}, trangThai={}, nguoiThu={}",
                id, canHo2, soTienThem, tongMoi, saved.getTrangThai(), nguoiThu2);
        auditLogService.log("Nộp thêm", "Thanh toán",
                "id=" + id + ", canHo=" + canHo2 + ", soTienThem=" + soTienThem
                + ", tongMoi=" + tongMoi + ", trangThai=" + saved.getTrangThai()
                + ", nguoiThu=" + nguoiThu2, currentUser());
        return saved;
    }

    @Transactional
    public ThanhToan baoDaHoanTien(Integer id, NguoiDung nguoiThu) {
        ThanhToan tt = findById(id);
        BigDecimal soTienYeuCau = tt.getSoTienYeuCauHieuLuc();
        tt.setSoTienDaNop(soTienYeuCau);
        tt.setTrangThai(TrangThaiThanhToan.DA_DONG);
        if (nguoiThu != null) tt.setNguoiThu(nguoiThu);
        ThanhToan saved = thanhToanRepository.save(tt);
        String canHo3    = tt.getHoGiaDinh() != null ? tt.getHoGiaDinh().getSoCanHo()    : "?";
        String nguoiThu3 = saved.getNguoiThu() != null ? saved.getNguoiThu().getTenDangNhap() : "?";
        log.info("[AUDIT] Báo đã hoàn tiền: id={}, canHo={}, soTienSauHoan={}, nguoiThu={}",
                id, canHo3, soTienYeuCau, nguoiThu3);
        auditLogService.log("Hoàn tiền", "Thanh toán",
                "id=" + id + ", canHo=" + canHo3 + ", soTienSauHoan=" + soTienYeuCau
                + ", nguoiThu=" + nguoiThu3, currentUser());
        return saved;
    }

    @Transactional
    public int nhapThuHo(KhoanThu khoanThu, java.util.Map<Integer, BigDecimal> soTienMap, NguoiDung nguoiThu) {
        int count = 0;
        String user = currentUser();
        for (java.util.Map.Entry<Integer, BigDecimal> entry : soTienMap.entrySet()) {
            Integer idHo   = entry.getKey();
            BigDecimal amt = entry.getValue();
            if (amt == null || amt.compareTo(BigDecimal.ZERO) <= 0) continue;

            HoGiaDinh ho = hoGiaDinhRepository.findById(idHo).orElse(null);
            if (ho == null) continue;

            List<ThanhToan> existing = thanhToanRepository
                    .findByHoGiaDinhIdAndKhoanThuIdOrderByNgayNopDesc(idHo, khoanThu.getId());

            ThanhToan tt;
            if (!existing.isEmpty()) {
                tt = existing.get(0);
                if (tt.getTrangThai() == TrangThaiThanhToan.DA_DONG
                        || tt.getTrangThai() == TrangThaiThanhToan.DONG_DU) {
                    continue; // đã đóng xong, không sửa
                }
                tt.setSoTienYeuCau(amt);
                tt.setTrangThai(tinhTrangThai(tt.getSoTienDaNop(), amt));
            } else {
                tt = new ThanhToan();
                tt.setHoGiaDinh(ho);
                tt.setKhoanThu(khoanThu);
                tt.setSoTienYeuCau(amt);
                tt.setSoTienDaNop(BigDecimal.ZERO);
                tt.setTrangThai(TrangThaiThanhToan.CON_NO);
                tt.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);
                tt.setNgayNop(LocalDate.now());
                if (nguoiThu != null) tt.setNguoiThu(nguoiThu);
            }
            thanhToanRepository.save(tt);
            count++;
            log.info("[AUDIT] Nhập thu hộ: canHo={}, khoanThu={}, soTien={}, user={}",
                    ho.getSoCanHo(), khoanThu.getMaKhoanThu(), amt, user);
            auditLogService.log("Nhập thu hộ", "Thanh toán",
                    "canHo=" + ho.getSoCanHo() + ", khoanThu=" + khoanThu.getMaKhoanThu()
                    + ", soTien=" + amt, user);
        }
        return count;
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    // bắt buộc: reset về CON_NO thay vì xóa hẳn
    @Transactional
    public boolean delete(Integer id) {
        ThanhToan tt = findById(id);
        boolean batBuoc = tt.getKhoanThu() != null
                && tt.getKhoanThu().getLoaiKhoanThu() != null
                && tt.getKhoanThu().getLoaiKhoanThu().getLoaiApDung() != null
                && tt.getKhoanThu().getLoaiKhoanThu().getLoaiApDung().isBatBuoc();

        String canHo4    = tt.getHoGiaDinh() != null ? tt.getHoGiaDinh().getSoCanHo()   : "?";
        String khoanThu4 = tt.getKhoanThu()  != null ? tt.getKhoanThu().getTenKhoanThu() : "?";
        if (batBuoc) {
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);
            tt.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);
            thanhToanRepository.save(tt);
            log.info("[AUDIT] Reset thanh toán bắt buộc về CON_NO: id={}, canHo={}, khoanThu={}",
                    id, canHo4, khoanThu4);
            auditLogService.log("Reset", "Thanh toán",
                    "id=" + id + ", canHo=" + canHo4 + ", khoanThu=" + khoanThu4 + " → CON_NO", currentUser());
            return false;
        }

        thanhToanRepository.deleteById(id);
        log.info("[AUDIT] Xóa thanh toán: id={}, canHo={}, khoanThu={}", id, canHo4, khoanThu4);
        auditLogService.log("Xóa", "Thanh toán",
                "id=" + id + ", canHo=" + canHo4 + ", khoanThu=" + khoanThu4, currentUser());
        return true;
    }
}
