package com.bluemoon.controller;

import com.bluemoon.model.KhoanThu;
import com.bluemoon.service.KhoanThuService;
import com.bluemoon.service.LoaiKhoanThuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

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
    public String them(@Valid @ModelAttribute KhoanThu khoanThu, BindingResult bindingResult, 
                       @RequestParam(value = "tenLoaiKhoanThu", required = false) String tenLoaiKhoanThu,
                       Model model, RedirectAttributes ra) {
        validateKhoanThu(khoanThu, tenLoaiKhoanThu, bindingResult);
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
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute KhoanThu khoanThu, BindingResult bindingResult, 
                      @RequestParam(value = "tenLoaiKhoanThu", required = false) String tenLoaiKhoanThu,
                      Model model, RedirectAttributes ra) {
        validateKhoanThu(khoanThu, tenLoaiKhoanThu, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai", loaiKhoanThuService.findAll());
            return "khoan-thu/form";
        }
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

    private void validateKhoanThu(KhoanThu khoanThu, String tenLoaiKhoanThu, BindingResult bindingResult) {
        if (tenLoaiKhoanThu == null || tenLoaiKhoanThu.trim().isEmpty()) {
            bindingResult.rejectValue("loaiKhoanThu", "error.khoanThu", "Vui lòng nhập loại khoản thu");
        } else {
            java.util.Optional<com.bluemoon.model.LoaiKhoanThu> loaiOpt = loaiKhoanThuService.findByTenLoai(tenLoaiKhoanThu.trim());
            if (loaiOpt.isPresent()) {
                khoanThu.setLoaiKhoanThu(loaiOpt.get());
            } else {
                bindingResult.rejectValue("loaiKhoanThu", "error.khoanThu", "Loại khoản thu không tồn tại");
            }
        }

        if (khoanThu.getKyThu() != null) {
            int kyThuYear = khoanThu.getKyThu().getYear();
            if (kyThuYear < 2000 || kyThuYear > 2100) {
                bindingResult.rejectValue("kyThu", "error.khoanThu", "Năm kỳ thu phải từ 2000 đến 2100");
            }
        }

        if (khoanThu.getHanNop() != null) {
            int hanNopYear = khoanThu.getHanNop().getYear();
            if (hanNopYear < 2000 || hanNopYear > 2100) {
                bindingResult.rejectValue("hanNop", "error.khoanThu", "Năm hạn nộp phải từ 2000 đến 2100");
            }
            if (khoanThu.getKyThu() != null && khoanThu.getHanNop().isBefore(khoanThu.getKyThu())) {
                bindingResult.rejectValue("hanNop", "error.khoanThu", "Hạn nộp phải sau hoặc bằng kỳ thu");
            }
        }
    }
}
