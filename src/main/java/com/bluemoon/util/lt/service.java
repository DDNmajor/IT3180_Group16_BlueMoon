package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.KhoanThu;
import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.enums.LoaiApDung;
import com.bluemoon.model.enums.PhuongThucThanhToan;
import com.bluemoon.model.enums.TrangThaiThanhToan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý các nghiệp vụ cốt lõi liên quan đến Thanh Toán và Dòng tiền.
 * Đảm bảo tính toàn vẹn dữ liệu (ACID) thông qua @Transactional.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThanhToanService {

    private final ThanhToanRepository thanhToanRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final KhoanThuRepository khoanThuRepository;
    private final KhoanThuService khoanThuService;
    private final AuditLogService auditLogService;

    /**
     * Ghi nhận thanh toán mới cho một hộ gia đình.
     * Xử lý rẽ nhánh logic: Nộp đủ, Nộp thiếu (Cộng dồn), Nộp thừa (Đóng dư).
     */
    @Transactional
    public ThanhToan ghiNhanThanhToan(Integer idHo, Integer idKhoan, BigDecimal soTienNop, 
                                      PhuongThucThanhToan phuongThuc, String nguoiThu) {
        
        ThanhToan thanhToan = findConNo(idHo, idKhoan);
        if (thanhToan == null) {
            throw new IllegalArgumentException("Không tìm thấy công nợ cho khoản thu này!");
        }

        BigDecimal soTienYeuCau = thanhToan.getSoTienYeuCauHieuLuc();
        BigDecimal tienDaNopCu = thanhToan.getSoTienDaNop() != null ? thanhToan.getSoTienDaNop() : BigDecimal.ZERO;
        BigDecimal tongTienSauNop = tienDaNopCu.add(soTienNop);

        thanhToan.setSoTienDaNop(tongTienSauNop);
        thanhToan.setPhuongThuc(phuongThuc);
        thanhToan.setNgayNop(LocalDate.now());
        // Set người thu (sẽ lấy từ Context Security khi gắn vào Controller)
        // thanhToan.setNguoiThu(...); 

        // Phân nhánh logic cập nhật trạng thái
        int compareResult = tongTienSauNop.compareTo(soTienYeuCau);
        if (compareResult == 0) {
            thanhToan.setTrangThai(TrangThaiThanhToan.DA_DONG);
        } else if (compareResult < 0) {
            thanhToan.setTrangThai(TrangThaiThanhToan.CON_NO);
        } else {
            thanhToan.setTrangThai(TrangThaiThanhToan.DONG_DU);
        }

        ThanhToan saved = thanhToanRepository.save(thanhToan);
        
        auditLogService.log("Thu tiền", "ThanhToan", 
            "Thu " + soTienNop + " VNĐ từ căn hộ " + thanhToan.getHoGiaDinh().getSoCanHo() + 
            " cho khoản " + thanhToan.getKhoanThu().getTenKhoanThu(), nguoiThu);
            
        return saved;
    }

    /**
     * Chức năng Nộp thêm (Dành cho trường hợp cư dân nộp lắt nhắt nhiều lần)
     */
    @Transactional
    public void nopThem(Integer idThanhToan, BigDecimal soTienThem, String nguoiThu) {
        ThanhToan tt = thanhToanRepository.findById(idThanhToan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi thanh toán"));

        if (tt.getTrangThai() != TrangThaiThanhToan.CON_NO) {
            throw new IllegalStateException("Chỉ có thể nộp thêm cho các khoản CÒN NỢ.");
        }

        BigDecimal current = tt.getSoTienDaNop() != null ? tt.getSoTienDaNop() : BigDecimal.ZERO;
        tt.setSoTienDaNop(current.add(soTienThem));
        
        if (tt.getSoTienDaNop().compareTo(tt.getSoTienYeuCauHieuLuc()) >= 0) {
            tt.setTrangThai(tt.getSoTienDaNop().compareTo(tt.getSoTienYeuCauHieuLuc()) == 0 ? 
                            TrangThaiThanhToan.DA_DONG : TrangThaiThanhToan.DONG_DU);
        }

        thanhToanRepository.save(tt);
        auditLogService.log("Nộp thêm", "ThanhToan", "Nộp thêm " + soTienThem + " VNĐ (ID: " + idThanhToan + ")", nguoiThu);
    }

    /**
     * Báo đã hoàn tiền (Reset trạng thái ĐÓNG DƯ về ĐÃ ĐÓNG sau khi kế toán trả lại tiền thừa)
     */
    @Transactional
    public void baoDaHoanTien(Integer idThanhToan, String nguoiThu) {
        ThanhToan tt = thanhToanRepository.findById(idThanhToan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi thanh toán"));

        if (tt.getTrangThai() != TrangThaiThanhToan.DONG_DU) {
            throw new IllegalStateException("Chỉ áp dụng cho trạng thái ĐÓNG DƯ.");
        }

        // Đưa số tiền đã nộp về đúng bằng số tiền yêu cầu
        tt.setSoTienDaNop(tt.getSoTienYeuCauHieuLuc());
        tt.setTrangThai(TrangThaiThanhToan.DA_DONG);
        
        thanhToanRepository.save(tt);
        auditLogService.log("Hoàn tiền", "ThanhToan", "Hoàn tiền thừa cho ID: " + idThanhToan, nguoiThu);
    }

    /**
     * Xóa hoặc Reset giao dịch. 
     * - Khoản bắt buộc: Khôi phục về CON_NO (trả về false)
     * - Khoản tự nguyện: Xóa hẳn khỏi DB (trả về true)
     */
    @Transactional
    public boolean delete(Integer id, String nguoiThu) {
        ThanhToan tt = thanhToanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi thanh toán"));

        boolean isBatBuoc = tt.getKhoanThu().getLoaiKhoanThu().getLoaiApDung().isBatBuoc();

        if (isBatBuoc) {
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);
            tt.setNgayNop(null);
            thanhToanRepository.save(tt);
            auditLogService.log("Reset giao dịch", "ThanhToan", "Reset giao dịch lỗi về CON_NO (ID: " + id + ")", nguoiThu);
            return false;
        } else {
            thanhToanRepository.delete(tt);
            auditLogService.log("Hủy giao dịch", "ThanhToan", "Xóa hoàn toàn giao dịch tự nguyện (ID: " + id + ")", nguoiThu);
            return true;
        }
    }

    /**
     * Tìm bản ghi công nợ hiện tại của một hộ đối với 1 khoản thu nhất định
     */
    public ThanhToan findConNo(Integer idHo, Integer idKhoan) {
        return thanhToanRepository.findByHoGiaDinh_IdAndKhoanThu_Id(idHo, idKhoan).orElse(null);
    }

    /**
     * Tự động gán nợ (Auto-apply) cho toàn bộ các hộ gia đình 
     * khi Kế toán tạo một khoản thu Bắt buộc mới.
     */
    @Transactional
    public void autoApplyForAllHo(KhoanThu khoanThu) {
        if (!khoanThu.getLoaiKhoanThu().getLoaiApDung().isBatBuoc()) {
            return; // Không áp dụng tự động cho khoản tự nguyện
        }

        List<HoGiaDinh> allHo = hoGiaDinhRepository.findAll();
        List<ThanhToan> danhSachNoMoi = new ArrayList<>();

        for (HoGiaDinh ho : allHo) {
            BigDecimal soTienYc = khoanThuService.tinhSoTienYeuCau(khoanThu, ho);
            
            // Bỏ qua nếu là phí theo người nhưng hộ không có nhân khẩu nào
            if (soTienYc.compareTo(BigDecimal.ZERO) <= 0) continue;

            ThanhToan tt = new ThanhToan();
            tt.setKhoanThu(khoanThu);
            tt.setHoGiaDinh(ho);
            tt.setSoTienYeuCau(soTienYc);
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);
            
            danhSachNoMoi.add(tt);
        }

        if (!danhSachNoMoi.isEmpty()) {
            thanhToanRepository.saveAll(danhSachNoMoi);
            log.info("[AUTO-APPLY] Đã gán nợ thành công cho {} hộ gia đình đối với Khoản thu: {}", 
                     danhSachNoMoi.size(), khoanThu.getMaKhoanThu());
        }
    }

    /**
     * Tự động gán các khoản nợ hiện hành cho một hộ gia đình vừa mới chuyển đến
     * (thường được gọi khi Import Excel hộ mới).
     */
    @Transactional
    public void autoApplyForNewHo(HoGiaDinh ho) {
        List<KhoanThu> cacKhoanBatBuoc = khoanThuRepository.findActiveBatBuoc();
        List<ThanhToan> danhSachNoMoi = new ArrayList<>();

        for (KhoanThu kt : cacKhoanBatBuoc) {
            BigDecimal soTienYc = khoanThuService.tinhSoTienYeuCau(kt, ho);
            if (soTienYc.compareTo(BigDecimal.ZERO) <= 0) continue;

            ThanhToan tt = new ThanhToan();
            tt.setKhoanThu(kt);
            tt.setHoGiaDinh(ho);
            tt.setSoTienYeuCau(soTienYc);
            tt.setSoTienDaNop(BigDecimal.ZERO);
            tt.setTrangThai(TrangThaiThanhToan.CON_NO);

            danhSachNoMoi.add(tt);
        }

        if (!danhSachNoMoi.isEmpty()) {
            thanhToanRepository.saveAll(danhSachNoMoi);
            log.info("[AUTO-APPLY] Đã gán {} khoản nợ hiện hành cho hộ mới: {}", 
                     danhSachNoMoi.size(), ho.getSoCanHo());
        }
    }
}