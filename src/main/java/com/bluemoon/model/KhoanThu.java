package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Column(name = "ten_khoan_thu")
    @NotBlank(message = "Tên khoản thu không được để trống")
    @Size(max = 200, message = "Tên khoản thu không được dài quá 200 ký tự")
    private String tenKhoanThu;

    @ManyToOne
    @JoinColumn(name = "id_loai")
    @NotNull(message = "Vui lòng chọn loại khoản thu")
    private LoaiKhoanThu loaiKhoanThu;

    @Column(name = "so_tien")
    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 0, message = "Số tiền không được nhỏ hơn 0")
    @Max(value = 100000000000L, message = "Số tiền quá lớn (tối đa 100 tỷ)")
    private BigDecimal soTien;

    @Column(name = "don_vi")
    private String donVi;

    @Column(name = "ky_thu")
    @NotNull(message = "Kỳ thu không được để trống")
    private LocalDate kyThu;

    @Column(name = "han_nop")
    private LocalDate hanNop;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "khoanThu")
    private List<ThanhToan> thanhToans;
}