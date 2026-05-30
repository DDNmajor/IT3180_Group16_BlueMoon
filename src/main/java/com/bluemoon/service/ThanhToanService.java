package com.bluemoon.service;

import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.ThanhToan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThanhToanService {

    private final ThanhToanRepository thanhToanRepository;

    public List<ThanhToan> findAll() {
        return thanhToanRepository.findAll();
    }

    public ThanhToan findById(Integer id) {
        return thanhToanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán id=" + id));
    }

    public List<ThanhToan> findByHoGiaDinh(Integer idHoGiaDinh) {
        return thanhToanRepository.findByHoGiaDinhId(idHoGiaDinh);
    }

    public List<ThanhToan> findByKhoanThu(Integer idKhoanThu) {
        return thanhToanRepository.findByKhoanThuId(idKhoanThu);
    }

    public boolean daThanhToan(Integer idHoGiaDinh, Integer idKhoanThu) {
        return thanhToanRepository.existsByHoGiaDinhIdAndKhoanThuId(idHoGiaDinh, idKhoanThu);
    }

    @Transactional
    public ThanhToan save(ThanhToan thanhToan) {
        if (thanhToan.getId() == null) {
            thanhToan.setNgayNop(LocalDateTime.now());
        }
        return thanhToanRepository.save(thanhToan);
    }

    @Transactional
    public void delete(Integer id) {
        thanhToanRepository.deleteById(id);
    }
}
