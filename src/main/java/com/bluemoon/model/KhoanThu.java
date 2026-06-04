package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Tên khoản thu không được để trống")
    @Size(max = 200, message = "Tên khoản thu không được vượt quá 200 ký tự")
    @Column(name = "ten_khoan_thu", nullable = false, length = 200)
    private String tenKhoanThu;

    @NotNull(message = "Vui lòng chọn loại khoản thu")
    @ManyToOne
    @JoinColumn(name = "id_loai", nullable = false)
    private LoaiKhoanThu loaiKhoanThu;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTien;

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

    @OneToMany(mappedBy = "khoanThu")
    private List<ThanhToan> thanhToans;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}