package com.bluemoon.dao;

import com.bluemoon.model.HoaDonThuHo;
import com.bluemoon.model.LoaiDichVuThuHo;
import com.bluemoon.model.TrangThaiHoaDonThuHo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonThuHoRepository extends JpaRepository<HoaDonThuHo, Integer> {

    List<HoaDonThuHo> findAllByOrderByNgayTaoDesc();

    List<HoaDonThuHo> findByHoGiaDinhId(Integer idHo);

    List<HoaDonThuHo> findByLoaiDichVu(LoaiDichVuThuHo loaiDichVu);

    List<HoaDonThuHo> findByTrangThai(TrangThaiHoaDonThuHo trangThai);

    List<HoaDonThuHo> findByKyThanhToanBetween(LocalDate from, LocalDate to);

    boolean existsByHoGiaDinhIdAndLoaiDichVuAndKyThanhToan(
            Integer idHo, LoaiDichVuThuHo loaiDichVu, LocalDate kyThanhToan);

    Optional<HoaDonThuHo> findByMaHoaDon(String maHoaDon);
}
