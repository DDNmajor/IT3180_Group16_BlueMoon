package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "loai_khoan_thu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoaiKhoanThu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_loai")
    private String tenLoai;

    @Column(name = "mo_ta")
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_ap_dung")
    private LoaiApDung loaiApDung;

    @OneToMany(mappedBy = "loaiKhoanThu")
    private List<KhoanThu> khoanThus;
}