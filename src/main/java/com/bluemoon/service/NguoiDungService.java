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

        // Encode password nếu chưa được mã hoá (BCrypt hash bắt đầu bằng $2a$)
        if (nguoiDung.getMatKhau() != null
                && !nguoiDung.getMatKhau().isBlank()
                && !nguoiDung.getMatKhau().startsWith("$2a$")) {
            nguoiDung.setMatKhau(passwordEncoder.encode(nguoiDung.getMatKhau()));
        }

        NguoiDung saved = nguoiDungRepository.save(nguoiDung);
        log.info("[AUDIT] {} người dùng: id={}, username={}, vaiTro={}, user={}",
                isNew ? "Tạo" : "Sửa",
                saved.getId(), saved.getTenDangNhap(), saved.getVaiTro(), currentUser());
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        NguoiDung nd = findById(id);
        nguoiDungRepository.deleteById(id);
        log.info("[AUDIT] Xóa người dùng: id={}, username={}, user={}", id, nd.getTenDangNhap(), currentUser());
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
