package com.bluemoon.service;

import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.LoaiKhoanThuRepository;
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
    private final LoaiKhoanThuRepository loaiKhoanThuRepository;

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
        Integer idLoai = khoanThu.getLoaiKhoanThu() == null ? null : khoanThu.getLoaiKhoanThu().getId();

        if (idLoai == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại khoản thu");
        }

        khoanThu.setLoaiKhoanThu(loaiKhoanThuRepository.findById(idLoai)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại khoản thu id=" + idLoai)));

        if (khoanThu.getId() == null) {
            khoanThu.setNgayTao(LocalDateTime.now());
        } else {
            KhoanThu current = findById(khoanThu.getId());
            khoanThu.setNgayTao(current.getNgayTao());
        }

        return khoanThuRepository.save(khoanThu);
    }

    @Transactional
    public void delete(Integer id) {
        KhoanThu khoanThu = findById(id);
        khoanThuRepository.delete(khoanThu);
    }
}