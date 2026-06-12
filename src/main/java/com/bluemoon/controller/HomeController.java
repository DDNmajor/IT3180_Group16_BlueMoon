package com.bluemoon.controller;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.dto.ThongKeKhoanThuDto;
import com.bluemoon.service.BaoCaoThanhToanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.YearMonth;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final NhanKhauRepository nhanKhauRepository;
    private final KhoanThuRepository khoanThuRepository;
    private final BaoCaoThanhToanService baoCaoThanhToanService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        YearMonth thangHienTai = YearMonth.now();

        List<ThongKeKhoanThuDto> thongKeList =
                baoCaoThanhToanService.getThongKeKhoanDongGop(thangHienTai, "ALL");

        model.addAttribute("tongHoGiaDinh", hoGiaDinhRepository.count());
        model.addAttribute("tongNhanKhau", nhanKhauRepository.count());
        model.addAttribute("tongKhoanThu", khoanThuRepository.count());
        model.addAttribute("tongTienThangNay", baoCaoThanhToanService.tongTienDaThuThangNay());

        model.addAttribute("thang", thangHienTai.toString());
        model.addAttribute("thongKeList", thongKeList);

        model.addAttribute("tongTienYeuCau", baoCaoThanhToanService.tongTienYeuCau(thongKeList));
        model.addAttribute("tongTienDaThu", baoCaoThanhToanService.tongTienDaThu(thongKeList));
        model.addAttribute("tongTienConThieu", baoCaoThanhToanService.tongTienConThieu(thongKeList));
        model.addAttribute(
                "tongSoHoDangNo",
                baoCaoThanhToanService.tongSoHoDangNoItNhatMotKhoanTrongThongKe(thongKeList)
        );

        return "dashboard";
    }
}