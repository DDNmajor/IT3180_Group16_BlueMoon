package com.bluemoon.service;

import com.bluemoon.dao.AuditLogRepository;
import com.bluemoon.model.AuditLog;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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
        String loai = blankToNull(loaiDoiTuong);
        String nd   = blankToNull(nguoiDung);

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (loai != null)
                predicates.add(cb.equal(root.get("loaiDoiTuong"), loai));
            if (nd != null)
                predicates.add(cb.equal(root.get("nguoiDung"), nd));
            if (tuNgay != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("thoiGian"), tuNgay.atStartOfDay()));
            if (denNgay != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("thoiGian"), denNgay.atTime(23, 59, 59)));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "thoiGian"));
    }

    public List<String> findDistinctNguoiDung() {
        return auditLogRepository.findDistinctNguoiDung();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
