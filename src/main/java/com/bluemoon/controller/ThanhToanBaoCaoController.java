package com.bluemoon.controller;

import com.bluemoon.dto.NoPhiHoDto;
import com.bluemoon.dto.ThongKeKhoanThuDto;
import com.bluemoon.service.BaoCaoThanhToanService;
import com.bluemoon.service.KhoanThuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.bluemoon.service.ExcelExportService;
import com.bluemoon.model.KhoanThu;

import java.io.IOException;
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
    private final ExcelExportService excelExportService;

    @GetMapping("/thong-ke/export/{idKhoanThu}")
    public ResponseEntity<byte[]> exportBaoCao(@PathVariable Integer idKhoanThu) throws IOException {
        byte[] excelData = excelExportService.exportBaoCaoKhoanThu(idKhoanThu);
        KhoanThu kt = khoanThuService.findById(idKhoanThu);
        String fileName = "BaoCao_" + kt.getMaKhoanThu() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

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