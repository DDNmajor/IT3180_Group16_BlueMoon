package com.bluemoon.model;

public enum TrangThaiHoaDonThuHo {
    CHO_THANH_TOAN("Chờ thanh toán", "warning"),
    DA_THANH_TOAN("Đã thanh toán", "success"),
    DA_HUY("Đã hủy", "secondary");

    private final String tenHienThi;
    private final String badgeColor;

    TrangThaiHoaDonThuHo(String tenHienThi, String badgeColor) {
        this.tenHienThi = tenHienThi;
        this.badgeColor = badgeColor;
    }

    public String getTenHienThi() { return tenHienThi; }
    public String getBadgeColor() { return badgeColor; }

    public String getBadgeCss() {
        return "bg-" + badgeColor;
    }
}
