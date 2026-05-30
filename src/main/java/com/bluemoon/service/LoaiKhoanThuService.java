package com.bluemoon.service;

import com.bluemoon.dao.LoaiKhoanThuRepository;
import com.bluemoon.model.LoaiKhoanThu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoaiKhoanThuService {

    private final LoaiKhoanThuRepository loaiKhoanThuRepository;

    public List<LoaiKhoanThu> findAll() {
        return loaiKhoanThuRepository.findAll();
    }

    public LoaiKhoanThu findById(Integer id) {
        return loaiKhoanThuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại khoản thu id=" + id));
    }

    public List<LoaiKhoanThu> findByBatBuoc(boolean batBuoc) {
        return loaiKhoanThuRepository.findByBatBuoc(batBuoc);
    }

    public boolean existsByTenLoai(String tenLoai) {
        return loaiKhoanThuRepository.existsByTenLoai(tenLoai);
    }

    @Transactional
    public LoaiKhoanThu save(LoaiKhoanThu loaiKhoanThu) {
        return loaiKhoanThuRepository.save(loaiKhoanThu);
    }

    @Transactional
    public void delete(Integer id) {
        loaiKhoanThuRepository.deleteById(id);
    }
}
