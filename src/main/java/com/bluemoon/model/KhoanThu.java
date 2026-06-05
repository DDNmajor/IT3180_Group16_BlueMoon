package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "khoan_thu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhoanThu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Mã khoản thu không được để trống")
    @Size(max = 50, message = "Mã khoản thu không được dài quá 50 ký tự")
    @Column(name = "ma_khoan_thu", unique = true)
    private String maKhoanThu;

    @NotBlank(message = "Tên khoản thu không được để trống")
    @Size(max = 200, message = "Tên khoản thu không được dài quá 200 ký tự")
    @Column(name = "ten_khoan_thu", nullable = false, length = 200)
    private String tenKhoanThu;

    @NotNull(message = "Vui lòng chọn loại khoản thu")
    @ManyToOne
    @JoinColumn(name = "id_loai", nullable = false)
    private LoaiKhoanThu loaiKhoanThu;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 0, message = "Số tiền không được nhỏ hơn 0")
    @Max(value = 100000000000L, message = "Số tiền quá lớn (tối đa 100 tỷ)")
    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTien;

    @DecimalMin(value = "0", message = "Đơn giá/m² không được nhỏ hơn 0")
    @Column(name = "don_gia_per_m2", precision = 15, scale = 2)
    private BigDecimal donGiaPerM2;

    @Size(max = 50, message = "Đơn vị không được vượt quá 50 ký tự")
    @Column(name = "don_vi", length = 50)
    private String donVi;

    @NotNull(message = "Kỳ thu không được để trống")
    @Column(name = "ky_thu", nullable = false)
    private LocalDate kyThu;

    @Column(name = "han_nop")
    private LocalDate hanNop;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau")
    private MauKhoanThu mauKhoanThu;

    @OneToMany(mappedBy = "khoanThu")
    private List<ThanhToan> thanhToans;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
