package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ho_gia_dinh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoGiaDinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "so_can_ho", unique = true)
    private String soCanHo;

    @Column(name = "chu_ho")
    private String chuHo;

    @Column(name = "dien_tich")
    private BigDecimal dienTich;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "hoGiaDinh")
    private List<NhanKhau> nhanKhaus;
}