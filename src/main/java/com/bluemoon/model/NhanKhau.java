package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "nhan_khau")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NhanKhau {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không quá 100 ký tự")
    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Pattern(regexp = "^$|^\\d{9}$|^\\d{12}$", message = "CCCD phải có 9 hoặc 12 chữ số")
    @Column(name = "cccd", unique = true, length = 12)
    private String cccd;

    @Pattern(regexp = "^$|^0\\d{9}$", message = "Số điện thoại phải có 10 số và bắt đầu bằng 0")
    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(name = "quan_he_chu_ho", length = 50)
    private String quanHeChuHo;

    @NotNull(message = "Vui lòng chọn hộ gia đình")
    @ManyToOne
    @JoinColumn(name = "id_ho_gia_dinh", nullable = false)
    private HoGiaDinh hoGiaDinh;

    @Enumerated(EnumType.STRING)
    @Column(name = "tinh_trang", length = 30)
    private TinhTrangCuTru tinhTrang = TinhTrangCuTru.THUONG_TRU;

    @OneToMany(mappedBy = "nhanKhau")
    private List<BienDong> bienDongs;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;
}
