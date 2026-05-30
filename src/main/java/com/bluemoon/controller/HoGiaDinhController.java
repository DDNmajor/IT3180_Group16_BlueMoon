package com.bluemoon.controller;

import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.service.HoGiaDinhService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ho-gia-dinh")
@RequiredArgsConstructor
public class HoGiaDinhController {

    private final HoGiaDinhService hoGiaDinhService;

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("danhSach", hoGiaDinhService.search(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("danhSach", hoGiaDinhService.findAll());
        }
        return "ho-gia-dinh/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("hoGiaDinh", hoGiaDinhService.findById(id));
        return "ho-gia-dinh/detail";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("hoGiaDinh", new HoGiaDinh());
        return "ho-gia-dinh/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute HoGiaDinh hoGiaDinh, RedirectAttributes ra) {
        if (hoGiaDinhService.existsBySoCanHo(hoGiaDinh.getSoCanHo())) {
            ra.addFlashAttribute("errorMsg", "Số căn hộ đã tồn tại.");
            return "redirect:/ho-gia-dinh/them";
        }
        hoGiaDinhService.save(hoGiaDinh);
        ra.addFlashAttribute("successMsg", "Thêm hộ gia đình thành công.");
        return "redirect:/ho-gia-dinh";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("hoGiaDinh", hoGiaDinhService.findById(id));
        return "ho-gia-dinh/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id, @ModelAttribute HoGiaDinh hoGiaDinh, RedirectAttributes ra) {
        hoGiaDinh.setId(id);
        hoGiaDinhService.save(hoGiaDinh);
        ra.addFlashAttribute("successMsg", "Cập nhật hộ gia đình thành công.");
        return "redirect:/ho-gia-dinh";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        hoGiaDinhService.delete(id);
        ra.addFlashAttribute("successMsg", "Xóa hộ gia đình thành công.");
        return "redirect:/ho-gia-dinh";
    }
}
