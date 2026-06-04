package com.bluemoon.controller;

import com.bluemoon.model.LoaiKhoanThu;
import com.bluemoon.service.LoaiKhoanThuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String them(@Valid @ModelAttribute("loaiKhoanThu") LoaiKhoanThu loaiKhoanThu,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes ra) {

        if (!bindingResult.hasFieldErrors("tenLoai")
                && loaiKhoanThuService.existsByTenLoai(loaiKhoanThu.getTenLoai())) {
            bindingResult.rejectValue("tenLoai", "duplicate", "Tên loại khoản thu đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            return "loai-khoan-thu/form";
        }

        loaiKhoanThuService.save(loaiKhoanThu);
        ra.addFlashAttribute("successMsg", "Thêm loại khoản thu thành công.");
        return "redirect:/loai-khoan-thu";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("loaiKhoanThu", loaiKhoanThuService.findById(id));
            return "loai-khoan-thu/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy loại khoản thu cần sửa.");
            return "redirect:/loai-khoan-thu";
        }
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute("loaiKhoanThu") LoaiKhoanThu loaiKhoanThu,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes ra) {

        if (!bindingResult.hasFieldErrors("tenLoai")) {
            loaiKhoanThuService.findByTenLoai(loaiKhoanThu.getTenLoai())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            bindingResult.rejectValue("tenLoai", "duplicate", "Tên loại khoản thu đã tồn tại");
                        }
                    });
        }

        if (bindingResult.hasErrors()) {
            return "loai-khoan-thu/form";
        }

        loaiKhoanThu.setId(id);
        loaiKhoanThuService.save(loaiKhoanThu);
        ra.addFlashAttribute("successMsg", "Cập nhật loại khoản thu thành công.");
        return "redirect:/loai-khoan-thu";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            loaiKhoanThuService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa loại khoản thu thành công.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("errorMsg", "Không thể xóa vì đang có khoản thu thuộc loại này.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/loai-khoan-thu";
    }
}
