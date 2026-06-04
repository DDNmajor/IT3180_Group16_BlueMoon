package com.bluemoon.controller;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.dao.ThanhToanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final NhanKhauRepository nhanKhauRepository;
    private final KhoanThuRepository khoanThuRepository;
    private final ThanhToanRepository thanhToanRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tongHoGiaDinh", hoGiaDinhRepository.count());
        model.addAttribute("tongNhanKhau", nhanKhauRepository.count());
        model.addAttribute("tongKhoanThu", khoanThuRepository.count());
        model.addAttribute("tongTienThangNay", thanhToanRepository.sumSoTienDaNopThangNay());
        
        return "dashboard";
    }
}
