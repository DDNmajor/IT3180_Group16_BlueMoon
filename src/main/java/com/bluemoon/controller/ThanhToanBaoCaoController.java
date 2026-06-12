package com.bluemoon.controller;

import com.bluemoon.dto.NoPhiHoDto;
import com.bluemoon.dto.ThongKeKhoanThuDto;
import com.bluemoon.service.BaoCaoThanhToanService;
import com.bluemoon.service.KhoanThuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanBaoCaoController {

    private final BaoCaoThanhToanService baoCaoThanhToanService;
    private final KhoanThuService khoanThuService;

    @GetMapping("/no-phi")
    public String noPhi(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer idKhoanThu,
                        @RequestParam(required = false) BigDecimal noTren,
                        Model model) {

        List<NoPhiHoDto> danhSachNoPhi = baoCaoThanhToanService.getBangNoPhi(keyword, idKhoanThu, noTren);

        model.addAttribute("danhSachNoPhi", danhSachNoPhi);
        model.addAttribute("danhSachKhoanThu", khoanThuService.findAll());

        model.addAttribute("keyword", keyword);
        model.addAttribute("idKhoanThu", idKhoanThu);
        model.addAttribute("noTren", noTren);

        model.addAttribute("tongSoHoNo", danhSachNoPhi.size());
        model.addAttribute("tongTienNo", baoCaoThanhToanService.tongTienNo(danhSachNoPhi));

        return "thanh-toan/no-phi";
    }

    @GetMapping("/thong-ke")
    public String thongKe(@RequestParam(required = false) String thang,
                          @RequestParam(required = false, defaultValue = "ALL") String loai,
                          Model model) {

        YearMonth ym = (thang == null || thang.isBlank()) ? YearMonth.now() : YearMonth.parse(thang);
        List<ThongKeKhoanThuDto> thongKeList = baoCaoThanhToanService.getThongKeKhoanDongGop(ym, loai);

        model.addAttribute("thang", ym.toString());
        model.addAttribute("loai", loai);
        model.addAttribute("thongKeList", thongKeList);

        model.addAttribute("tongTienYeuCau", baoCaoThanhToanService.tongTienYeuCau(thongKeList));
        model.addAttribute("tongTienDaThu", baoCaoThanhToanService.tongTienDaThu(thongKeList));
        model.addAttribute("tongTienConThieu", baoCaoThanhToanService.tongTienConThieu(thongKeList));
        model.addAttribute("tongSoHoDangNo", baoCaoThanhToanService.tongSoHoDangNoItNhatMotKhoanTrongThongKe(thongKeList));

        return "thanh-toan/thong-ke";
    }

    @GetMapping("/no-phi/email/{idHo}")
    public String emailNhacNo(@PathVariable Integer idHo, Model model) {
        NoPhiHoDto noPhi = baoCaoThanhToanService.getNoPhiCuaHo(idHo);

        String subject = baoCaoThanhToanService.taoTieuDeEmail(noPhi);
        String body = baoCaoThanhToanService.taoNoiDungEmail(noPhi);

        model.addAttribute("noPhi", noPhi);
        model.addAttribute("subject", subject);
        model.addAttribute("body", body);
        model.addAttribute("subjectEncoded", encode(subject));
        model.addAttribute("bodyEncoded", encode(body));

        return "thanh-toan/email-nhac-no";
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}