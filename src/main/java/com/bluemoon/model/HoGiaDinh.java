package com.bluemoon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ho_gia_dinh")
@SQLRestriction("deleted_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class HoGiaDinh {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Số căn hộ không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9\\-\\.]+$", message = "Số căn hộ chỉ được chứa chữ cái, số, dấu '-' hoặc '.'")
    @Size(max = 20, message = "Số căn hộ không quá 20 ký tự")
    @Column(name = "so_can_ho", unique = true, nullable = false, length = 20)
    private String soCanHo;

    @NotBlank(message = "Tên chủ hộ không được để trống")
    @Size(max = 100, message = "Tên chủ hộ không quá 100 ký tự")
    @Column(name = "chu_ho", nullable = false, length = 100)
    private String chuHo;

    @Column(name = "dien_tich", precision = 7, scale = 2)
    private BigDecimal dienTich;

    @Pattern(regexp = "^$|^0\\d{9}$", message = "Số điện thoại phải có 10 số và bắt đầu bằng 0")
    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Size(max = 50, message = "Tầng/khu vực không quá 50 ký tự")
    @Column(name = "tang_khu_vuc", length = 50)
    private String tangKhuVuc;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không quá 255 ký tự")
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "hoGiaDinh")
    private List<NhanKhau> nhanKhaus;

    public String getTieuDe() {
        return soCanHo + " - " + chuHo;
    }
}
