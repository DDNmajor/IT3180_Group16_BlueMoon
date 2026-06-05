package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự")
    @Column(name = "ten_dang_nhap", nullable = false, unique = true)
    private String tenDangNhap;

    // Không @NotBlank, không @Size(min) — khi sửa, bỏ trống = giữ mật khẩu cũ; độ dài check trong controller
    @Size(max = 100, message = "Mật khẩu không được vượt quá 100 ký tự")
    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    @NotNull(message = "Vui lòng chọn vai trò")
    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro")
    private VaiTro vaiTro;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "doi_mat_khau_lan_dau")
    private Boolean doiMatKhauLanDau = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
