package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.dto.NoPhiChiTietDto;
import com.bluemoon.dto.NoPhiHoDto;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.KhoanThu;
import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.enums.TrangThaiThanhToan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service chuyên biệt phục vụ bóc tách dữ liệu cho Dashboard và Báo cáo nợ phí.
 * Sử dụng readOnly = true để tối ưu hiệu năng (Performance Optimization) cho các query SELECT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BaoCaoThanhToanService {

    private final ThanhToanRepository thanhToanRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final KhoanThuRepository khoanThuRepository;

    /**
     * Tổng hợp danh sách các hộ gia đình đang nợ phí.
     * Ánh xạ (Map) dữ liệu từ Entity sang DTO để bảo mật và giảm tải payload.
     */
    public List<NoPhiHoDto> getDanhSachNoPhi() {
        List<ThanhToan> danhSachNo = thanhToanRepository.findByTrangThai(TrangThaiThanhToan.CON_NO);
        
        // Gom nhóm các khoản nợ theo từng hộ gia đình
        Map<HoGiaDinh, List<ThanhToan>> groupedByHo = danhSachNo.stream()
                .collect(Collectors.groupingBy(ThanhToan::getHoGiaDinh));

        List<NoPhiHoDto> result = new ArrayList<>();

        for (Map.Entry<HoGiaDinh, List<ThanhToan>> entry : groupedByHo.entrySet()) {
            HoGiaDinh ho = entry.getKey();
            List<ThanhToan> listNo = entry.getValue();

            NoPhiHoDto dto = new NoPhiHoDto();
            dto.setIdHoGiaDinh(ho.getId());
            dto.setSoCanHo(ho.getSoCanHo());
            dto.setChuHo(ho.getChuHo());
            
            BigDecimal tongNo = BigDecimal.ZERO;
            List<NoPhiChiTietDto> chiTietList = new ArrayList<>();

            for (ThanhToan tt : listNo) {
                KhoanThu kt = tt.getKhoanThu();
                BigDecimal tienYeuCau = tt.getSoTienYeuCauHieuLuc();
                BigDecimal tienDaNop = tt.getSoTienDaNop() != null ? tt.getSoTienDaNop() : BigDecimal.ZERO;
                BigDecimal conThieu = tienYeuCau.subtract(tienDaNop);
                
                tongNo = tongNo.add(conThieu);

                NoPhiChiTietDto chiTiet = new NoPhiChiTietDto(
                        kt.getId(), kt.getTenKhoanThu(), tienYeuCau, tienDaNop, conThieu, kt.getHanNop()
                );
                chiTietList.add(chiTiet);
            }

            dto.setTongTienNo(tongNo);
            dto.setChiTietNo(chiTietList);
            result.add(dto);
        }

        // Sắp xếp giảm dần theo tổng tiền nợ
        result.sort((a, b) -> b.getTongTienNo().compareTo(a.getTongTienNo()));
        return result;
    }

    /**
     * Đếm tổng số hộ đang nợ ít nhất 1 khoản bắt buộc để hiển thị lên Dashboard.
     */
    public long tongSoHoDangNoItNhatMotKhoanTrongThongKe() {
        return thanhToanRepository.findByTrangThai(TrangThaiThanhToan.CON_NO).stream()
                .map(tt -> tt.getHoGiaDinh().getId())
                .distinct()
                .count();
    }
}