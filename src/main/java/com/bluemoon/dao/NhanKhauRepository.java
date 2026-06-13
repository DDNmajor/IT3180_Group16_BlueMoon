package com.bluemoon.dao;

import com.bluemoon.model.NhanKhau;
import com.bluemoon.model.TinhTrangCuTru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NhanKhauRepository extends JpaRepository<NhanKhau, Integer> {
    List<NhanKhau> findByHoGiaDinhId(Integer idHoGiaDinh);
    Optional<NhanKhau> findByCccd(String cccd);
    List<NhanKhau> findByHoTenContainingIgnoreCase(String hoTen);
    boolean existsByCccd(String cccd);

    long countByNgayTaoBetween(LocalDateTime from, LocalDateTime to);

    // đếm nhân khẩu đang ở (loại trừ CHUYEN_DI) — dùng tính phí PER_PERSON
    long countByHoGiaDinhIdAndTinhTrangNot(Integer idHoGiaDinh, TinhTrangCuTru tinhTrang);
}
