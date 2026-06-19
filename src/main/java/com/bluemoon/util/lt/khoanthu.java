package com.bluemoon.service;

import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.KhoanThu;
import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.enums.LoaiXe;
import com.bluemoon.model.enums.TinhTrangCuTru;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service đóng vai trò là Động cơ tính phí (Calculation Engine).
 * Chứa logic nội suy phức tạp cho các biểu phí của chung cư.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KhoanThuService {

    private final KhoanThuRepository khoanThuRepository;
    private final ThanhToanRepository thanhToanRepository;

    /**
     * Tính số tiền yêu cầu của 1 khoản thu áp dụng lên 1 hộ gia đình.
     * Thuật toán thay đổi linh hoạt theo LoaiTinhPhi được cấu hình.
     */
    public BigDecimal tinhSoTienYeuCau(KhoanThu khoanThu, HoGiaDinh ho) {
        return switch (khoanThu.getLoaiTinhPhi()) {
            case FIXED -> khoanThu.getSoTien() != null ? khoanThu.getSoTien() : BigDecimal.ZERO;
            
            case PER_M2 -> {
                if (ho.getDienTich() == null || khoanThu.getDonGiaPerM2() == null) yield BigDecimal.ZERO;
                yield ho.getDienTich().multiply(khoanThu.getDonGiaPerM2());
            }
            
            case PER_PERSON -> {
                if (khoanThu.getSoTien() == null) yield BigDecimal.ZERO;
                // Đếm số người đang ở thực tế (Bỏ qua những người đã chuyển đi)
                long soNguoi = ho.getNhanKhaus().stream()
                        .filter(nk -> nk.getTinhTrang() != TinhTrangCuTru.CHUYEN_DI)
                        .count();
                yield khoanThu.getSoTien().multiply(BigDecimal.valueOf(soNguoi));
            }
            
            case PER_XE -> {
                long soXeMay = ho.getPhuongTiens().stream().filter(xe -> xe.getLoaiXe() == LoaiXe.XEMAY).count();
                long soOto = ho.getPhuongTiens().stream().filter(xe -> xe.getLoaiXe() == LoaiXe.OTO).count();
                
                // Fallback về giá mặc định nếu admin để trống
                BigDecimal giaXeMay = khoanThu.getGiaXeMay() != null ? khoanThu.getGiaXeMay() : BigDecimal.valueOf(70000);
                BigDecimal giaOto = khoanThu.getGiaOto() != null ? khoanThu.getGiaOto() : BigDecimal.valueOf(1200000);
                
                BigDecimal tongXeMay = giaXeMay.multiply(BigDecimal.valueOf(soXeMay));
                BigDecimal tongOto = giaOto.multiply(BigDecimal.valueOf(soOto));
                yield tongXeMay.add(tongOto);
            }
        };
    }

    /**
     * Logic thông minh: Kích hoạt khi có Biến động phương tiện (Thêm/Sửa/Xóa xe).
     * Tìm khoản thu phí gửi xe của tháng HIỆN TẠI và tính lại tiền.
     */
    @Transactional
    public void recalculatePerXeForHo(HoGiaDinh ho) {
        LocalDate now = LocalDate.now();
        // Lấy tất cả khoản thu mang loại tính phí PER_XE trong tháng hiện tại
        var cacKhoanPhiXeThangNay = khoanThuRepository.findKhoanThuXeThangHienTai(now.getMonthValue(), now.getYear());

        for (KhoanThu kt : cacKhoanPhiXeThangNay) {
            ThanhToan tt = thanhToanRepository.findByHoGiaDinh_IdAndKhoanThu_Id(ho.getId(), kt.getId()).orElse(null);
            
            BigDecimal soTienMoi = tinhSoTienYeuCau(kt, ho);

            if (tt != null) {
                // Đã có record thanh toán -> Cập nhật đè số tiền mới
                tt.setSoTienYeuCau(soTienMoi);
                thanhToanRepository.save(tt);
                log.info("[CALC-ENGINE] Đã cập nhật lại phí xe tháng này cho căn hộ {}", ho.getSoCanHo());
            } else if (soTienMoi.compareTo(BigDecimal.ZERO) > 0) {
                // Chưa có nhưng nay mua xe -> Sinh nợ mới
                tt = new ThanhToan();
                tt.setKhoanThu(kt);
                tt.setHoGiaDinh(ho);
                tt.setSoTienYeuCau(soTienMoi);
                tt.setSoTienDaNop(BigDecimal.ZERO);
                // ... set các trạng thái khác
                thanhToanRepository.save(tt);
            }
        }
    }
}