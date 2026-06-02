package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_ho_gia_dinh")
    private HoGiaDinh hoGiaDinh;

    @ManyToOne
    @JoinColumn(name = "id_khoan_thu")
    private KhoanThu khoanThu;

    @Column(name = "so_tien_da_nop")
    private BigDecimal soTienDaNop;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "ngay_nop")
    private LocalDate ngayNop;

    @ManyToOne
    @JoinColumn(name = "nguoi_thu")
    private NguoiDung nguoiThu;

    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc")
    private PhuongThucThanhToan phuongThuc;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiThanhToan trangThai;

    @Column(name = "ghi_chu")
    private String ghiChu;
}
