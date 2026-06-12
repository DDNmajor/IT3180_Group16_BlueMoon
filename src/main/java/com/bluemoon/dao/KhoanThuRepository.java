package com.bluemoon.dao;

import com.bluemoon.model.KhoanThu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhoanThuRepository extends JpaRepository<KhoanThu, Integer> {
    Optional<KhoanThu> findByMaKhoanThu(String maKhoanThu);
    List<KhoanThu> findByLoaiKhoanThuId(Integer idLoai);
    List<KhoanThu> findByKyThuBetween(LocalDate from, LocalDate to);
    List<KhoanThu> findByLoaiKhoanThuIdAndKyThuBetween(Integer idLoai, LocalDate from, LocalDate to);
    List<KhoanThu> findByHanNopBefore(LocalDate date);
    boolean existsByMauKhoanThuIdAndKyThu(Integer mauId, LocalDate kyThu);
    List<KhoanThu> findByMauKhoanThuIdOrderByKyThuDesc(Integer mauId);

    List<KhoanThu> findByHanNopBetweenOrderByHanNop(LocalDate from, LocalDate to);

    // Trash bin — native queries bypass @SQLRestriction
    @Query(value = "SELECT * FROM khoan_thu WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC", nativeQuery = true)
    List<KhoanThu> findAllDeleted();

    @Query(value = "SELECT * FROM khoan_thu WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<KhoanThu> findDeletedById(@Param("id") Integer id);

    @Modifying
    @Query(value = "UPDATE khoan_thu SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    void restoreById(@Param("id") Integer id);

    @Modifying
    @Query(value = "DELETE FROM khoan_thu WHERE id = :id", nativeQuery = true)
    void permanentDeleteById(@Param("id") Integer id);
}
