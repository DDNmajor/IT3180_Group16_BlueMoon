package com.bluemoon.model;

public enum LoaiBienDong {
    TAM_TRU("Tạm trú"),
    TAM_VANG("Tạm vắng"),
    CHUYEN_DEN("Chuyển đến"),
    CHUYEN_DI("Chuyển đi");

    private final String tenHienThi;
    LoaiBienDong(String tenHienThi) { this.tenHienThi = tenHienThi; }
    public String getTenHienThi() { return tenHienThi; }
}
