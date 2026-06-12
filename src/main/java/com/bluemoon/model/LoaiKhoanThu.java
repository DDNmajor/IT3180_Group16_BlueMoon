package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Tên loại không được để trống")
    @Size(max = 100, message = "Tên loại không được vượt quá 100 ký tự")
    @Column(name = "ten_loai", nullable = false)
    private String tenLoai;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    @Column(name = "mo_ta")
    private String moTa;

    @NotNull(message = "Vui lòng chọn loại áp dụng")
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_ap_dung", nullable = false)
    private LoaiApDung loaiApDung;

    @OneToMany(mappedBy = "loaiKhoanThu")
    private List<KhoanThu> khoanThus;
}
