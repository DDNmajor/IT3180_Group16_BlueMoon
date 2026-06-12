package com.bluemoon.dao;

import com.bluemoon.model.HoGiaDinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoGiaDinhRepository extends JpaRepository<HoGiaDinh, Integer> {
    Optional<HoGiaDinh> findBySoCanHo(String soCanHo);
    Optional<HoGiaDinh> findBySoCanHoIgnoreCase(String soCanHo);
    List<HoGiaDinh> findByChuHoContainingIgnoreCase(String chuHo);
    List<HoGiaDinh> findBySoCanHoContainingIgnoreCaseOrChuHoContainingIgnoreCase(String soCanHo, String chuHo);
    boolean existsBySoCanHo(String soCanHo);
    Optional<HoGiaDinh> findBySoCanHoAndIdNot(String soCanHo, Integer id);

    @Query("SELECT DISTINCT h FROM HoGiaDinh h JOIN h.nhanKhaus n WHERE n.cccd = :cccd")
    List<HoGiaDinh> findByCccdNhanKhau(@Param("cccd") String cccd);

    long countByNgayTaoBetween(LocalDateTime from, LocalDateTime to);
}
