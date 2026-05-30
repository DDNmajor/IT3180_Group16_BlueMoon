package com.bluemoon.service;

import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.model.NhanKhau;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanKhauService {

    private final NhanKhauRepository nhanKhauRepository;

    public List<NhanKhau> findAll() {
        return nhanKhauRepository.findAll();
    }

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

    @Transactional
    public NhanKhau save(NhanKhau nhanKhau) {
        if (nhanKhau.getId() == null) {
            nhanKhau.setNgayTao(LocalDateTime.now());
        }
        return nhanKhauRepository.save(nhanKhau);
    }

    @Transactional
    public void delete(Integer id) {
        nhanKhauRepository.deleteById(id);
    }
}
