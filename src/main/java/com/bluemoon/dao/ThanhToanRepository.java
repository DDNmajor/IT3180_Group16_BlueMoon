package com.bluemoon.dao;

import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.TrangThaiThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {
    @Query("SELECT COALESCE(SUM(t.soTienDaNop), 0) FROM ThanhToan t WHERE MONTH(t.ngayNop) = MONTH(CURRENT_DATE) AND YEAR(t.ngayNop) = YEAR(CURRENT_DATE)")
    BigDecimal sumSoTienDaNopThangNay();

    List<ThanhToan> findByHoGiaDinhIdOrderByNgayNopDesc(Integer idHoGiaDinh);
    List<ThanhToan> findByKhoanThuIdOrderByNgayNopDesc(Integer idKhoanThu);
    boolean existsByKhoanThuId(Integer idKhoanThu);
    boolean existsByHoGiaDinhIdAndKhoanThuIdAndTrangThaiIn(Integer idHoGiaDinh, Integer idKhoanThu, Collection<TrangThaiThanhToan> trangThais);
}
