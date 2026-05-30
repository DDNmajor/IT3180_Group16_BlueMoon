package com.bluemoon.controller;

import com.bluemoon.model.ThanhToan;
import com.bluemoon.service.HoGiaDinhService;
import com.bluemoon.service.KhoanThuService;
import com.bluemoon.service.ThanhToanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanController {

    private final ThanhToanService thanhToanService;
    private final HoGiaDinhService hoGiaDinhService;
    private final KhoanThuService khoanThuService;

    @GetMapping
    public String list(@RequestParam(required = false) Integer idHo,
                       @RequestParam(required = false) Integer idKhoan,
                       Model model) {
        if (idHo != null) {
            model.addAttribute("danhSach", thanhToanService.findByHoGiaDinh(idHo));
            model.addAttribute("hoGiaDinh", hoGiaDinhService.findById(idHo));
        } else if (idKhoan != null) {
            model.addAttribute("danhSach", thanhToanService.findByKhoanThu(idKhoan));
            model.addAttribute("khoanThu", khoanThuService.findById(idKhoan));
        } else {
            model.addAttribute("danhSach", thanhToanService.findAll());
        }
        return "thanh-toan/list";
    }

    @GetMapping("/them")
    public String themForm(@RequestParam(required = false) Integer idHo,
                           @RequestParam(required = false) Integer idKhoan,
                           Model model) {
        ThanhToan thanhToan = new ThanhToan();
        if (idHo != null) {
            thanhToan.setHoGiaDinh(hoGiaDinhService.findById(idHo));
        }
        if (idKhoan != null) {
            thanhToan.setKhoanThu(khoanThuService.findById(idKhoan));
        }
        model.addAttribute("thanhToan", thanhToan);
        model.addAttribute("danhSachHo", hoGiaDinhService.findAll());
        model.addAttribute("danhSachKhoan", khoanThuService.findAll());
        return "thanh-toan/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute ThanhToan thanhToan, RedirectAttributes ra) {
        Integer idHo = thanhToan.getHoGiaDinh() != null ? thanhToan.getHoGiaDinh().getId() : null;
        Integer idKhoan = thanhToan.getKhoanThu() != null ? thanhToan.getKhoanThu().getId() : null;
        if (idHo != null && idKhoan != null && thanhToanService.daThanhToan(idHo, idKhoan)) {
            ra.addFlashAttribute("errorMsg", "Hộ gia đình này đã thanh toán khoản thu đó rồi.");
            return "redirect:/thanh-toan/them";
        }
        thanhToanService.save(thanhToan);
        ra.addFlashAttribute("successMsg", "Ghi nhận thanh toán thành công.");
        return "redirect:/thanh-toan";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        thanhToanService.delete(id);
        ra.addFlashAttribute("successMsg", "Xóa thanh toán thành công.");
        return "redirect:/thanh-toan";
    }
}
