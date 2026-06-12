package com.bluemoon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeKhoanThuDto {
    private Integer idKhoanThu;
    private String tenKhoanThu;
    private String loai;
    private BigDecimal tongTienYeuCau;
    private BigDecimal tongTienDaThu;
    private BigDecimal conThieu;
    private long soHoDaDong;
    private long soHoConNo;
    private long soHoDongDu;
    private long soHoChuaNop;
    private BigDecimal tiLeThuDuoc;
    private String maKhoanThu;
    private LocalDate kyThu;
}