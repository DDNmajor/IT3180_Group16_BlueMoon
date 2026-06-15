package com.bluemoon.model;

public enum LoaiEmail {
    THONG_BAO_KHOAN_THU("Thông báo khoản thu"),
    CHAO_MUNG_HO_MOI("Chào mừng hộ mới"),
    NHAC_NO_TU_DONG("Nhắc nợ tự động"),
    NHAC_NO_THU_CONG("Nhắc nợ thủ công"),
    THU_HO_THONG_BAO("Thu hộ - Thông báo hóa đơn"),
    THU_HO_XAC_NHAN("Thu hộ - Xác nhận thanh toán");

    private final String tenHienThi;

    LoaiEmail(String tenHienThi) { this.tenHienThi = tenHienThi; }

    public String getTenHienThi() { return tenHienThi; }
}
