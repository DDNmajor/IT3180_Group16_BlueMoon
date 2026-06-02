package com.bluemoon.dao;

import com.bluemoon.model.HoGiaDinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoGiaDinhRepository extends JpaRepository<HoGiaDinh, Integer> {
    Optional<HoGiaDinh> findBySoCanHo(String soCanHo);
    List<HoGiaDinh> findByChuHoContainingIgnoreCase(String chuHo);
    List<HoGiaDinh> findBySoCanHoContainingIgnoreCaseOrChuHoContainingIgnoreCase(String soCanHo, String chuHo);
    boolean existsBySoCanHo(String soCanHo);
}
