package com.bluemoon.dao;

import com.bluemoon.model.BienDong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BienDongRepository extends JpaRepository<BienDong, Integer> {
    List<BienDong> findByNhanKhauIdOrderByNgayBienDongDesc(Integer idNhanKhau);
    List<BienDong> findByNhanKhauHoGiaDinhIdOrderByNgayBienDongDesc(Integer idHoGiaDinh);
}
