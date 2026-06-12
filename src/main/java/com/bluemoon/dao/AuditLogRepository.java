package com.bluemoon.dao;

import com.bluemoon.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer>,
        JpaSpecificationExecutor<AuditLog> {

    @Query("SELECT DISTINCT a.nguoiDung FROM AuditLog a WHERE a.nguoiDung IS NOT NULL ORDER BY a.nguoiDung")
    List<String> findDistinctNguoiDung();

    List<AuditLog> findTop5ByOrderByThoiGianDesc();
}
