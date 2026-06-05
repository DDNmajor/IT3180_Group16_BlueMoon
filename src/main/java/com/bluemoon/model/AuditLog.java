package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "hanh_dong", length = 50, nullable = false)
    private String hanhDong;

    @Column(name = "loai_doi_tuong", length = 50)
    private String loaiDoiTuong;

    @Column(name = "chi_tiet", columnDefinition = "TEXT")
    private String chiTiet;

    @Column(name = "nguoi_dung", length = 100)
    private String nguoiDung;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @PrePersist
    public void prePersist() {
        if (thoiGian == null) thoiGian = LocalDateTime.now();
    }
}
