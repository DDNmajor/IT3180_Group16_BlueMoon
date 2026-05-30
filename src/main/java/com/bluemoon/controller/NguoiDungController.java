package com.bluemoon.controller;

import com.bluemoon.model.NguoiDung;
import com.bluemoon.model.VaiTro;
import com.bluemoon.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nguoi-dung")
@RequiredArgsConstructor
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("danhSach", nguoiDungService.findAll());
        return "nguoi-dung/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        model.addAttribute("danhSachVaiTro", VaiTro.values());
        return "nguoi-dung/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute NguoiDung nguoiDung, RedirectAttributes ra) {
        if (nguoiDungService.existsByTenDangNhap(nguoiDung.getTenDangNhap())) {
            ra.addFlashAttribute("errorMsg", "Tên đăng nhập đã tồn tại.");
            return "redirect:/nguoi-dung/them";
        }
        nguoiDungService.save(nguoiDung);
        ra.addFlashAttribute("successMsg", "Thêm người dùng thành công.");
        return "redirect:/nguoi-dung";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("nguoiDung", nguoiDungService.findById(id));
        model.addAttribute("danhSachVaiTro", VaiTro.values());
        return "nguoi-dung/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id, @ModelAttribute NguoiDung nguoiDung, RedirectAttributes ra) {
        nguoiDung.setId(id);
        nguoiDungService.save(nguoiDung);
        ra.addFlashAttribute("successMsg", "Cập nhật người dùng thành công.");
        return "redirect:/nguoi-dung";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        nguoiDungService.delete(id);
        ra.addFlashAttribute("successMsg", "Xóa người dùng thành công.");
        return "redirect:/nguoi-dung";
    }
}
