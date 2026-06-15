package com.bluemoon.model;

public enum LoaiDichVuThuHo {
    DIEN("Điện", "EVN Hà Nội", "bi-lightning-charge-fill", 200_000, 900_000),
    NUOC("Nước", "Hawaco", "bi-droplet-fill", 50_000, 250_000),
    INTERNET("Internet", "Viettel / VNPT", "bi-wifi", 180_000, 280_000),
    GAS("Khí đốt", "PetroVietnam Gas", "bi-fire", 100_000, 400_000);

    private final String tenHienThi;
    private final String nhaCungCap;
    private final String icon;
    private final long soTienMin;
    private final long soTienMax;

    LoaiDichVuThuHo(String tenHienThi, String nhaCungCap, String icon, long soTienMin, long soTienMax) {
        this.tenHienThi = tenHienThi;
        this.nhaCungCap = nhaCungCap;
        this.icon = icon;
        this.soTienMin = soTienMin;
        this.soTienMax = soTienMax;
    }

    public String getTenHienThi() { return tenHienThi; }
    public String getNhaCungCap() { return nhaCungCap; }
    public String getIcon()       { return icon; }
    public long getSoTienMin()    { return soTienMin; }
    public long getSoTienMax()    { return soTienMax; }

    public String getMoTa() {
        return tenHienThi + " (" + nhaCungCap + ")";
    }
}
