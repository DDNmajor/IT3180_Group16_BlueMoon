package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.LoaiKhoanThuRepository;
import com.bluemoon.dao.PhuongTienRepository;
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
    private final PhuongTienRepository   phuongTienRepository;
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
        kt.setDeletedAt(LocalDateTime.now());
        khoanThuRepository.save(kt);
        String user = currentUser();
        log.info("[AUDIT] Chuyển vào thùng rác — khoản thu: id={}, ma={}, ten={}, user={}",
                id, kt.getMaKhoanThu(), kt.getTenKhoanThu(), user);
        auditLogService.log("Xóa", "Khoản thu",
                "id=" + id + ", ma=" + kt.getMaKhoanThu() + ", ten=" + kt.getTenKhoanThu() + " (thùng rác)", user);
    }

    public List<KhoanThu> findAllDeleted() {
        return khoanThuRepository.findAllDeleted();
    }

    @Transactional
    public void restore(Integer id) {
        KhoanThu kt = khoanThuRepository.findDeletedById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trong thùng rác id=" + id));
        khoanThuRepository.restoreById(id);
        String user = currentUser();
        log.info("[AUDIT] Khôi phục khoản thu: id={}, ma={}, ten={}, user={}",
                id, kt.getMaKhoanThu(), kt.getTenKhoanThu(), user);
        auditLogService.log("Khôi phục", "Khoản thu",
                "id=" + id + ", ma=" + kt.getMaKhoanThu() + ", ten=" + kt.getTenKhoanThu(), user);
    }

    @Transactional
    public void permanentDelete(Integer id) {
        KhoanThu kt = khoanThuRepository.findDeletedById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trong thùng rác id=" + id));
        // native query vì KhoanThu đang bị ẩn bởi @SQLRestriction
        thanhToanRepository.hardDeleteByKhoanThuId(id);
        khoanThuRepository.permanentDeleteById(id);
        String user = currentUser();
        log.info("[AUDIT] Xóa vĩnh viễn khoản thu: id={}, ma={}, ten={}, user={}",
                id, kt.getMaKhoanThu(), kt.getTenKhoanThu(), user);
        auditLogService.log("Xóa vĩnh viễn", "Khoản thu",
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

        boolean isPerXe = khoanThu.getLoaiTinhPhi() == LoaiTinhPhi.PER_XE;

        List<HoGiaDinh> tatCaHo = hoGiaDinhRepository.findAll();
        int count = 0;
        for (HoGiaDinh ho : tatCaHo) {
            BigDecimal soTienYeuCauCuaHo = tinhSoTienYeuCau(khoanThu, ho);

            // hộ không có xe → soTien = 0, bỏ qua
            if (isPerXe && (soTienYeuCauCuaHo == null
                    || soTienYeuCauCuaHo.compareTo(BigDecimal.ZERO) == 0)) {
                continue;
            }

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
            count++;
        }

        String user = currentUser();
        log.info("[AUDIT] Auto-apply khoản thu bắt buộc: id={}, ma={}, soHo={}, user={}",
                khoanThu.getId(), khoanThu.getMaKhoanThu(), count, user);
        auditLogService.log("Auto-apply", "Khoản thu",
                "id=" + khoanThu.getId() + ", ma=" + khoanThu.getMaKhoanThu()
                + ", áp dụng cho " + count + " hộ", user);
    }

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
            // hộ mới chưa đăng ký xe, bỏ qua PER_XE
            if (kt.getLoaiTinhPhi() == LoaiTinhPhi.PER_XE) {
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

    public BigDecimal tinhSoTienYeuCau(KhoanThu kt, HoGiaDinh ho) {
        LoaiTinhPhi ltp = kt.getLoaiTinhPhi() != null ? kt.getLoaiTinhPhi() : LoaiTinhPhi.FIXED;
        if (ltp == LoaiTinhPhi.PER_M2) {
            if (kt.getDonGiaPerM2() != null && ho.getDienTich() != null) {
                return ho.getDienTich().multiply(kt.getDonGiaPerM2());
            }
            return null;
        }
        if (ltp == LoaiTinhPhi.PER_XE) {
            long soXeMay = phuongTienRepository.countByHoGiaDinhIdAndLoaiXe(ho.getId(), LoaiXe.XEMAY);
            long soOto   = phuongTienRepository.countByHoGiaDinhIdAndLoaiXe(ho.getId(), LoaiXe.OTO);
            long giaXeMay = kt.getGiaXeMay() != null ? kt.getGiaXeMay().longValue() : 70_000L;
            long giaOto   = kt.getGiaOto()   != null ? kt.getGiaOto().longValue()   : 1_200_000L;
            return BigDecimal.valueOf(soXeMay * giaXeMay + soOto * giaOto);
        }
        // null = fallback về kt.soTien
        return null;
    }

    // soTienDaNop=0 → tự động cập nhật; >0 → không sửa, ghi audit cần xét lại
    // dienTichCu dùng để ghi audit rõ nguyên nhân thay đổi
    @Transactional
    public int recalculatePerM2ForHo(HoGiaDinh ho, BigDecimal dienTichCu) {
        List<ThanhToan> list = thanhToanRepository
                .findByHoGiaDinhIdAndKhoanThuLoaiTinhPhi(ho.getId(), LoaiTinhPhi.PER_M2);

        int flagged = 0;
        String user = currentUser();
        for (ThanhToan tt : list) {
            if (tt.getTrangThai() == TrangThaiThanhToan.DA_DONG
                    || tt.getTrangThai() == TrangThaiThanhToan.DONG_DU) continue;

            BigDecimal soMoi    = tinhSoTienYeuCau(tt.getKhoanThu(), ho);
            BigDecimal soHienTai = tt.getSoTienYeuCauHieuLuc();

            if (tt.getSoTienDaNop().compareTo(BigDecimal.ZERO) == 0) {
                tt.setSoTienYeuCau(soMoi);
                thanhToanRepository.save(tt);
                log.info("[AUDIT] Điều chỉnh phí m²: ttId={}, canHo={}, dienTich={}→{}, soTien={}→{}, user={}",
                        tt.getId(), ho.getSoCanHo(), dienTichCu, ho.getDienTich(), soHienTai, soMoi, user);
                auditLogService.log("Điều chỉnh", "Thanh toán",
                        "id=" + tt.getId() + ", canHo=" + ho.getSoCanHo()
                        + ", diện tích " + dienTichCu + "→" + ho.getDienTich() + " m²"
                        + ", phí " + soHienTai + "→" + soMoi, user);
            } else {
                flagged++;
                log.info("[AUDIT] Cần xét lại phí m²: ttId={}, canHo={}, dienTich={}→{}, soTienMoi={}, soTienDaNop={}, user={}",
                        tt.getId(), ho.getSoCanHo(), dienTichCu, ho.getDienTich(), soMoi, tt.getSoTienDaNop(), user);
                auditLogService.log("Cần xét lại", "Thanh toán",
                        "id=" + tt.getId() + ", canHo=" + ho.getSoCanHo()
                        + ", diện tích " + dienTichCu + "→" + ho.getDienTich() + " m²"
                        + ", phí tính lại=" + soMoi + ", đã nộp=" + tt.getSoTienDaNop(), user);
            }
        }
        return flagged;
    }

    // applyCurrentMonth=false → ghi log, không sửa; thay đổi có hiệu lực tháng sau
    // nguon mô tả nguyên nhân để audit log rõ ràng (vd: "thêm xe mới", "xóa xe nhập sai")
    // forceUpdate=true (nhập sai) → luôn cập nhật kể cả đang nộp dở, vì dữ liệu sai từ đầu
    // Khi phí TĂNG: cập nhật kể cả đang nộp dở (hộ thêm xe → hợp lệ phải nộp thêm)
    // Khi phí GIẢM + đang nộp dở + không forceUpdate: chỉ flag, không tự sửa
    @Transactional
    public int recalculatePerXeForHo(HoGiaDinh ho, boolean applyCurrentMonth, String nguon, boolean forceUpdate) {
        String user = currentUser();
        if (!applyCurrentMonth) {
            log.info("[INFO] PER_XE thay đổi cho hộ {} ({}): áp dụng từ tháng sau", ho.getSoCanHo(), nguon);
            return 0;
        }

        YearMonth ym = YearMonth.now();
        LocalDate from = ym.atDay(1);
        LocalDate to   = ym.atEndOfMonth();

        List<ThanhToan> list = thanhToanRepository
                .findByHoGiaDinhIdAndKhoanThuLoaiTinhPhiAndKhoanThuKyThuBetween(
                        ho.getId(), LoaiTinhPhi.PER_XE, from, to);

        int flagged = 0;
        for (ThanhToan tt : list) {
            if (tt.getTrangThai() == TrangThaiThanhToan.DA_DONG
                    || tt.getTrangThai() == TrangThaiThanhToan.DONG_DU) continue;

            BigDecimal soMoi    = tinhSoTienYeuCau(tt.getKhoanThu(), ho);
            BigDecimal soHienTai = tt.getSoTienYeuCauHieuLuc();
            boolean tangPhi = soMoi != null && soMoi.compareTo(soHienTai) > 0;

            if (tt.getSoTienDaNop().compareTo(BigDecimal.ZERO) == 0) {
                if (soMoi == null || soMoi.compareTo(BigDecimal.ZERO) == 0) {
                    thanhToanRepository.delete(tt);
                    log.info("[AUDIT] Xóa phí xe (không còn xe): ttId={}, canHo={}, nguon={}, user={}",
                            tt.getId(), ho.getSoCanHo(), nguon, user);
                    auditLogService.log("Xóa", "Thanh toán",
                            "id=" + tt.getId() + ", canHo=" + ho.getSoCanHo()
                            + ", không còn phương tiện (" + nguon + ")", user);
                } else {
                    tt.setSoTienYeuCau(soMoi);
                    thanhToanRepository.save(tt);
                    log.info("[AUDIT] Điều chỉnh phí xe: ttId={}, canHo={}, soTien={}→{}, nguon={}, user={}",
                            tt.getId(), ho.getSoCanHo(), soHienTai, soMoi, nguon, user);
                    auditLogService.log("Điều chỉnh", "Thanh toán",
                            "id=" + tt.getId() + ", canHo=" + ho.getSoCanHo()
                            + ", phí xe " + soHienTai + "→" + soMoi + " (" + nguon + ")", user);
                }
            } else if (tangPhi || forceUpdate) {
                // phí tăng (thêm xe) hoặc forceUpdate (nhập sai) → cập nhật ngay
                BigDecimal soTienMoi = (soMoi != null && soMoi.compareTo(BigDecimal.ZERO) > 0) ? soMoi : null;
                BigDecimal yeuCauHieuLuc = soTienMoi != null ? soTienMoi : tt.getKhoanThu().getSoTien();
                int cmpNop = tt.getSoTienDaNop().compareTo(yeuCauHieuLuc);
                TrangThaiThanhToan trangThaiMoi = cmpNop == 0 ? TrangThaiThanhToan.DA_DONG
                        : cmpNop < 0 ? TrangThaiThanhToan.CON_NO : TrangThaiThanhToan.DONG_DU;
                tt.setSoTienYeuCau(soTienMoi);
                tt.setTrangThai(trangThaiMoi);
                thanhToanRepository.save(tt);
                log.info("[AUDIT] Điều chỉnh phí xe (đang nộp dở): ttId={}, canHo={}, soTien={}→{}, daNop={}, trangThai={}, nguon={}, user={}",
                        tt.getId(), ho.getSoCanHo(), soHienTai, soTienMoi, tt.getSoTienDaNop(), trangThaiMoi, nguon, user);
                auditLogService.log("Điều chỉnh", "Thanh toán",
                        "id=" + tt.getId() + ", canHo=" + ho.getSoCanHo()
                        + ", phí xe " + soHienTai + "→" + soTienMoi
                        + ", đã nộp=" + tt.getSoTienDaNop() + " (" + nguon + ")", user);
            } else {
                // phí giảm + đang nộp dở + không phải nhập sai → staff xét lại
                flagged++;
                log.info("[AUDIT] Cần xét lại phí xe: ttId={}, canHo={}, soTienMoi={}, soTienDaNop={}, nguon={}, user={}",
                        tt.getId(), ho.getSoCanHo(), soMoi, tt.getSoTienDaNop(), nguon, user);
                auditLogService.log("Cần xét lại", "Thanh toán",
                        "id=" + tt.getId() + ", canHo=" + ho.getSoCanHo()
                        + ", phí giảm " + soHienTai + "→" + soMoi
                        + ", đã nộp=" + tt.getSoTienDaNop() + " (" + nguon + ")", user);
            }
        }

        // tạo ThanhToan mới nếu hộ chưa có (thêm xe lần đầu)
        List<KhoanThu> perXeThang = khoanThuRepository.findAll().stream()
                .filter(kt -> kt.getLoaiTinhPhi() == LoaiTinhPhi.PER_XE
                        && kt.getLoaiKhoanThu() != null
                        && kt.getLoaiKhoanThu().getLoaiApDung() != null
                        && kt.getLoaiKhoanThu().getLoaiApDung().isBatBuoc()
                        && kt.getKyThu() != null
                        && !kt.getKyThu().isBefore(from) && !kt.getKyThu().isAfter(to))
                .collect(Collectors.toList());

        for (KhoanThu kt : perXeThang) {
            if (thanhToanRepository.existsByHoGiaDinhIdAndKhoanThuId(ho.getId(), kt.getId())) continue;
            BigDecimal soTien = tinhSoTienYeuCau(kt, ho);
            if (soTien == null || soTien.compareTo(BigDecimal.ZERO) == 0) continue;
            ThanhToan tt = new ThanhToan();
            tt.setHoGiaDinh(ho);
            tt.setKhoanThu(kt);
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setNgayNop(kt.getKyThu());
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);
            tt.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);
            tt.setSoTienYeuCau(soTien);
            thanhToanRepository.save(tt);
            log.info("[AUDIT] Tạo phí xe mới: canHo={}, khoanThu={}, soTien={}, nguon={}, user={}",
                    ho.getSoCanHo(), kt.getMaKhoanThu(), soTien, nguon, user);
            auditLogService.log("Tạo", "Thanh toán",
                    "canHo=" + ho.getSoCanHo() + ", khoanThu=" + kt.getMaKhoanThu()
                    + ", soTien=" + soTien + " (" + nguon + ")", user);
        }

        return flagged;
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
