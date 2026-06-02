package com.bluemoon.model;

public enum TrangThaiThanhToan {
    DA_DONG("Đã đóng"),
    CON_NO("Còn nợ"),
    DONG_DU("Đóng dư");

    private final String tenHienThi;

    TrangThaiThanhToan(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}
