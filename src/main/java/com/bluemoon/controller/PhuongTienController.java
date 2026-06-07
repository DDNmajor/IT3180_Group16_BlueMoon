package com.bluemoon.controller;

import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.LoaiXe;
import com.bluemoon.model.PhuongTien;
import com.bluemoon.service.HoGiaDinhService;
import com.bluemoon.service.PhuongTienService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/ho-gia-dinh/{idHo}/phuong-tien")
@RequiredArgsConstructor
public class PhuongTienController {

    private final PhuongTienService phuongTienService;
    private final HoGiaDinhService  hoGiaDinhService;

    @GetMapping("/them")
    public String themForm(@PathVariable Integer idHo, Model model, RedirectAttributes ra) {
        try {
            HoGiaDinh ho = hoGiaDinhService.findById(idHo);
            PhuongTien phuongTien = new PhuongTien();
            phuongTien.setHoGiaDinh(ho);
            model.addAttribute("phuongTien", phuongTien);
            model.addAttribute("hoGiaDinh",  ho);
            model.addAttribute("loaiXeList", LoaiXe.values());
            return "phuong-tien/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy hộ gia đình.");
            return "redirect:/ho-gia-dinh";
        }
    }

    @PostMapping("/them")
    public String them(@PathVariable Integer idHo,
                       @Valid @ModelAttribute("phuongTien") PhuongTien phuongTien,
                       BindingResult bindingResult,
                       @RequestParam(defaultValue = "DANG_KY_THEM") String lyDo,
                       Model model,
                       RedirectAttributes ra) {

        HoGiaDinh ho = hoGiaDinhService.findById(idHo);
        phuongTien.setHoGiaDinh(ho);

        if (!bindingResult.hasFieldErrors("bienSo")
                && phuongTienService.existsByBienSo(phuongTien.getBienSo())) {
            bindingResult.rejectValue("bienSo", "duplicate", "Biển số này đã được đăng ký");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("hoGiaDinh",  ho);
            model.addAttribute("loaiXeList", LoaiXe.values());
            return "phuong-tien/form";
        }

        int flagged = phuongTienService.save(phuongTien, lyDo);
        boolean applyNow = "NHAP_SAI".equals(lyDo) || LocalDate.now().getDayOfMonth() <= 25;
        if (flagged > 0) {
            ra.addFlashAttribute("warnMsg",
                    "Đã thêm phương tiện. " + flagged + " khoản phí xe đang nộp dở cần xem lại.");
        } else if (applyNow) {
            ra.addFlashAttribute("successMsg",
                    "Đã thêm phương tiện và cập nhật khoản phí xe tháng này.");
        } else {
            ra.addFlashAttribute("successMsg",
                    "Đã thêm phương tiện. Khoản phí xe sẽ được tính từ tháng sau (đã qua ngày 25).");
        }
        return "redirect:/ho-gia-dinh/" + idHo;
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer idHo,
                          @PathVariable Integer id,
                          Model model, RedirectAttributes ra) {
        try {
            HoGiaDinh ho  = hoGiaDinhService.findById(idHo);
            PhuongTien pt = phuongTienService.findById(id);
            model.addAttribute("phuongTien", pt);
            model.addAttribute("hoGiaDinh",  ho);
            model.addAttribute("loaiXeList", LoaiXe.values());
            return "phuong-tien/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy phương tiện cần sửa.");
            return "redirect:/ho-gia-dinh/" + idHo;
        }
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer idHo,
                      @PathVariable Integer id,
                      @Valid @ModelAttribute("phuongTien") PhuongTien phuongTien,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes ra) {

        HoGiaDinh ho = hoGiaDinhService.findById(idHo);
        phuongTien.setId(id);
        phuongTien.setHoGiaDinh(ho);

        if (!bindingResult.hasFieldErrors("bienSo")
                && phuongTienService.existsByBienSoForOther(phuongTien.getBienSo(), id)) {
            bindingResult.rejectValue("bienSo", "duplicate", "Biển số này đã được đăng ký");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("hoGiaDinh",  ho);
            model.addAttribute("loaiXeList", LoaiXe.values());
            return "phuong-tien/form";
        }

        int flagged = phuongTienService.save(phuongTien, "NHAP_SAI");
        if (flagged > 0) {
            ra.addFlashAttribute("warnMsg",
                    "Đã sửa phương tiện và điều chỉnh phí xe. "
                    + flagged + " khoản đang nộp dở cần xem lại.");
        } else {
            ra.addFlashAttribute("successMsg",
                    "Đã sửa phương tiện và điều chỉnh khoản phí xe tháng này.");
        }
        return "redirect:/ho-gia-dinh/" + idHo;
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer idHo,
                      @PathVariable Integer id,
                      @RequestParam(defaultValue = "BOT_XE") String lyDo,
                      RedirectAttributes ra) {
        try {
            int flagged = phuongTienService.delete(id, lyDo);
            boolean applyNow = "NHAP_SAI".equals(lyDo) || LocalDate.now().getDayOfMonth() <= 5;
            if (flagged > 0) {
                ra.addFlashAttribute("warnMsg",
                        "Đã xóa phương tiện. " + flagged + " khoản phí xe đang nộp dở cần xem lại.");
            } else if (applyNow) {
                ra.addFlashAttribute("successMsg",
                        "Đã xóa phương tiện và điều chỉnh khoản phí xe tháng này.");
            } else {
                ra.addFlashAttribute("successMsg",
                        "Đã xóa phương tiện. Khoản phí xe tháng hiện tại giữ nguyên, tháng sau sẽ điều chỉnh.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Xóa phương tiện thất bại: " + e.getMessage());
        }
        return "redirect:/ho-gia-dinh/" + idHo;
    }
}
