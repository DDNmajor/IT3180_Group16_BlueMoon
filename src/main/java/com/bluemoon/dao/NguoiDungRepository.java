package com.bluemoon.dao;

import com.bluemoon.model.NguoiDung;
import com.bluemoon.model.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);
    List<NguoiDung> findByVaiTro(VaiTro vaiTro);
    boolean existsByTenDangNhap(String tenDangNhap);
}
