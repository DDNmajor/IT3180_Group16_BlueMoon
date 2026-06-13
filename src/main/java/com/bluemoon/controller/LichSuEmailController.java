package com.bluemoon.controller;

import com.bluemoon.model.LoaiEmail;
import com.bluemoon.service.LichSuEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/lich-su-email")
@RequiredArgsConstructor
public class LichSuEmailController {

    private final LichSuEmailService lichSuEmailService;

    @GetMapping
    public String list(@RequestParam(required = false) String toEmail,
                       @RequestParam(required = false) String soCanHo,
                       @RequestParam(required = false) LoaiEmail loaiEmail,
                       @RequestParam(required = false) String trangThai,
                       @RequestParam(required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                       @RequestParam(required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
                       Model model) {

        model.addAttribute("danhSach",
                lichSuEmailService.findWithFilter(toEmail, soCanHo, loaiEmail, trangThai, tuNgay, denNgay));
        model.addAttribute("loaiEmailValues", LoaiEmail.values());
        model.addAttribute("toEmailFilter",   toEmail);
        model.addAttribute("soCanHoFilter",   soCanHo);
        model.addAttribute("loaiEmailFilter", loaiEmail);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("tuNgayFilter",    tuNgay);
        model.addAttribute("denNgayFilter",   denNgay);
        return "lich-su-email/list";
    }
}
