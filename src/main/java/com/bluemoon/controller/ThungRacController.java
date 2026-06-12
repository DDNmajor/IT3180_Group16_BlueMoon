package com.bluemoon.controller;

import com.bluemoon.service.HoGiaDinhService;
import com.bluemoon.service.KhoanThuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thung-rac")
@RequiredArgsConstructor
public class ThungRacController {

    private final HoGiaDinhService hoGiaDinhService;
    private final KhoanThuService  khoanThuService;

    @GetMapping
    public String index(@RequestParam(defaultValue = "ho") String tab, Model model) {
        model.addAttribute("tab",             tab);
        model.addAttribute("danhSachHoXoa",   hoGiaDinhService.findAllDeleted());
        model.addAttribute("danhSachKhoanXoa", khoanThuService.findAllDeleted());
        return "thung-rac/index";
    }

    @PostMapping("/ho-gia-dinh/{id}/khoi-phuc")
    public String khoiPhucHo(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            hoGiaDinhService.restore(id);
            ra.addFlashAttribute("successMsg", "Đã khôi phục hộ gia đình thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Khôi phục thất bại: " + e.getMessage());
        }
        return "redirect:/thung-rac?tab=ho";
    }

    @PostMapping("/ho-gia-dinh/{id}/xoa-vinh-vien")
    public String xoaVinhVienHo(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            hoGiaDinhService.permanentDelete(id);
            ra.addFlashAttribute("successMsg", "Đã xóa vĩnh viễn hộ gia đình.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/thung-rac?tab=ho";
    }

    @PostMapping("/khoan-thu/{id}/khoi-phuc")
    public String khoiPhucKhoanThu(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            khoanThuService.restore(id);
            ra.addFlashAttribute("successMsg", "Đã khôi phục khoản thu thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Khôi phục thất bại: " + e.getMessage());
        }
        return "redirect:/thung-rac?tab=khoan";
    }

    @PostMapping("/khoan-thu/{id}/xoa-vinh-vien")
    public String xoaVinhVienKhoanThu(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            khoanThuService.permanentDelete(id);
            ra.addFlashAttribute("successMsg", "Đã xóa vĩnh viễn khoản thu.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/thung-rac?tab=khoan";
    }
}
