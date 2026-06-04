package com.bluemoon.dao;

import com.bluemoon.model.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {
    @Query("SELECT COALESCE(SUM(t.soTienDaNop), 0) FROM ThanhToan t WHERE MONTH(t.ngayNop) = MONTH(CURRENT_DATE) AND YEAR(t.ngayNop) = YEAR(CURRENT_DATE)")
    BigDecimal sumSoTienDaNopThangNay();
    
    List<ThanhToan> findByHoGiaDinhId(Integer idHoGiaDinh);
    List<ThanhToan> findByKhoanThuId(Integer idKhoanThu);
    boolean existsByHoGiaDinhIdAndKhoanThuId(Integer idHoGiaDinh, Integer idKhoanThu);
}
