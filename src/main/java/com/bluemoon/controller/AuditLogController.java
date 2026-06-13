package com.bluemoon.controller;

import com.bluemoon.model.LoaiEmail;
import com.bluemoon.service.AuditLogService;
import com.bluemoon.service.LichSuEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService    auditLogService;
    private final LichSuEmailService lichSuEmailService;

    private static final List<String> LOAI_DOI_TUONG_LIST =
            List.of("Hộ gia đình", "Nhân khẩu", "Người dùng", "Khoản thu", "Thanh toán");

    @GetMapping
    public String list(
            // chọn tab
            @RequestParam(defaultValue = "nhat-ky") String tab,
            // filter audit log
            @RequestParam(required = false) String loaiDoiTuong,
            @RequestParam(required = false) String nguoiDung,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            // filter email
            @RequestParam(required = false) String toEmail,
            @RequestParam(required = false) String soCanHo,
            @RequestParam(required = false) LoaiEmail loaiEmail,
            @RequestParam(required = false) String trangThaiEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate emailTuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate emailDenNgay,
            Model model) {

        boolean isEmailTab = "email".equals(tab);

        if (isEmailTab) {
            model.addAttribute("emailList",
                    lichSuEmailService.findWithFilter(toEmail, soCanHo, loaiEmail, trangThaiEmail, emailTuNgay, emailDenNgay));
            model.addAttribute("loaiEmailValues", LoaiEmail.values());
            model.addAttribute("toEmailFilter",     toEmail);
            model.addAttribute("soCanHoFilter",     soCanHo);
            model.addAttribute("loaiEmailFilter",   loaiEmail);
            model.addAttribute("trangThaiFilter",   trangThaiEmail);
            model.addAttribute("emailTuNgayFilter", emailTuNgay);
            model.addAttribute("emailDenNgayFilter",emailDenNgay);
        } else {
            model.addAttribute("danhSach",
                    auditLogService.findWithFilter(loaiDoiTuong, nguoiDung, tuNgay, denNgay));
            model.addAttribute("danhSachNguoiDung", auditLogService.findDistinctNguoiDung());
            model.addAttribute("loaiDoiTuongList",  LOAI_DOI_TUONG_LIST);
            model.addAttribute("filterLoai",        loaiDoiTuong);
            model.addAttribute("filterNguoiDung",   nguoiDung);
            model.addAttribute("filterTuNgay",      tuNgay);
            model.addAttribute("filterDenNgay",     denNgay);
        }

        model.addAttribute("activeTab", tab);
        return "audit-log/list";
    }
}
