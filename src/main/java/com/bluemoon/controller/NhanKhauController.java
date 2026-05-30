package com.bluemoon.controller;

import com.bluemoon.model.NhanKhau;
import com.bluemoon.service.HoGiaDinhService;
import com.bluemoon.service.NhanKhauService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nhan-khau")
@RequiredArgsConstructor
public class NhanKhauController {

    private final NhanKhauService nhanKhauService;
    private final HoGiaDinhService hoGiaDinhService;

    @GetMapping
    public String list(@RequestParam(required = false) Integer idHo,
                       @RequestParam(required = false) String search,
                       Model model) {
        if (idHo != null) {
            model.addAttribute("danhSach", nhanKhauService.findByHoGiaDinh(idHo));
            model.addAttribute("hoGiaDinh", hoGiaDinhService.findById(idHo));
        } else if (search != null && !search.isBlank()) {
            model.addAttribute("danhSach", nhanKhauService.search(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("danhSach", nhanKhauService.findAll());
        }
        return "nhan-khau/list";
    }

    @GetMapping("/them")
    public String themForm(@RequestParam(required = false) Integer idHo, Model model) {
        NhanKhau nhanKhau = new NhanKhau();
        if (idHo != null) {
            nhanKhau.setHoGiaDinh(hoGiaDinhService.findById(idHo));
        }
        model.addAttribute("nhanKhau", nhanKhau);
        model.addAttribute("danhSachHo", hoGiaDinhService.findAll());
        return "nhan-khau/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute NhanKhau nhanKhau, RedirectAttributes ra) {
        if (nhanKhau.getCccd() != null && !nhanKhau.getCccd().isBlank()
                && nhanKhauService.existsByCccd(nhanKhau.getCccd())) {
            ra.addFlashAttribute("errorMsg", "CCCD đã được đăng ký.");
            return "redirect:/nhan-khau/them";
        }
        nhanKhauService.save(nhanKhau);
        ra.addFlashAttribute("successMsg", "Thêm nhân khẩu thành công.");
        return "redirect:/nhan-khau";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("nhanKhau", nhanKhauService.findById(id));
        model.addAttribute("danhSachHo", hoGiaDinhService.findAll());
        return "nhan-khau/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id, @ModelAttribute NhanKhau nhanKhau, RedirectAttributes ra) {
        nhanKhau.setId(id);
        nhanKhauService.save(nhanKhau);
        ra.addFlashAttribute("successMsg", "Cập nhật nhân khẩu thành công.");
        return "redirect:/nhan-khau";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        nhanKhauService.delete(id);
        ra.addFlashAttribute("successMsg", "Xóa nhân khẩu thành công.");
        return "redirect:/nhan-khau";
    }
}
