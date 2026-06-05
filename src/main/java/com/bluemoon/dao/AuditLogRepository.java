package com.bluemoon.dao;

import com.bluemoon.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:loaiDoiTuong IS NULL OR a.loaiDoiTuong = :loaiDoiTuong) AND " +
           "(:nguoiDung    IS NULL OR a.nguoiDung    = :nguoiDung)    AND " +
           "(:tuNgay       IS NULL OR a.thoiGian    >= :tuNgay)       AND " +
           "(:denNgay      IS NULL OR a.thoiGian    <= :denNgay)      " +
           "ORDER BY a.thoiGian DESC")
    List<AuditLog> findWithFilter(@Param("loaiDoiTuong") String loaiDoiTuong,
                                  @Param("nguoiDung")    String nguoiDung,
                                  @Param("tuNgay")       LocalDateTime tuNgay,
                                  @Param("denNgay")      LocalDateTime denNgay);

    @Query("SELECT DISTINCT a.nguoiDung FROM AuditLog a WHERE a.nguoiDung IS NOT NULL ORDER BY a.nguoiDung")
    List<String> findDistinctNguoiDung();
}
