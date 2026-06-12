package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "phuong_tien")
@Getter
@Setter
@NoArgsConstructor
public class PhuongTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Vui lòng chọn loại xe")
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_xe", nullable = false)
    private LoaiXe loaiXe;

    @NotBlank(message = "Biển số không được để trống")
    @Size(max = 20, message = "Biển số không được dài quá 20 ký tự")
    @Column(name = "bien_so", nullable = false, unique = true, length = 20)
    private String bienSo;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @ManyToOne
    @JoinColumn(name = "id_ho", nullable = false)
    private HoGiaDinh hoGiaDinh;
}
