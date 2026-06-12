package com.bluemoon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoPhiHoDto {
    private Integer idHo;
    private String soCanHo;
    private String chuHo;
    private String email;
    private List<NoPhiChiTietDto> danhSachNo = new ArrayList<>();
    private BigDecimal tongNo = BigDecimal.ZERO;
    private LocalDate ngayNopGanNhat;
}