package com.bluemoon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error",    required = false) String error,
                            @RequestParam(value = "disabled", required = false) String disabled,
                            @RequestParam(value = "logout",   required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
        if (disabled != null) {
            model.addAttribute("errorMsg", "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Bạn đã đăng xuất thành công.");
        }
        return "auth/login";
    }
}
