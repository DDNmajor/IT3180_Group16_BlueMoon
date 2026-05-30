package com.bluemoon.controller;

import com.bluemoon.model.LoaiKhoanThu;
import com.bluemoon.service.LoaiKhoanThuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/loai-khoan-thu")
@RequiredArgsConstructor
public class LoaiKhoanThuController {

    private final LoaiKhoanThuService loaiKhoanThuService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("danhSach", loaiKhoanThuService.findAll());
        return "loai-khoan-thu/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("loaiKhoanThu", new LoaiKhoanThu());
        return "loai-khoan-thu/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute LoaiKhoanThu loaiKhoanThu, RedirectAttributes ra) {
        if (loaiKhoanThuService.existsByTenLoai(loaiKhoanThu.getTenLoai())) {
            ra.addFlashAttribute("errorMsg", "Loại khoản thu này đã tồn tại.");
            return "redirect:/loai-khoan-thu/them";
        }
        loaiKhoanThuService.save(loaiKhoanThu);
        ra.addFlashAttribute("successMsg", "Thêm loại khoản thu thành công.");
        return "redirect:/loai-khoan-thu";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("loaiKhoanThu", loaiKhoanThuService.findById(id));
        return "loai-khoan-thu/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id, @ModelAttribute LoaiKhoanThu loaiKhoanThu, RedirectAttributes ra) {
        loaiKhoanThu.setId(id);
        loaiKhoanThuService.save(loaiKhoanThu);
        ra.addFlashAttribute("successMsg", "Cập nhật loại khoản thu thành công.");
        return "redirect:/loai-khoan-thu";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        loaiKhoanThuService.delete(id);
        ra.addFlashAttribute("successMsg", "Xóa loại khoản thu thành công.");
        return "redirect:/loai-khoan-thu";
    }
}
