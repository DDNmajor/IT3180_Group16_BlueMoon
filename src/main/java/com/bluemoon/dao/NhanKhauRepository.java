package com.bluemoon.dao;

import com.bluemoon.model.NhanKhau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanKhauRepository extends JpaRepository<NhanKhau, Integer> {
    List<NhanKhau> findByHoGiaDinhId(Integer idHoGiaDinh);
    Optional<NhanKhau> findByCccd(String cccd);
    List<NhanKhau> findByHoTenContainingIgnoreCase(String hoTen);
    boolean existsByCccd(String cccd);
}
