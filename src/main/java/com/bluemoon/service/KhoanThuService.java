package com.bluemoon.service;

import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.model.KhoanThu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhoanThuService {

    private final KhoanThuRepository khoanThuRepository;

    public List<KhoanThu> findAll() {
        return khoanThuRepository.findAll();
    }

    public KhoanThu findById(Integer id) {
        return khoanThuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoản thu id=" + id));
    }

    public List<KhoanThu> findByLoai(Integer idLoai) {
        return khoanThuRepository.findByLoaiKhoanThuId(idLoai);
    }

    public List<KhoanThu> findByKyThu(LocalDate from, LocalDate to) {
        return khoanThuRepository.findByKyThuBetween(from, to);
    }

    public List<KhoanThu> findQuaHan() {
        return khoanThuRepository.findByHanNopBefore(LocalDate.now());
    }

    @Transactional
    public KhoanThu save(KhoanThu khoanThu) {
        if (khoanThu.getId() == null) {
            khoanThu.setNgayTao(LocalDateTime.now());
        }
        return khoanThuRepository.save(khoanThu);
    }

    @Transactional
    public void delete(Integer id) {
        khoanThuRepository.deleteById(id);
    }
}
