package com.bluemoon.model;

public enum LoaiApDung {
    BAT_BUOC_DINH_KY("Bắt buộc - Định kỳ"),
    BAT_BUOC_DOT_XUAT("Bắt buộc - Đột xuất"),
    TU_NGUYEN("Tự nguyện");

    private final String tenHienThi;

    LoaiApDung(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public boolean isBatBuoc() {
        return this != TU_NGUYEN;
    }
}
