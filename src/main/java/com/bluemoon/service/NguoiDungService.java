package com.bluemoon.service;

import com.bluemoon.dao.NguoiDungRepository;
import com.bluemoon.model.NguoiDung;
import com.bluemoon.model.VaiTro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NguoiDungService {

    private final NguoiDungRepository nguoiDungRepository;

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
        if (nguoiDung.getId() == null) {
            nguoiDung.setNgayTao(LocalDateTime.now());
        }
        return nguoiDungRepository.save(nguoiDung);
    }

    @Transactional
    public void delete(Integer id) {
        nguoiDungRepository.deleteById(id);
    }
}
