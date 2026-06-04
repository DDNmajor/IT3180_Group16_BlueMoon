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

@Controller
@RequestMapping("/khoan-thu")
@RequiredArgsConstructor
public class KhoanThuController {

    private final KhoanThuService khoanThuService;
    private final LoaiKhoanThuService loaiKhoanThuService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("danhSach", khoanThuService.findAll());
        return "khoan-thu/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("khoanThu", new KhoanThu());
        model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
        return "khoan-thu/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("khoanThu") KhoanThu khoanThu,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes ra) {

        validateKhoanThu(khoanThu, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
            return "khoan-thu/form";
        }

        khoanThuService.save(khoanThu);
        ra.addFlashAttribute("successMsg", "Thêm khoản thu thành công.");
        return "redirect:/khoan-thu";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("khoanThu", khoanThuService.findById(id));
        model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
        return "khoan-thu/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute("khoanThu") KhoanThu khoanThu,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes ra) {

        khoanThu.setId(id);
        validateKhoanThu(khoanThu, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
            return "khoan-thu/form";
        }

        khoanThuService.save(khoanThu);
        ra.addFlashAttribute("successMsg", "Cập nhật khoản thu thành công.");
        return "redirect:/khoan-thu";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        khoanThuService.delete(id);
        ra.addFlashAttribute("successMsg", "Xóa khoản thu thành công.");
        return "redirect:/khoan-thu";
    }

    private void validateKhoanThu(KhoanThu khoanThu, BindingResult bindingResult) {
        if (khoanThu.getLoaiKhoanThu() == null || khoanThu.getLoaiKhoanThu().getId() == null) {
            bindingResult.rejectValue("loaiKhoanThu", "loaiKhoanThu.required", "Vui lòng chọn loại khoản thu");
        }

        if (khoanThu.getKyThu() != null) {
            int kyThuYear = khoanThu.getKyThu().getYear();
            if (kyThuYear < 2000 || kyThuYear > 2100) {
                bindingResult.rejectValue("kyThu", "kyThu.invalid", "Năm kỳ thu phải từ 2000 đến 2100");
            }
        }

        if (khoanThu.getHanNop() != null) {
            int hanNopYear = khoanThu.getHanNop().getYear();
            if (hanNopYear < 2000 || hanNopYear > 2100) {
                bindingResult.rejectValue("hanNop", "hanNop.invalid", "Năm hạn nộp phải từ 2000 đến 2100");
            }

            if (khoanThu.getKyThu() != null && khoanThu.getHanNop().isBefore(khoanThu.getKyThu())) {
                bindingResult.rejectValue("hanNop", "hanNop.invalid", "Hạn nộp phải sau hoặc bằng kỳ thu");
            }
        }
    }
}