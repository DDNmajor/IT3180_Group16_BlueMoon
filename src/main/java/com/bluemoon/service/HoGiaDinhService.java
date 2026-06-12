package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.TrangThaiThanhToan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoGiaDinhService {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final NhanKhauRepository  nhanKhauRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final AuditLogService     auditLogService;
    private final KhoanThuService     khoanThuService;

    public List<HoGiaDinh> findAll() {
        return hoGiaDinhRepository.findAll();
    }

    public HoGiaDinh findById(Integer id) {
        return hoGiaDinhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hộ gia đình id=" + id));
    }

    public HoGiaDinh findBySoCanHo(String soCanHo) {
        return hoGiaDinhRepository.findBySoCanHo(soCanHo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy căn hộ: " + soCanHo));
    }

    public List<HoGiaDinh> search(String keyword) {
        Set<HoGiaDinh> results = new LinkedHashSet<>();
        results.addAll(hoGiaDinhRepository
                .findBySoCanHoContainingIgnoreCaseOrChuHoContainingIgnoreCase(keyword, keyword));
        results.addAll(hoGiaDinhRepository.findByCccdNhanKhau(keyword));
        return new ArrayList<>(results);
    }

    public List<HoGiaDinh> searchByCanHoOrChuHo(String keyword) {
        return hoGiaDinhRepository
                .findBySoCanHoContainingIgnoreCaseOrChuHoContainingIgnoreCase(keyword, keyword);
    }

    public boolean existsBySoCanHo(String soCanHo) {
        return hoGiaDinhRepository.existsBySoCanHo(soCanHo);
    }

    public boolean existsBySoCanHoForOther(String soCanHo, Integer id) {
        return hoGiaDinhRepository.findBySoCanHoAndIdNot(soCanHo, id).isPresent();
    }

    @Transactional
    public HoGiaDinh save(HoGiaDinh hoGiaDinh) {
        boolean isNew = (hoGiaDinh.getId() == null);
        if (isNew) hoGiaDinh.setNgayTao(LocalDateTime.now());
        HoGiaDinh saved = hoGiaDinhRepository.save(hoGiaDinh);
        String action = isNew ? "Tạo" : "Sửa";
        String user = currentUser();
        log.info("[AUDIT] {} hộ gia đình: id={}, canHo={}, chuHo={}, user={}",
                action, saved.getId(), saved.getSoCanHo(), saved.getChuHo(), user);
        auditLogService.log(action, "Hộ gia đình",
                "id=" + saved.getId() + ", canHo=" + saved.getSoCanHo() + ", chuHo=" + saved.getChuHo(), user);
        if (isNew) {
            khoanThuService.autoApplyForNewHo(saved);
        }
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        HoGiaDinh ho = findById(id);

        long soNhanKhau = nhanKhauRepository.findByHoGiaDinhId(id).size();
        if (soNhanKhau > 0) {
            throw new IllegalStateException(
                    "Không thể xóa căn hộ \"" + ho.getSoCanHo()
                    + "\" vì còn " + soNhanKhau + " nhân khẩu đăng ký.");
        }

        List<TrangThaiThanhToan> noStatuses = List.of(TrangThaiThanhToan.CON_NO);
        boolean conNo = thanhToanRepository
                .existsByHoGiaDinhIdAndTrangThaiIn(id, noStatuses);
        if (conNo) {
            throw new IllegalStateException(
                    "Không thể xóa căn hộ \"" + ho.getSoCanHo()
                    + "\" vì còn khoản phí chưa thanh toán.");
        }

        hoGiaDinhRepository.deleteById(id);
        String user = currentUser();
        log.info("[AUDIT] Xóa hộ gia đình: id={}, canHo={}, user={}", id, ho.getSoCanHo(), user);
        auditLogService.log("Xóa", "Hộ gia đình", "id=" + id + ", canHo=" + ho.getSoCanHo(), user);
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
