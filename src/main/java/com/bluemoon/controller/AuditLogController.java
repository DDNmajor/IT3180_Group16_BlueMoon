package com.bluemoon.controller;

import com.bluemoon.service.AuditLogService;
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

    private final AuditLogService auditLogService;

    private static final List<String> LOAI_DOI_TUONG_LIST =
            List.of("Hộ gia đình", "Nhân khẩu", "Người dùng", "Khoản thu", "Thanh toán");

    @GetMapping
    public String list(@RequestParam(required = false) String loaiDoiTuong,
                       @RequestParam(required = false) String nguoiDung,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
                       Model model) {
        model.addAttribute("danhSach",
                auditLogService.findWithFilter(loaiDoiTuong, nguoiDung, tuNgay, denNgay));
        model.addAttribute("danhSachNguoiDung", auditLogService.findDistinctNguoiDung());
        model.addAttribute("loaiDoiTuongList",  LOAI_DOI_TUONG_LIST);
        model.addAttribute("filterLoai",       loaiDoiTuong);
        model.addAttribute("filterNguoiDung",  nguoiDung);
        model.addAttribute("filterTuNgay",     tuNgay);
        model.addAttribute("filterDenNgay",    denNgay);
        return "audit-log/list";
    }
}
