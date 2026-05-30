package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nhan_khau")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanKhau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    private String cccd;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "quan_he_chu_ho")
    private String quanHeChuHo;

    @ManyToOne
    @JoinColumn(name = "id_ho_gia_dinh")
    private HoGiaDinh hoGiaDinh;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}