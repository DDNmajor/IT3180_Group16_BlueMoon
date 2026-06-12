package com.bluemoon.model;

public enum LoaiXe {
    XEMAY("Xe máy"),
    OTO("Ô tô");

    private final String tenHienThi;

    LoaiXe(String tenHienThi) { this.tenHienThi = tenHienThi; }

    public String getTenHienThi() { return tenHienThi; }
}
