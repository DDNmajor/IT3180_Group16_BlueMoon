package com.bluemoon.service;

import com.bluemoon.dao.PhuongTienRepository;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.PhuongTien;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhuongTienService {

    private final PhuongTienRepository phuongTienRepository;
    private final AuditLogService      auditLogService;
    private final KhoanThuService      khoanThuService;

    public List<PhuongTien> findByHoGiaDinh(Integer idHo) {
        return phuongTienRepository.findByHoGiaDinhId(idHo);
    }

    public PhuongTien findById(Integer id) {
        return phuongTienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương tiện id=" + id));
    }

    public boolean existsByBienSo(String bienSo) {
        return phuongTienRepository.existsByBienSo(bienSo);
    }

    public boolean existsByBienSoForOther(String bienSo, Integer id) {
        return phuongTienRepository.existsByBienSoAndIdNot(bienSo, id);
    }

    @Transactional
    public int save(PhuongTien phuongTien, String lyDo) {
        boolean isNew = (phuongTien.getId() == null);
        PhuongTien saved = phuongTienRepository.save(phuongTien);
        String action = isNew ? "Tạo" : "Sửa";
        String user = currentUser();
        String canHo = saved.getHoGiaDinh() != null ? saved.getHoGiaDinh().getSoCanHo() : "?";
        log.info("[AUDIT] {} phương tiện: bienSo={}, loai={}, canHo={}, lyDo={}, user={}",
                action, saved.getBienSo(), saved.getLoaiXe(), canHo, lyDo, user);
        auditLogService.log(action, "Phương tiện",
                "bienSo=" + saved.getBienSo() + ", loai=" + saved.getLoaiXe().getTenHienThi()
                + ", canHo=" + canHo + ", lyDo=" + lyDo, user);

        HoGiaDinh ho = saved.getHoGiaDinh();
        if (ho == null) return 0;

        // NHAP_SAI: luôn tính lại tháng này; DANG_KY_THEM: chỉ tính nếu <= ngày 25; sửa: luôn tính lại
        boolean applyCurrentMonth = !isNew
                || "NHAP_SAI".equals(lyDo)
                || LocalDate.now().getDayOfMonth() <= 25;
        String nguon = isNew
                ? ("NHAP_SAI".equals(lyDo) ? "bổ sung thiếu thông tin" : "đăng ký xe mới")
                : "sửa thông tin xe";
        // NHAP_SAI khi thêm → forceUpdate để trừ đúng dù đang nộp dở
        boolean forceUpdate = "NHAP_SAI".equals(lyDo);
        return khoanThuService.recalculatePerXeForHo(ho, applyCurrentMonth, nguon, forceUpdate);
    }

    @Transactional
    public int delete(Integer id, String lyDo) {
        PhuongTien pt = findById(id);
        HoGiaDinh ho = pt.getHoGiaDinh();

        phuongTienRepository.deleteById(id);
        String user = currentUser();
        String canHo = ho != null ? ho.getSoCanHo() : "?";
        log.info("[AUDIT] Xóa phương tiện (lyDo={}): bienSo={}, loai={}, canHo={}, user={}",
                lyDo, pt.getBienSo(), pt.getLoaiXe(), canHo, user);
        auditLogService.log("Xóa", "Phương tiện",
                "bienSo=" + pt.getBienSo() + ", loai=" + pt.getLoaiXe().getTenHienThi()
                + ", canHo=" + canHo + ", lyDo=" + lyDo, user);

        if (ho == null) return 0;

        // nhập sai: luôn tính lại + forceUpdate; bớt xe: chỉ tính lại nếu trước ngày 5
        boolean applyCurrentMonth = "NHAP_SAI".equals(lyDo)
                || LocalDate.now().getDayOfMonth() <= 5;
        String nguon = "NHAP_SAI".equals(lyDo) ? "xóa xe nhập sai" : "bán xe / thôi gửi xe";
        boolean forceUpdate = "NHAP_SAI".equals(lyDo);
        return khoanThuService.recalculatePerXeForHo(ho, applyCurrentMonth, nguon, forceUpdate);
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
