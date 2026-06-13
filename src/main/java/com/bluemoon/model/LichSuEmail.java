package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_email")
@Getter @Setter @NoArgsConstructor
public class LichSuEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "to_email", length = 255)
    private String toEmail;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_email", length = 50)
    private LoaiEmail loaiEmail;

    // THANH_CONG | THAT_BAI
    @Column(name = "trang_thai", length = 20)
    private String trangThai;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui;

    @Column(name = "so_can_ho", length = 20)
    private String soCanHo;

    @Column(name = "nguoi_gui", length = 100)
    private String nguoiGui;

    @PrePersist
    public void prePersist() {
        if (ngayGui == null) ngayGui = LocalDateTime.now();
    }
}
