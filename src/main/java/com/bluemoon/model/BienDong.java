package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bien_dong")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BienDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Vui lòng chọn nhân khẩu")
    @ManyToOne
    @JoinColumn(name = "id_nhan_khau", nullable = false)
    private NhanKhau nhanKhau;

    @NotNull(message = "Vui lòng chọn loại biến động")
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_bien_dong", nullable = false)
    private LoaiBienDong loaiBienDong;

    @NotNull(message = "Ngày biến động không được để trống")
    @Column(name = "ngay_bien_dong", nullable = false)
    private LocalDate ngayBienDong;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
