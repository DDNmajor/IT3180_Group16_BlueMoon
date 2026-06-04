package com.bluemoon.controller;

import com.bluemoon.model.KhoanThu;
import com.bluemoon.service.KhoanThuService;
import com.bluemoon.service.LoaiKhoanThuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;

@Controller
@RequestMapping("/khoan-thu")
@RequiredArgsConstructor
public class KhoanThuController {

    private final KhoanThuService     khoanThuService;
    private final LoaiKhoanThuService loaiKhoanThuService;

    @GetMapping
    public String list(
            @RequestParam(required = false) Integer idLoai,
            @RequestParam(required = false) String  trangThai,
            @RequestParam(required = false) String  thang,
            Model model) {

        YearMonth ym = (thang != null && !thang.isBlank())
                ? YearMonth.parse(thang)
                : YearMonth.now();

        model.addAttribute("danhSach",       khoanThuService.findWithFilter(idLoai, trangThai, ym));
        model.addAttribute("danhSachLoai",   loaiKhoanThuService.findAll());
        model.addAttribute("idLoaiFilter",   idLoai);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("thangFilter",    ym.toString());
        return "khoan-thu/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("khoanThu",     new KhoanThu());
        model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
        return "khoan-thu/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute KhoanThu khoanThu,
                       BindingResult bindingResult,
                       Model model, RedirectAttributes ra) {
        validateDates(khoanThu, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
            return "khoan-thu/form";
        }
        try {
            khoanThuService.save(khoanThu);
            ra.addFlashAttribute("successMsg", "Thêm khoản thu thành công.");
            return "redirect:/khoan-thu";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("maKhoanThu", "duplicate", e.getMessage());
            model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
            return "khoan-thu/form";
        }
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("khoanThu",     khoanThuService.findById(id));
        model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
        return "khoan-thu/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute KhoanThu khoanThu,
                      BindingResult bindingResult,
                      Model model, RedirectAttributes ra) {
        validateDates(khoanThu, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
            return "khoan-thu/form";
        }
        khoanThu.setId(id);
        try {
            khoanThuService.save(khoanThu);
            ra.addFlashAttribute("successMsg", "Cập nhật khoản thu thành công.");
            return "redirect:/khoan-thu";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("maKhoanThu", "duplicate", e.getMessage());
            model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
            return "khoan-thu/form";
        }
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            khoanThuService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa khoản thu thành công.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/khoan-thu";
    }

    private void validateDates(KhoanThu khoanThu, BindingResult bindingResult) {
        if (khoanThu.getKyThu() != null) {
            int year = khoanThu.getKyThu().getYear();
            if (year < 2000 || year > 2100) {
                bindingResult.rejectValue("kyThu", "error.khoanThu", "Năm kỳ thu phải từ 2000 đến 2100");
            }
        }
        if (khoanThu.getHanNop() != null) {
            int year = khoanThu.getHanNop().getYear();
            if (year < 2000 || year > 2100) {
                bindingResult.rejectValue("hanNop", "error.khoanThu", "Năm hạn nộp phải từ 2000 đến 2100");
            } else if (khoanThu.getKyThu() != null && khoanThu.getHanNop().isBefore(khoanThu.getKyThu())) {
                bindingResult.rejectValue("hanNop", "error.khoanThu", "Hạn nộp phải sau hoặc bằng kỳ thu");
            }
        }
    }
}
