package com.bluemoon.service;

import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.TrangThaiThanhToan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThanhToanService {

    private final ThanhToanRepository thanhToanRepository;

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

    /** Kiểm tra hộ đã hoàn tất (DA_DONG hoặc DONG_DU) khoản thu này chưa — dùng cho anti-duplicate */
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
        if (thanhToan.getNgayNop() == null) {
            thanhToan.setNgayNop(LocalDate.now());
        }
        if (thanhToan.getKhoanThu() != null && thanhToan.getSoTienDaNop() != null) {
            thanhToan.setTrangThai(
                    tinhTrangThai(thanhToan.getSoTienDaNop(), thanhToan.getKhoanThu().getSoTien()));
        }
        ThanhToan saved = thanhToanRepository.save(thanhToan);
        log.info("[AUDIT] Ghi nhận thanh toán: id={}, canHo={}, khoanThu={}, soTien={}, trangThai={}, phuongThuc={}, nguoiThu={}",
                saved.getId(),
                saved.getHoGiaDinh()  != null ? saved.getHoGiaDinh().getSoCanHo()      : "?",
                saved.getKhoanThu()   != null ? saved.getKhoanThu().getTenKhoanThu()    : "?",
                saved.getSoTienDaNop(),
                saved.getTrangThai(),
                saved.getPhuongThuc(),
                saved.getNguoiThu()   != null ? saved.getNguoiThu().getTenDangNhap()    : "?");
        return saved;
    }

    @Transactional
    public ThanhToan nopThem(Integer id, BigDecimal soTienThem) {
        ThanhToan tt = findById(id);
        BigDecimal tongMoi = tt.getSoTienDaNop().add(soTienThem);
        tt.setSoTienDaNop(tongMoi);
        tt.setTrangThai(tinhTrangThai(tongMoi, tt.getKhoanThu().getSoTien()));
        ThanhToan saved = thanhToanRepository.save(tt);
        log.info("[AUDIT] Nộp thêm: id={}, canHo={}, soTienThem={}, tongMoi={}, trangThai={}",
                id, tt.getHoGiaDinh() != null ? tt.getHoGiaDinh().getSoCanHo() : "?",
                soTienThem, tongMoi, saved.getTrangThai());
        return saved;
    }

    @Transactional
    public ThanhToan baoDaHoanTien(Integer id) {
        ThanhToan tt = findById(id);
        tt.setTrangThai(TrangThaiThanhToan.DA_DONG);
        ThanhToan saved = thanhToanRepository.save(tt);
        log.info("[AUDIT] Báo đã hoàn tiền: id={}, canHo={}",
                id, tt.getHoGiaDinh() != null ? tt.getHoGiaDinh().getSoCanHo() : "?");
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        ThanhToan tt = findById(id);
        thanhToanRepository.deleteById(id);
        log.info("[AUDIT] Xóa thanh toán: id={}, canHo={}, khoanThu={}",
                id,
                tt.getHoGiaDinh() != null ? tt.getHoGiaDinh().getSoCanHo()   : "?",
                tt.getKhoanThu()  != null ? tt.getKhoanThu().getTenKhoanThu() : "?");
    }
}
