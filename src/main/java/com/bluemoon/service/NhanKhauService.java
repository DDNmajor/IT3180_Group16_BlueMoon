package com.bluemoon.service;

import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.model.NhanKhau;
import com.bluemoon.model.TinhTrangCuTru;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhanKhauService {

    private final NhanKhauRepository nhanKhauRepository;
    private final AuditLogService    auditLogService;

    public List<NhanKhau> findAll() { return nhanKhauRepository.findAll(); }

    public NhanKhau findById(Integer id) {
        return nhanKhauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân khẩu id=" + id));
    }

    public List<NhanKhau> findByHoGiaDinh(Integer idHoGiaDinh) {
        return nhanKhauRepository.findByHoGiaDinhId(idHoGiaDinh);
    }

    public List<NhanKhau> search(String hoTen) {
        return nhanKhauRepository.findByHoTenContainingIgnoreCase(hoTen);
    }

    public boolean existsByCccd(String cccd) {
        return nhanKhauRepository.existsByCccd(cccd);
    }

    public Optional<NhanKhau> findByCccd(String cccd) {
        return nhanKhauRepository.findByCccd(cccd);
    }

    @Transactional
    public NhanKhau save(NhanKhau nhanKhau) {
        boolean isNew = (nhanKhau.getId() == null);
        if (isNew) {
            nhanKhau.setNgayTao(LocalDateTime.now());
            if (nhanKhau.getTinhTrang() == null) nhanKhau.setTinhTrang(TinhTrangCuTru.THUONG_TRU);
        }
        NhanKhau saved = nhanKhauRepository.save(nhanKhau);
        String action = isNew ? "Tạo" : "Sửa";
        String user = currentUser();
        String canHo = saved.getHoGiaDinh() != null ? saved.getHoGiaDinh().getSoCanHo() : "?";
        log.info("[AUDIT] {} nhân khẩu: id={}, hoTen={}, cccd={}, ho={}, user={}",
                action, saved.getId(), saved.getHoTen(), saved.getCccd(), canHo, user);
        auditLogService.log(action, "Nhân khẩu",
                "id=" + saved.getId() + ", hoTen=" + saved.getHoTen()
                + ", cccd=" + saved.getCccd() + ", canHo=" + canHo, user);
        return saved;
    }

    // không ghi audit — chỉ dùng nội bộ cho BienDongService
    @Transactional
    public NhanKhau saveRaw(NhanKhau nhanKhau) {
        return nhanKhauRepository.save(nhanKhau);
    }

    @Transactional
    public void delete(Integer id) {
        NhanKhau nk = findById(id);
        nhanKhauRepository.deleteById(id);
        String user = currentUser();
        log.info("[AUDIT] Xóa nhân khẩu: id={}, hoTen={}, user={}", id, nk.getHoTen(), user);
        auditLogService.log("Xóa", "Nhân khẩu", "id=" + id + ", hoTen=" + nk.getHoTen(), user);
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
