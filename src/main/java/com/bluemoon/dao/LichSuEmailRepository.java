package com.bluemoon.dao;

import com.bluemoon.model.LichSuEmail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuEmailRepository extends JpaRepository<LichSuEmail, Integer> {
    List<LichSuEmail> findAllByOrderByNgayGuiDesc(Pageable pageable);
}
