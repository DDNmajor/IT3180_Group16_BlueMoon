package com.bluemoon.dao;

import com.bluemoon.model.LoaiXe;
import com.bluemoon.model.PhuongTien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhuongTienRepository extends JpaRepository<PhuongTien, Integer> {
    List<PhuongTien> findByHoGiaDinhId(Integer idHo);
    long countByHoGiaDinhIdAndLoaiXe(Integer idHo, LoaiXe loaiXe);
    boolean existsByBienSo(String bienSo);
    boolean existsByBienSoAndIdNot(String bienSo, Integer id);
}
