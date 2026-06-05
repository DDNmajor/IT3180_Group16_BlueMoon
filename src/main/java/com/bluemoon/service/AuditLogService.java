package com.bluemoon.service;

import com.bluemoon.dao.AuditLogRepository;
import com.bluemoon.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String hanhDong, String loaiDoiTuong, String chiTiet, String nguoiDung) {
        AuditLog entry = new AuditLog();
        entry.setHanhDong(hanhDong);
        entry.setLoaiDoiTuong(loaiDoiTuong);
        entry.setChiTiet(chiTiet);
        entry.setNguoiDung(nguoiDung != null ? nguoiDung : "system");
        auditLogRepository.save(entry);
    }

    public List<AuditLog> findWithFilter(String loaiDoiTuong, String nguoiDung,
                                         LocalDate tuNgay, LocalDate denNgay) {
        return auditLogRepository.findWithFilter(
                blankToNull(loaiDoiTuong),
                blankToNull(nguoiDung),
                tuNgay  != null ? tuNgay.atStartOfDay()        : null,
                denNgay != null ? denNgay.atTime(23, 59, 59)   : null
        );
    }

    public List<String> findDistinctNguoiDung() {
        return auditLogRepository.findDistinctNguoiDung();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
