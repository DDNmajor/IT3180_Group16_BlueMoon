package com.bluemoon.dao;

import com.bluemoon.model.MauKhoanThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MauKhoanThuRepository extends JpaRepository<MauKhoanThu, Integer> {
    List<MauKhoanThu> findByActiveTrue();
    boolean existsByMaMauPrefix(String maMauPrefix);
    boolean existsByMaMauPrefixAndIdNot(String maMauPrefix, Integer id);
    Optional<MauKhoanThu> findByMaMauPrefix(String maMauPrefix);
}
