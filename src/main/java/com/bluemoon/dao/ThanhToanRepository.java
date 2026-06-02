package com.bluemoon.dao;

import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.TrangThaiThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {
    List<ThanhToan> findByHoGiaDinhIdOrderByNgayNopDesc(Integer idHoGiaDinh);
    List<ThanhToan> findByKhoanThuIdOrderByNgayNopDesc(Integer idKhoanThu);
    boolean existsByHoGiaDinhIdAndKhoanThuIdAndTrangThaiIn(Integer idHoGiaDinh, Integer idKhoanThu, java.util.Collection<TrangThaiThanhToan> trangThais);
}
