package com.bluemoon.model;

public enum TinhTrangCuTru {
    THUONG_TRU("Thường trú"),
    TAM_TRU("Tạm trú"),
    TAM_VANG("Tạm vắng"),
    CHUYEN_DI("Đã chuyển đi");

    private final String tenHienThi;
    TinhTrangCuTru(String tenHienThi) { this.tenHienThi = tenHienThi; }
    public String getTenHienThi() { return tenHienThi; }
}
