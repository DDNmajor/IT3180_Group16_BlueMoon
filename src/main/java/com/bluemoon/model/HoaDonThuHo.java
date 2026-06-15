package com.bluemoon.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.text.NumberFormat;
import java.util.Locale;

@Entity
@Table(
    name = "hoa_don_thu_ho",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_hoadon_ho_dv_ky",
        columnNames = {"id_ho_gia_dinh", "loai_dich_vu", "ky_thanh_toan"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonThuHo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_hoa_don", unique = true, nullable = false, length = 50)
    private String maHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ho_gia_dinh", nullable = false)
    private HoGiaDinh hoGiaDinh;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_dich_vu", nullable = false, length = 20)
    private LoaiDichVuThuHo loaiDichVu;

    @Column(name = "ky_thanh_toan", nullable = false)
    private LocalDate kyThanhToan;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 2)
    private BigDecimal soTien;

    @Column(name = "han_thanh_toan")
    private LocalDate hanThanhToan;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiHoaDonThuHo trangThai = TrangThaiHoaDonThuHo.CHO_THANH_TOAN;

    @Column(name = "email_da_gui")
    private boolean emailDaGui = false;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_xac_nhan")
    private LocalDateTime ngayXacNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_xac_nhan")
    private NguoiDung nguoiXacNhan;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (trangThai == null) trangThai = TrangThaiHoaDonThuHo.CHO_THANH_TOAN;
    }

    @Transient
    public String getKyHienThi() {
        if (kyThanhToan == null) return "";
        return String.format("%02d/%d", kyThanhToan.getMonthValue(), kyThanhToan.getYear());
    }

    @Transient
    public String getHanHienThi() {
        if (hanThanhToan == null) return null;
        return String.format("%02d/%02d/%d",
                hanThanhToan.getDayOfMonth(), hanThanhToan.getMonthValue(), hanThanhToan.getYear());
    }

    @Transient
    public String getSoTienHienThi() {
        if (soTien == null) return "";
        return NumberFormat.getIntegerInstance(Locale.US).format(soTien.longValue()) + " đ";
    }

    @Transient
    public String getTenNguoiXacNhan() {
        return nguoiXacNhan != null ? nguoiXacNhan.getTenDangNhap() : "BQL";
    }
}
