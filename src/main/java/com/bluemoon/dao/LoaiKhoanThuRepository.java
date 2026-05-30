package com.bluemoon.dao;

import com.bluemoon.model.LoaiKhoanThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoaiKhoanThuRepository extends JpaRepository<LoaiKhoanThu, Integer> {
    List<LoaiKhoanThu> findByBatBuoc(boolean batBuoc);
    boolean existsByTenLoai(String tenLoai);
}
