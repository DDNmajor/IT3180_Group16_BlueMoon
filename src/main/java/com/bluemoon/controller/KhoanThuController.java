package com.bluemoon.controller;

import com.bluemoon.model.KhoanThu;
import com.bluemoon.model.LoaiTinhPhi;
import com.bluemoon.service.KhoanThuService;
import com.bluemoon.service.LoaiKhoanThuService;

import java.math.BigDecimal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/khoan-thu")
@RequiredArgsConstructor
public class KhoanThuController {

    private final KhoanThuService     khoanThuService;
    private final LoaiKhoanThuService loaiKhoanThuService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // type=month gửi "yyyy-MM", chuyển sang ngày đầu tháng
        binder.registerCustomEditor(LocalDate.class, "kyThu", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                } else if (text.length() == 7) {
                    setValue(YearMonth.parse(text).atDay(1));
                } else {
                    setValue(LocalDate.parse(text));
                }
            }
        });
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Integer idLoai,
            @RequestParam(required = false) String  trangThai,
            @RequestParam(required = false) String  thang,
            Model model) {

        YearMonth ym = (thang != null && !thang.isBlank()) ? YearMonth.parse(thang) : null;

        model.addAttribute("danhSach",        khoanThuService.findWithFilter(idLoai, trangThai, ym));
        model.addAttribute("danhSachLoai",    loaiKhoanThuService.findAll());
        model.addAttribute("idLoaiFilter",    idLoai);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("thangFilter",     ym != null ? ym.toString() : null);
        return "khoan-thu/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("khoanThu",          new KhoanThu());
        model.addAttribute("danhSachLoai",      loaiKhoanThuService.findAll());
        model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
        return "khoan-thu/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("khoanThu") KhoanThu khoanThu,
                       BindingResult bindingResult,
                       Model model, RedirectAttributes ra) {
        validateKhoanThu(khoanThu, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai",      loaiKhoanThuService.findAll());
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "khoan-thu/form";
        }
        try {
            khoanThuService.save(khoanThu);
            ra.addFlashAttribute("successMsg", "Thêm khoản thu thành công.");
            return "redirect:/khoan-thu";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("maKhoanThu", "duplicate", e.getMessage());
            model.addAttribute("danhSachLoai",      loaiKhoanThuService.findAll());
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "khoan-thu/form";
        }
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("khoanThu",          khoanThuService.findById(id));
        model.addAttribute("danhSachLoai",      loaiKhoanThuService.findAll());
        model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
        return "khoan-thu/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute("khoanThu") KhoanThu khoanThu,
                      BindingResult bindingResult,
                      Model model, RedirectAttributes ra) {
        khoanThu.setId(id);
        validateKhoanThu(khoanThu, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai",      loaiKhoanThuService.findAll());
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "khoan-thu/form";
        }
        try {
            khoanThuService.save(khoanThu);
            ra.addFlashAttribute("successMsg", "Cập nhật khoản thu thành công.");
            return "redirect:/khoan-thu";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("maKhoanThu", "duplicate", e.getMessage());
            model.addAttribute("danhSachLoai",      loaiKhoanThuService.findAll());
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "khoan-thu/form";
        }
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            khoanThuService.delete(id);
            ra.addFlashAttribute("undoMsg",
                    "Đã chuyển khoản thu vào thùng rác. " +
                    "<a href='/thung-rac?tab=khoan' class='alert-link'>Xem thùng rác</a> để khôi phục.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/khoan-thu";
    }

    private void validateKhoanThu(KhoanThu khoanThu, BindingResult bindingResult) {
        if (khoanThu.getLoaiKhoanThu() == null || khoanThu.getLoaiKhoanThu().getId() == null) {
            bindingResult.rejectValue("loaiKhoanThu", "loaiKhoanThu.required", "Vui lòng chọn loại khoản thu");
        }
        LoaiTinhPhi ltp = khoanThu.getLoaiTinhPhi();
        if (ltp == LoaiTinhPhi.PER_M2
                && (khoanThu.getDonGiaPerM2() == null
                    || khoanThu.getDonGiaPerM2().compareTo(BigDecimal.ZERO) <= 0)) {
            bindingResult.rejectValue("donGiaPerM2", "donGiaPerM2.required",
                    "Vui lòng nhập đơn giá/m² lớn hơn 0");
        }
        if (ltp == LoaiTinhPhi.PER_XE) {
            khoanThu.setSoTien(BigDecimal.ZERO);
            if (khoanThu.getGiaXeMay() == null || khoanThu.getGiaXeMay().compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("giaXeMay", "required", "Vui lòng nhập giá xe máy lớn hơn 0");
            }
            if (khoanThu.getGiaOto() == null || khoanThu.getGiaOto().compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("giaOto", "required", "Vui lòng nhập giá ô tô lớn hơn 0");
            }
        }
        if (ltp == LoaiTinhPhi.PER_PERSON
                && (khoanThu.getSoTien() == null
                    || khoanThu.getSoTien().compareTo(BigDecimal.ZERO) <= 0)) {
            bindingResult.rejectValue("soTien", "soTien.required",
                    "Vui lòng nhập đơn giá/người lớn hơn 0");
        }
        if (khoanThu.getKyThu() != null) {
            int year = khoanThu.getKyThu().getYear();
            if (year < 2000 || year > 2100) {
                bindingResult.rejectValue("kyThu", "kyThu.invalid", "Năm kỳ thu phải từ 2000 đến 2100");
            }
        }
        if (khoanThu.getHanNop() != null) {
            int year = khoanThu.getHanNop().getYear();
            if (year < 2000 || year > 2100) {
                bindingResult.rejectValue("hanNop", "hanNop.invalid", "Năm hạn nộp phải từ 2000 đến 2100");
            } else if (khoanThu.getKyThu() != null && khoanThu.getHanNop().isBefore(khoanThu.getKyThu())) {
                bindingResult.rejectValue("hanNop", "hanNop.invalid", "Hạn nộp phải sau hoặc bằng kỳ thu");
            }
        }
    }
}
