package com.bluemoon.dao;

import com.bluemoon.model.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {
    List<ThanhToan> findByHoGiaDinhId(Integer idHoGiaDinh);
    List<ThanhToan> findByKhoanThuId(Integer idKhoanThu);
    boolean existsByHoGiaDinhIdAndKhoanThuId(Integer idHoGiaDinh, Integer idKhoanThu);
}
