package com.bluemoon.controller;

import com.bluemoon.model.KhoanThu;
import com.bluemoon.service.KhoanThuService;
import com.bluemoon.service.LoaiKhoanThuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String them(@ModelAttribute KhoanThu khoanThu, RedirectAttributes ra) {
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
    public String sua(@PathVariable Integer id, @ModelAttribute KhoanThu khoanThu, RedirectAttributes ra) {
        khoanThu.setId(id);
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
}
