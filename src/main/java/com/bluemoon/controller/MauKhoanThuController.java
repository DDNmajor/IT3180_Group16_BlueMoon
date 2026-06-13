package com.bluemoon.controller;

import com.bluemoon.model.LoaiApDung;
import com.bluemoon.model.LoaiTinhPhi;
import com.bluemoon.model.MauKhoanThu;
import com.bluemoon.service.LoaiKhoanThuService;
import com.bluemoon.service.MauKhoanThuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mau-khoan-thu")
@RequiredArgsConstructor
public class MauKhoanThuController {

    private final MauKhoanThuService  mauKhoanThuService;
    private final LoaiKhoanThuService loaiKhoanThuService;

    @GetMapping
    public String list(Model model) {
        var danhSach = mauKhoanThuService.findAll();
        model.addAttribute("danhSach", danhSach);
        model.addAttribute("khoanThuMap",
                danhSach.stream().collect(Collectors.toMap(
                        MauKhoanThu::getId,
                        m -> mauKhoanThuService.findKhoanThuCuaMau(m.getId()))));
        return "mau-khoan-thu/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("mauKhoanThu", new MauKhoanThu());
        model.addAttribute("danhSachLoai",
                loaiKhoanThuService.findByLoaiApDung(LoaiApDung.BAT_BUOC_DINH_KY));
        model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
        return "mau-khoan-thu/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("mauKhoanThu") MauKhoanThu mau,
                       BindingResult bindingResult,
                       Model model, RedirectAttributes ra) {
        validateLoai(mau, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai",
                    loaiKhoanThuService.findByLoaiApDung(LoaiApDung.BAT_BUOC_DINH_KY));
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "mau-khoan-thu/form";
        }
        try {
            mauKhoanThuService.save(mau);
            ra.addFlashAttribute("successMsg", "Thêm mẫu khoản thu thành công.");
            return "redirect:/mau-khoan-thu";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("maMauPrefix", "duplicate", e.getMessage());
            model.addAttribute("danhSachLoai",
                    loaiKhoanThuService.findByLoaiApDung(LoaiApDung.BAT_BUOC_DINH_KY));
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "mau-khoan-thu/form";
        }
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model) {
        model.addAttribute("mauKhoanThu", mauKhoanThuService.findById(id));
        model.addAttribute("danhSachLoai",
                loaiKhoanThuService.findByLoaiApDung(LoaiApDung.BAT_BUOC_DINH_KY));
        model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
        return "mau-khoan-thu/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute("mauKhoanThu") MauKhoanThu mau,
                      BindingResult bindingResult,
                      Model model, RedirectAttributes ra) {
        mau.setId(id);
        validateLoai(mau, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoai",
                    loaiKhoanThuService.findByLoaiApDung(LoaiApDung.BAT_BUOC_DINH_KY));
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "mau-khoan-thu/form";
        }
        try {
            mauKhoanThuService.save(mau);
            ra.addFlashAttribute("successMsg", "Cập nhật mẫu khoản thu thành công.");
            return "redirect:/mau-khoan-thu";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("maMauPrefix", "duplicate", e.getMessage());
            model.addAttribute("danhSachLoai",
                    loaiKhoanThuService.findByLoaiApDung(LoaiApDung.BAT_BUOC_DINH_KY));
            model.addAttribute("loaiTinhPhiValues", LoaiTinhPhi.values());
            return "mau-khoan-thu/form";
        }
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            mauKhoanThuService.delete(id);
            ra.addFlashAttribute("successMsg", "Đã xóa mẫu khoản thu.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/mau-khoan-thu";
    }

    @PostMapping("/{id}/doi-trang-thai")
    public String doiTrangThai(@PathVariable Integer id, RedirectAttributes ra) {
        boolean nowActive = mauKhoanThuService.toggleActive(id);
        ra.addFlashAttribute("successMsg",
                nowActive ? "Mẫu đã được kích hoạt." : "Mẫu đã được tạm dừng.");
        return "redirect:/mau-khoan-thu";
    }

    @PostMapping("/{id}/tao-ky")
    public String taoKy(@PathVariable Integer id,
                        @RequestParam String thang,
                        RedirectAttributes ra) {
        try {
            YearMonth ym = YearMonth.parse(thang);
            var khoanThu = mauKhoanThuService.taoKhoanThuChoKy(id, ym);
            String kyStr = ym.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            ra.addFlashAttribute("successMsg", "Đã tạo khoản thu cho kỳ " + kyStr + ".");
            if (khoanThu.getLoaiTinhPhi() == LoaiTinhPhi.THU_HO) {
                return "redirect:/thanh-toan/nhap-thu-ho/" + khoanThu.getId();
            }
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi khi tạo: " + e.getMessage());
        }
        return "redirect:/mau-khoan-thu";
    }

    private void validateLoai(MauKhoanThu mau, BindingResult br) {
        if (mau.getLoaiKhoanThu() == null || mau.getLoaiKhoanThu().getId() == null) {
            br.rejectValue("loaiKhoanThu", "required", "Vui lòng chọn loại khoản thu");
        }
        LoaiTinhPhi ltp = mau.getLoaiTinhPhi();
        if (ltp == LoaiTinhPhi.PER_M2
                && (mau.getDonGiaPerM2() == null
                    || mau.getDonGiaPerM2().compareTo(BigDecimal.ZERO) <= 0)) {
            br.rejectValue("donGiaPerM2", "donGiaPerM2.required",
                    "Vui lòng nhập đơn giá/m² lớn hơn 0");
        }
        if (ltp == LoaiTinhPhi.PER_XE) {
            mau.setSoTien(BigDecimal.ZERO);
            if (mau.getGiaXeMay() == null || mau.getGiaXeMay().compareTo(BigDecimal.ZERO) <= 0) {
                br.rejectValue("giaXeMay", "required", "Vui lòng nhập giá xe máy lớn hơn 0");
            }
            if (mau.getGiaOto() == null || mau.getGiaOto().compareTo(BigDecimal.ZERO) <= 0) {
                br.rejectValue("giaOto", "required", "Vui lòng nhập giá ô tô lớn hơn 0");
            }
        }
        if (ltp == LoaiTinhPhi.THU_HO) {
            mau.setSoTien(BigDecimal.ZERO);
        }
        if (ltp == LoaiTinhPhi.PER_PERSON
                && (mau.getSoTien() == null
                    || mau.getSoTien().compareTo(BigDecimal.ZERO) <= 0)) {
            br.rejectValue("soTien", "soTien.required",
                    "Vui lòng nhập đơn giá/người lớn hơn 0");
        }
    }
}
