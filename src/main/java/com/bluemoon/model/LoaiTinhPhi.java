package com.bluemoon.model;

public enum LoaiTinhPhi {
    FIXED("Cố định"),
    PER_M2("Theo diện tích (m²)"),
    PER_XE("Theo phương tiện"),
    PER_PERSON("Theo số nhân khẩu"),
    THU_HO("Thu hộ (điện/nước/internet)");

    private final String tenHienThi;

    LoaiTinhPhi(String tenHienThi) { this.tenHienThi = tenHienThi; }

    public String getTenHienThi() { return tenHienThi; }
}
