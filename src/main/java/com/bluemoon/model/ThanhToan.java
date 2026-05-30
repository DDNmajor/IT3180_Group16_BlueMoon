package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(name = "ngay_nop")
    private LocalDateTime ngayNop;

    @ManyToOne
    @JoinColumn(name = "nguoi_thu")
    private NguoiDung nguoiThu;

    @Column(name = "ghi_chu")
    private String ghiChu;
}