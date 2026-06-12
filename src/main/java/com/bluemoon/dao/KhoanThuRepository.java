package com.bluemoon.dao;

import com.bluemoon.model.KhoanThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhoanThuRepository extends JpaRepository<KhoanThu, Integer> {
    Optional<KhoanThu> findByMaKhoanThu(String maKhoanThu);
    List<KhoanThu> findByLoaiKhoanThuId(Integer idLoai);
    List<KhoanThu> findByKyThuBetween(LocalDate from, LocalDate to);
    List<KhoanThu> findByLoaiKhoanThuIdAndKyThuBetween(Integer idLoai, LocalDate from, LocalDate to);
    List<KhoanThu> findByHanNopBefore(LocalDate date);
    boolean existsByMauKhoanThuIdAndKyThu(Integer mauId, LocalDate kyThu);
    List<KhoanThu> findByMauKhoanThuIdOrderByKyThuDesc(Integer mauId);

    List<KhoanThu> findByHanNopBetweenOrderByHanNop(LocalDate from, LocalDate to);
}
