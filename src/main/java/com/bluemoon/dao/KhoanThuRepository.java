package com.bluemoon.dao;

import com.bluemoon.model.KhoanThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KhoanThuRepository extends JpaRepository<KhoanThu, Integer> {
    List<KhoanThu> findByLoaiKhoanThuId(Integer idLoai);
    List<KhoanThu> findByKyThuBetween(LocalDate from, LocalDate to);
    List<KhoanThu> findByHanNopBefore(LocalDate date);
}
