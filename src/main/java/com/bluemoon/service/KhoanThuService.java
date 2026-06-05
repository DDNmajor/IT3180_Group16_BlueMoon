package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.LoaiKhoanThuRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhoanThuService {

    private final KhoanThuRepository     khoanThuRepository;
    private final ThanhToanRepository    thanhToanRepository;
    private final HoGiaDinhRepository    hoGiaDinhRepository;
    private final LoaiKhoanThuRepository loaiKhoanThuRepository;
    private final AuditLogService        auditLogService;
    private final EmailService           emailService;

    public List<KhoanThu> findAll() {
        return khoanThuRepository.findAll();
    }

    public KhoanThu findById(Integer id) {
        return khoanThuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoản thu id=" + id));
    }

    public List<KhoanThu> findByLoai(Integer idLoai) {
        return khoanThuRepository.findByLoaiKhoanThuId(idLoai);
    }

    public List<KhoanThu> findByKyThu(LocalDate from, LocalDate to) {
        return khoanThuRepository.findByKyThuBetween(from, to);
    }

    public List<KhoanThu> findQuaHan() {
        return khoanThuRepository.findByHanNopBefore(LocalDate.now());
    }

    public List<KhoanThu> findWithFilter(Integer idLoai, String trangThai, YearMonth thang) {
        List<KhoanThu> list;
        if (thang != null) {
            LocalDate from = thang.atDay(1);
            LocalDate to   = thang.atEndOfMonth();
            list = (idLoai != null)
                    ? khoanThuRepository.findByLoaiKhoanThuIdAndKyThuBetween(idLoai, from, to)
                    : khoanThuRepository.findByKyThuBetween(from, to);
        } else {
            list = (idLoai != null)
                    ? khoanThuRepository.findByLoaiKhoanThuId(idLoai)
                    : khoanThuRepository.findAll();
        }

        if ("qua-han".equals(trangThai)) {
            LocalDate today = LocalDate.now();
            list = list.stream()
                    .filter(kt -> kt.getHanNop() != null && kt.getHanNop().isBefore(today))
                    .collect(Collectors.toList());
        } else if ("con-han".equals(trangThai)) {
            LocalDate today = LocalDate.now();
            list = list.stream()
                    .filter(kt -> kt.getHanNop() == null || !kt.getHanNop().isBefore(today))
                    .collect(Collectors.toList());
        }
        return list;
    }

    @Transactional
    public KhoanThu save(KhoanThu khoanThu) {
        Integer idLoai = khoanThu.getLoaiKhoanThu() == null ? null : khoanThu.getLoaiKhoanThu().getId();
        if (idLoai != null) {
            khoanThu.setLoaiKhoanThu(loaiKhoanThuRepository.findById(idLoai)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại khoản thu id=" + idLoai)));
        }

        validateMaKhoanThu(khoanThu);

        boolean isNew = (khoanThu.getId() == null);
        if (isNew) {
            khoanThu.setNgayTao(LocalDateTime.now());
        } else {
            KhoanThu current = findById(khoanThu.getId());
            khoanThu.setNgayTao(current.getNgayTao());
        }

        KhoanThu saved = khoanThuRepository.save(khoanThu);

        String user = currentUser();
        if (isNew) {
            String tenLoai = saved.getLoaiKhoanThu() != null ? saved.getLoaiKhoanThu().getTenLoai() : "?";
            log.info("[AUDIT] Tạo khoản thu: ma={}, ten={}, loai={}, soTien={}, user={}",
                    saved.getMaKhoanThu(), saved.getTenKhoanThu(), tenLoai, saved.getSoTien(), user);
            auditLogService.log("Tạo", "Khoản thu",
                    "ma=" + saved.getMaKhoanThu() + ", ten=" + saved.getTenKhoanThu()
                    + ", loai=" + tenLoai + ", soTien=" + saved.getSoTien(), user);
            autoApplyNeuBatBuoc(saved);
        } else {
            log.info("[AUDIT] Sửa khoản thu: id={}, ma={}, ten={}, user={}",
                    saved.getId(), saved.getMaKhoanThu(), saved.getTenKhoanThu(), user);
            auditLogService.log("Sửa", "Khoản thu",
                    "id=" + saved.getId() + ", ma=" + saved.getMaKhoanThu()
                    + ", ten=" + saved.getTenKhoanThu(), user);
        }

        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        KhoanThu kt = findById(id);
        if (thanhToanRepository.existsByKhoanThuIdAndSoTienDaNopGreaterThan(id, BigDecimal.ZERO)) {
            throw new IllegalStateException(
                    "Không thể xóa khoản thu \"" + kt.getTenKhoanThu()
                    + "\" vì đã có hộ gia đình nộp tiền.");
        }
        // Xóa các bản ghi auto-tạo (soTienDaNop = 0) trước khi xóa khoản thu
        thanhToanRepository.deleteByKhoanThuId(id);
        khoanThuRepository.deleteById(id);
        String user = currentUser();
        log.info("[AUDIT] Xóa khoản thu: id={}, ma={}, ten={}, user={}",
                id, kt.getMaKhoanThu(), kt.getTenKhoanThu(), user);
        auditLogService.log("Xóa", "Khoản thu",
                "id=" + id + ", ma=" + kt.getMaKhoanThu() + ", ten=" + kt.getTenKhoanThu(), user);
    }

    private void validateMaKhoanThu(KhoanThu khoanThu) {
        khoanThuRepository.findByMaKhoanThu(khoanThu.getMaKhoanThu())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(khoanThu.getId())) {
                        throw new IllegalArgumentException(
                                "Mã khoản thu \"" + khoanThu.getMaKhoanThu() + "\" đã tồn tại.");
                    }
                });
    }

    private void autoApplyNeuBatBuoc(KhoanThu khoanThu) {
        LoaiKhoanThu loai = khoanThu.getLoaiKhoanThu();
        if (loai == null || loai.getLoaiApDung() == null || !loai.getLoaiApDung().isBatBuoc()) {
            return;
        }

        List<HoGiaDinh> tatCaHo = hoGiaDinhRepository.findAll();
        for (HoGiaDinh ho : tatCaHo) {
            BigDecimal soTienYeuCauCuaHo = tinhSoTienYeuCau(khoanThu, ho);
            ThanhToan tt = new ThanhToan();
            tt.setHoGiaDinh(ho);
            tt.setKhoanThu(khoanThu);
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setNgayNop(khoanThu.getKyThu());
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);
            tt.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);
            tt.setSoTienYeuCau(soTienYeuCauCuaHo);
            thanhToanRepository.save(tt);
            emailService.guiThongBaoKhoanThu(ho, khoanThu, soTienYeuCauCuaHo);
        }

        String user = currentUser();
        log.info("[AUDIT] Auto-apply khoản thu bắt buộc: id={}, ma={}, soHo={}, user={}",
                khoanThu.getId(), khoanThu.getMaKhoanThu(), tatCaHo.size(), user);
        auditLogService.log("Auto-apply", "Khoản thu",
                "id=" + khoanThu.getId() + ", ma=" + khoanThu.getMaKhoanThu()
                + ", áp dụng cho " + tatCaHo.size() + " hộ", user);
    }

    /**
     * Áp khoản thu bắt buộc đang tồn tại cho hộ gia đình mới được tạo.
     * Gọi sau khi lưu HoGiaDinh mới thành công.
     */
    @Transactional
    public void autoApplyForNewHo(HoGiaDinh ho) {
        List<KhoanThu> batBuocList = khoanThuRepository.findAll().stream()
                .filter(kt -> kt.getLoaiKhoanThu() != null
                        && kt.getLoaiKhoanThu().getLoaiApDung() != null
                        && kt.getLoaiKhoanThu().getLoaiApDung().isBatBuoc())
                .collect(java.util.stream.Collectors.toList());

        int count = 0;
        for (KhoanThu kt : batBuocList) {
            if (thanhToanRepository.existsByHoGiaDinhIdAndKhoanThuId(ho.getId(), kt.getId())) {
                continue;
            }
            ThanhToan tt = new ThanhToan();
            tt.setHoGiaDinh(ho);
            tt.setKhoanThu(kt);
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setNgayNop(kt.getKyThu());
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);
            tt.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);
            tt.setSoTienYeuCau(tinhSoTienYeuCau(kt, ho));
            thanhToanRepository.save(tt);
            count++;
        }

        if (count > 0) {
            String user = currentUser();
            log.info("[AUDIT] Auto-apply hộ mới: canHo={}, soKhoanThu={}, user={}",
                    ho.getSoCanHo(), count, user);
            auditLogService.log("Auto-apply", "Khoản thu",
                    "canHo=" + ho.getSoCanHo() + ", áp " + count + " khoản thu bắt buộc cho hộ mới", user);
        }
    }

    /** Tính soTienYeuCau: nếu donGiaPerM2 và dienTich đều có → diện tích × đơn giá, ngược lại null (fallback soTien). */
    private BigDecimal tinhSoTienYeuCau(KhoanThu kt, HoGiaDinh ho) {
        if (kt.getDonGiaPerM2() != null && ho.getDienTich() != null) {
            return ho.getDienTich().multiply(kt.getDonGiaPerM2());
        }
        return null;
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
