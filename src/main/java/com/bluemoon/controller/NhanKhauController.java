package com.bluemoon.controller;

import com.bluemoon.model.BienDong;
import com.bluemoon.model.NhanKhau;
import com.bluemoon.service.BienDongService;
import com.bluemoon.service.HoGiaDinhService;
import com.bluemoon.service.NhanKhauService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nhan-khau")
@RequiredArgsConstructor
public class NhanKhauController {

    private final NhanKhauService  nhanKhauService;
    private final HoGiaDinhService hoGiaDinhService;
    private final BienDongService  bienDongService;

    @GetMapping
    public String list(@RequestParam(required = false) Integer idHo,
                       @RequestParam(required = false) String search,
                       Model model) {
        if (idHo != null) {
            model.addAttribute("danhSach", nhanKhauService.findByHoGiaDinh(idHo));
            model.addAttribute("hoGiaDinh", hoGiaDinhService.findById(idHo));
        } else if (search != null && !search.isBlank()) {
            var danhSach = nhanKhauService.search(search);
            model.addAttribute("danhSach", danhSach);
            model.addAttribute("search", search);
            if (danhSach.isEmpty()) {
                model.addAttribute("emptyMsg", "Không tìm thấy kết quả cho \"" + search + "\".");
            }
        } else {
            model.addAttribute("danhSach", nhanKhauService.findAll());
        }
        return "nhan-khau/list";
    }

    @GetMapping("/them")
    public String themForm(@RequestParam(required = false) Integer idHo, Model model) {
        NhanKhau nhanKhau = new NhanKhau();
        if (idHo != null) nhanKhau.setHoGiaDinh(hoGiaDinhService.findById(idHo));
        model.addAttribute("nhanKhau", nhanKhau);
        model.addAttribute("danhSachHo", hoGiaDinhService.findAll());
        model.addAttribute("idHo", idHo);
        return "nhan-khau/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("nhanKhau") NhanKhau nhanKhau,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Integer idHo,
                       Model model, RedirectAttributes ra) {

        // Resolve hoGiaDinh từ ID (select box dùng name, không bind đầy đủ)
        Integer idHoForm = nhanKhau.getHoGiaDinh() != null ? nhanKhau.getHoGiaDinh().getId() : null;
        if (idHoForm == null) {
            bindingResult.rejectValue("hoGiaDinh", "required", "Vui lòng chọn hộ gia đình");
        }

        if (!bindingResult.hasFieldErrors("cccd")
                && nhanKhau.getCccd() != null && !nhanKhau.getCccd().isBlank()
                && nhanKhauService.existsByCccd(nhanKhau.getCccd())) {
            bindingResult.rejectValue("cccd", "duplicate", "CCCD đã được đăng ký");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachHo", hoGiaDinhService.findAll());
            model.addAttribute("idHo", idHo);
            return "nhan-khau/form";
        }

        if (idHoForm != null) nhanKhau.setHoGiaDinh(hoGiaDinhService.findById(idHoForm));
        nhanKhauService.save(nhanKhau);
        ra.addFlashAttribute("successMsg", "Thêm nhân khẩu thành công.");
        return idHoForm != null
                ? "redirect:/ho-gia-dinh/" + idHoForm
                : "redirect:/nhan-khau";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("nhanKhau", nhanKhauService.findById(id));
            model.addAttribute("danhSachHo", hoGiaDinhService.findAll());
            return "nhan-khau/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy nhân khẩu.");
            return "redirect:/nhan-khau";
        }
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute("nhanKhau") NhanKhau nhanKhau,
                      BindingResult bindingResult,
                      Model model, RedirectAttributes ra) {

        NhanKhau cu;
        try {
            cu = nhanKhauService.findById(id);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy nhân khẩu.");
            return "redirect:/nhan-khau";
        }

        // CCCD uniqueness: only check if CCCD changed
        String cccdMoi = nhanKhau.getCccd();
        if (!bindingResult.hasFieldErrors("cccd")
                && cccdMoi != null && !cccdMoi.isBlank()
                && !cccdMoi.equals(cu.getCccd())
                && nhanKhauService.existsByCccd(cccdMoi)) {
            bindingResult.rejectValue("cccd", "duplicate", "CCCD đã được đăng ký");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachHo", hoGiaDinhService.findAll());
            return "nhan-khau/form";
        }

        nhanKhau.setId(id);
        nhanKhau.setNgayTao(cu.getNgayTao());
        // Resolve hoGiaDinh
        Integer idHoForm = nhanKhau.getHoGiaDinh() != null ? nhanKhau.getHoGiaDinh().getId() : null;
        if (idHoForm != null) nhanKhau.setHoGiaDinh(hoGiaDinhService.findById(idHoForm));
        if (nhanKhau.getTinhTrang() == null) nhanKhau.setTinhTrang(cu.getTinhTrang());

        nhanKhauService.save(nhanKhau);
        ra.addFlashAttribute("successMsg", "Cập nhật nhân khẩu thành công.");
        Integer redirectHo = nhanKhau.getHoGiaDinh() != null ? nhanKhau.getHoGiaDinh().getId() : null;
        return redirectHo != null ? "redirect:/ho-gia-dinh/" + redirectHo : "redirect:/nhan-khau";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            NhanKhau nk = nhanKhauService.findById(id);
            Integer idHo = nk.getHoGiaDinh() != null ? nk.getHoGiaDinh().getId() : null;
            nhanKhauService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa nhân khẩu thành công.");
            return idHo != null ? "redirect:/ho-gia-dinh/" + idHo : "redirect:/nhan-khau";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Xóa thất bại: " + e.getMessage());
            return "redirect:/nhan-khau";
        }
    }

    // ── Biến động ────────────────────────────────────────────────────

    @GetMapping("/{id}/bien-dong")
    public String bienDongForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            NhanKhau nk = nhanKhauService.findById(id);
            model.addAttribute("nhanKhau", nk);
            BienDong bd = new BienDong();
            bd.setNhanKhau(nk);
            model.addAttribute("bienDong", bd);
            return "nhan-khau/bien-dong-form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy nhân khẩu.");
            return "redirect:/nhan-khau";
        }
    }

    @PostMapping("/{id}/bien-dong")
    public String bienDong(@PathVariable Integer id,
                           @Valid @ModelAttribute("bienDong") BienDong bienDong,
                           BindingResult bindingResult,
                           Model model, RedirectAttributes ra) {

        NhanKhau nk = nhanKhauService.findById(id);
        bienDong.setNhanKhau(nk);

        if (bindingResult.hasErrors()) {
            model.addAttribute("nhanKhau", nk);
            return "nhan-khau/bien-dong-form";
        }

        bienDongService.save(bienDong);
        ra.addFlashAttribute("successMsg", "Ghi nhận biến động thành công.");
        return "redirect:/ho-gia-dinh/" + nk.getHoGiaDinh().getId();
    }
}
