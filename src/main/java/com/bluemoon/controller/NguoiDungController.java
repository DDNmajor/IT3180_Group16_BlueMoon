package com.bluemoon.controller;

import com.bluemoon.model.NguoiDung;
import com.bluemoon.model.VaiTro;
import com.bluemoon.service.NguoiDungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nguoi-dung")
@RequiredArgsConstructor
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("danhSach", nguoiDungService.findAll());
        return "nguoi-dung/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("nguoiDung", new NguoiDung());
        model.addAttribute("danhSachVaiTro", VaiTro.values());
        return "nguoi-dung/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes ra) {

        if (nguoiDungService.existsByTenDangNhap(nguoiDung.getTenDangNhap())) {
            bindingResult.rejectValue("tenDangNhap", "duplicate", "Tên đăng nhập đã được sử dụng");
        }
        if (nguoiDung.getMatKhau() == null || nguoiDung.getMatKhau().isBlank()) {
            bindingResult.rejectValue("matKhau", "required", "Mật khẩu không được để trống");
        } else if (nguoiDung.getMatKhau().length() < 6) {
            bindingResult.rejectValue("matKhau", "size", "Mật khẩu phải từ 6 ký tự trở lên");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachVaiTro", VaiTro.values());
            return "nguoi-dung/form";
        }

        nguoiDungService.save(nguoiDung);
        ra.addFlashAttribute("successMsg", "Thêm người dùng thành công.");
        return "redirect:/nguoi-dung";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("nguoiDung", nguoiDungService.findById(id));
            model.addAttribute("danhSachVaiTro", VaiTro.values());
            return "nguoi-dung/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy người dùng cần sửa.");
            return "redirect:/nguoi-dung";
        }
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute("nguoiDung") NguoiDung nguoiDung,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes ra,
                      Authentication auth) {

        NguoiDung nguoiDungCu;
        try {
            nguoiDungCu = nguoiDungService.findById(id);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy người dùng cần cập nhật.");
            return "redirect:/nguoi-dung";
        }

        // Không cho tự đổi vai trò của chính mình
        if (auth.getName().equals(nguoiDungCu.getTenDangNhap())
                && nguoiDung.getVaiTro() != nguoiDungCu.getVaiTro()) {
            bindingResult.rejectValue("vaiTro", "selfRole",
                    "Không thể thay đổi quyền của tài khoản đang đăng nhập");
        }

        // Validate mật khẩu mới nếu có nhập, bỏ trống = giữ nguyên
        String matKhauMoi = nguoiDung.getMatKhau();
        if (matKhauMoi != null && !matKhauMoi.isBlank() && matKhauMoi.length() < 6) {
            bindingResult.rejectValue("matKhau", "size", "Mật khẩu phải từ 6 ký tự trở lên");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachVaiTro", VaiTro.values());
            return "nguoi-dung/form";
        }

        // Khoá username, giữ ngày tạo
        nguoiDung.setId(id);
        nguoiDung.setTenDangNhap(nguoiDungCu.getTenDangNhap());
        nguoiDung.setNgayTao(nguoiDungCu.getNgayTao());
        nguoiDung.setDoiMatKhauLanDau(nguoiDungCu.getDoiMatKhauLanDau());

        // Nếu không nhập mật khẩu mới, giữ lại mật khẩu cũ (đã encode)
        if (matKhauMoi == null || matKhauMoi.isBlank()) {
            nguoiDung.setMatKhau(nguoiDungCu.getMatKhau());
        }

        nguoiDungService.save(nguoiDung);
        ra.addFlashAttribute("successMsg", "Cập nhật người dùng thành công.");
        return "redirect:/nguoi-dung";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra, Authentication auth) {
        try {
            NguoiDung nd = nguoiDungService.findById(id);
            if (auth.getName().equals(nd.getTenDangNhap())) {
                ra.addFlashAttribute("errorMsg", "Không thể xóa tài khoản đang đăng nhập.");
                return "redirect:/nguoi-dung";
            }
            nguoiDungService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa người dùng thành công.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("errorMsg", "Không thể xóa người dùng vì đang có dữ liệu liên quan.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Xóa người dùng thất bại: " + e.getMessage());
        }
        return "redirect:/nguoi-dung";
    }
}
