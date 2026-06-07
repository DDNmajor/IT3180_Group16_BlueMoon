package com.bluemoon.service;

import com.bluemoon.dao.NguoiDungRepository;
import com.bluemoon.model.NguoiDung;
import com.bluemoon.model.VaiTro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NguoiDungService {

    private final NguoiDungRepository   nguoiDungRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService       auditLogService;

    public List<NguoiDung> findAll() {
        return nguoiDungRepository.findAll();
    }

    public NguoiDung findById(Integer id) {
        return nguoiDungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng id=" + id));
    }

    public NguoiDung findByTenDangNhap(String tenDangNhap) {
        return nguoiDungRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + tenDangNhap));
    }

    public List<NguoiDung> findByVaiTro(VaiTro vaiTro) {
        return nguoiDungRepository.findByVaiTro(vaiTro);
    }

    public boolean existsByTenDangNhap(String tenDangNhap) {
        return nguoiDungRepository.existsByTenDangNhap(tenDangNhap);
    }

    @Transactional
    public NguoiDung save(NguoiDung nguoiDung) {
        boolean isNew = (nguoiDung.getId() == null);

        if (isNew) {
            nguoiDung.setNgayTao(LocalDateTime.now());
            nguoiDung.setDoiMatKhauLanDau(true);
        }

        // skip nếu đã là BCrypt hash ($2a$)
        if (nguoiDung.getMatKhau() != null
                && !nguoiDung.getMatKhau().isBlank()
                && !nguoiDung.getMatKhau().startsWith("$2a$")) {
            nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        }

        NguoiDung saved = nguoiDungRepository.save(nguoiDung);
        String action = isNew ? "Tạo" : "Sửa";
        String user = currentUser();
        log.info("[AUDIT] {} người dùng: id={}, username={}, vaiTro={}, user={}",
                action, saved.getId(), saved.getTenDangNhap(), saved.getVaiTro(), user);
        auditLogService.log(action, "Người dùng",
                "id=" + saved.getId() + ", username=" + saved.getTenDangNhap()
                + ", vaiTro=" + saved.getVaiTro(), user);
        return saved;
    }

    @Transactional
    public void toggleActive(Integer id, String currentUsername) {
        NguoiDung nd = findById(id);

        if (nd.getTenDangNhap().equals(currentUsername)) {
            throw new IllegalArgumentException("Không thể vô hiệu hoá tài khoản đang đăng nhập");
        }

        boolean seVoHieuHoa = Boolean.TRUE.equals(nd.getActive());
        if (seVoHieuHoa && nd.getVaiTro() == VaiTro.admin) {
            long soAdminConLai = nguoiDungRepository.findByVaiTro(VaiTro.admin)
                    .stream().filter(a -> Boolean.TRUE.equals(a.getActive()) && !a.getId().equals(id)).count();
            if (soAdminConLai == 0) {
                throw new IllegalArgumentException("Không thể vô hiệu hoá admin cuối cùng còn hoạt động");
            }
        }

        nd.setActive(!Boolean.TRUE.equals(nd.getActive()));
        nguoiDungRepository.save(nd);

        String action = nd.getActive() ? "Kích hoạt" : "Vô hiệu hoá";
        String user = currentUser();
        log.info("[AUDIT] {} người dùng: id={}, username={}, user={}", action, nd.getId(), nd.getTenDangNhap(), user);
        auditLogService.log(action, "Người dùng",
                "id=" + nd.getId() + ", username=" + nd.getTenDangNhap(), user);
    }

    @Transactional
    public void delete(Integer id) {
        NguoiDung nd = findById(id);
        nguoiDungRepository.deleteById(id);
        String user = currentUser();
        log.info("[AUDIT] Xóa người dùng: id={}, username={}, user={}", id, nd.getTenDangNhap(), user);
        auditLogService.log("Xóa", "Người dùng", "id=" + id + ", username=" + nd.getTenDangNhap(), user);
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
