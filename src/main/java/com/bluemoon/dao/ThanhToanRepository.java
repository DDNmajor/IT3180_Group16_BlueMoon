package com.bluemoon.dao;

import com.bluemoon.model.LoaiTinhPhi;
import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.TrangThaiThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {
    @Query("SELECT COALESCE(SUM(t.soTienDaNop), 0) FROM ThanhToan t WHERE MONTH(t.ngayNop) = MONTH(CURRENT_DATE) AND YEAR(t.ngayNop) = YEAR(CURRENT_DATE)")
    BigDecimal sumSoTienDaNopThangNay();

    List<ThanhToan> findByHoGiaDinhIdOrderByNgayNopDesc(Integer idHoGiaDinh);
    List<ThanhToan> findByKhoanThuIdOrderByNgayNopDesc(Integer idKhoanThu);
    List<ThanhToan> findByHoGiaDinhIdAndKhoanThuIdOrderByNgayNopDesc(Integer idHoGiaDinh, Integer idKhoanThu);
    boolean existsByKhoanThuId(Integer idKhoanThu);
    boolean existsByKhoanThuIdAndSoTienDaNopGreaterThan(Integer idKhoanThu, java.math.BigDecimal soTien);
    void deleteByKhoanThuId(Integer idKhoanThu);
    boolean existsByHoGiaDinhIdAndKhoanThuIdAndTrangThaiIn(Integer idHoGiaDinh, Integer idKhoanThu, Collection<TrangThaiThanhToan> trangThais);
    boolean existsByHoGiaDinhIdAndTrangThaiIn(Integer idHoGiaDinh, Collection<TrangThaiThanhToan> trangThais);
    boolean existsByHoGiaDinhIdAndKhoanThuId(Integer idHoGiaDinh, Integer idKhoanThu);

    List<ThanhToan> findByHoGiaDinhIdAndKhoanThuLoaiTinhPhi(Integer idHoGiaDinh, LoaiTinhPhi loaiTinhPhi);

    List<ThanhToan> findByHoGiaDinhIdAndKhoanThuLoaiTinhPhiAndKhoanThuKyThuBetween(
            Integer idHoGiaDinh, LoaiTinhPhi loaiTinhPhi, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(t.soTienDaNop), 0) FROM ThanhToan t WHERE YEAR(t.ngayNop) = :nam AND MONTH(t.ngayNop) = :thang")
    BigDecimal sumByNamAndThang(@Param("nam") int nam, @Param("thang") int thang);

    // Dùng khi xóa vĩnh viễn từ thùng rác — bypass @SQLRestriction trên entity tham chiếu
    @Modifying
    @Query(value = "DELETE FROM thanh_toan WHERE id_khoan_thu = :khoanThuId", nativeQuery = true)
    void hardDeleteByKhoanThuId(@Param("khoanThuId") Integer khoanThuId);

    @Modifying
    @Query(value = "DELETE FROM thanh_toan WHERE id_ho_gia_dinh = :hoGiaDinhId", nativeQuery = true)
    void hardDeleteByHoGiaDinhId(@Param("hoGiaDinhId") Integer hoGiaDinhId);
}
