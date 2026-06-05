package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mau_khoan_thu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MauKhoanThu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên mẫu không được để trống")
    @Size(max = 200, message = "Tên mẫu không được vượt quá 200 ký tự")
    @Column(name = "ten_mau", nullable = false)
    private String tenMau;

    @NotBlank(message = "Tiền tố mã không được để trống")
    @Size(max = 20, message = "Tiền tố mã không được vượt quá 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "Tiền tố chỉ được chứa chữ cái, số, dấu '-'")
    @Column(name = "ma_mau_prefix", nullable = false, length = 20, unique = true)
    private String maMauPrefix;

    @NotNull(message = "Vui lòng chọn loại khoản thu")
    @ManyToOne
    @JoinColumn(name = "id_loai", nullable = false)
    private LoaiKhoanThu loaiKhoanThu;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0", message = "Số tiền không được nhỏ hơn 0")
    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTien;

    @DecimalMin(value = "0", message = "Đơn giá/m² không được nhỏ hơn 0")
    @Column(name = "don_gia_per_m2", precision = 15, scale = 2)
    private BigDecimal donGiaPerM2;

    @Size(max = 50, message = "Đơn vị không được vượt quá 50 ký tự")
    @Column(name = "don_vi", length = 50)
    private String donVi;

    @Min(value = 0, message = "Số ngày không được nhỏ hơn 0")
    @Max(value = 365, message = "Số ngày không được vượt quá 365")
    @Column(name = "so_ngay_han_nop")
    private Integer soNgayHanNop;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}
