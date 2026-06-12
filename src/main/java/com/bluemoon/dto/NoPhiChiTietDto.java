package com.bluemoon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoPhiChiTietDto {
    private Integer idKhoanThu;
    private String tenKhoanThu;
    private BigDecimal soTienYeuCau;
    private BigDecimal soTienDaNop;
    private BigDecimal conThieu;
}