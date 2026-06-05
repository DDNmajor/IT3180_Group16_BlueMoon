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

        if (isNew) {
            log.info("[AUDIT] Tạo khoản thu: ma={}, ten={}, loai={}, soTien={}, user={}",
                    saved.getMaKhoanThu(), saved.getTenKhoanThu(),
                    saved.getLoaiKhoanThu() != null ? saved.getLoaiKhoanThu().getTenLoai() : "?",
                    saved.getSoTien(), currentUser());
            autoApplyNeuBatBuoc(saved);
        } else {
            log.info("[AUDIT] Sửa khoản thu: id={}, ma={}, ten={}, user={}",
                    saved.getId(), saved.getMaKhoanThu(), saved.getTenKhoanThu(), currentUser());
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
        log.info("[AUDIT] Xóa khoản thu: id={}, ma={}, ten={}, user={}",
                id, kt.getMaKhoanThu(), kt.getTenKhoanThu(), currentUser());
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
            ThanhToan tt = new ThanhToan();
            tt.setHoGiaDinh(ho);
            tt.setKhoanThu(khoanThu);
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setNgayNop(khoanThu.getKyThu());
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);
            tt.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);
            thanhToanRepository.save(tt);
        }

        log.info("[AUDIT] Auto-apply khoản thu bắt buộc: id={}, ma={}, soHo={}, user={}",
                khoanThu.getId(), khoanThu.getMaKhoanThu(), tatCaHo.size(), currentUser());
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
