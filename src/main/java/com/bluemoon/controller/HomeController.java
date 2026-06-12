package com.bluemoon.controller;

import com.bluemoon.dao.AuditLogRepository;
import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.NhanKhauRepository;
import com.bluemoon.dto.NoPhiHoDto;
import com.bluemoon.dto.ThongKeKhoanThuDto;
import com.bluemoon.model.AuditLog;
import com.bluemoon.model.KhoanThu;
import com.bluemoon.service.BaoCaoThanhToanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final NhanKhauRepository nhanKhauRepository;
    private final KhoanThuRepository khoanThuRepository;
    private final AuditLogRepository auditLogRepository;
    private final BaoCaoThanhToanService baoCaoThanhToanService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        YearMonth thangHienTai = YearMonth.now();
        YearMonth thangTruoc = thangHienTai.minusMonths(1);
        LocalDate homNay = LocalDate.now();

        // --- Thống kê chính ---
        List<ThongKeKhoanThuDto> thongKeList =
                baoCaoThanhToanService.getThongKeKhoanDongGop(thangHienTai, "ALL");

        BigDecimal tongTienThangNay  = baoCaoThanhToanService.tongTienDaThuTheoThang(thangHienTai);
        BigDecimal tongTienThangTruoc = baoCaoThanhToanService.tongTienDaThuTheoThang(thangTruoc);
        BigDecimal tongTienYeuCau    = baoCaoThanhToanService.tongTienYeuCau(thongKeList);
        BigDecimal tongTienDaThu     = baoCaoThanhToanService.tongTienDaThu(thongKeList);
        BigDecimal tongTienConThieu  = baoCaoThanhToanService.tongTienConThieu(thongKeList);
        long tongSoHoDangNo = baoCaoThanhToanService.tongSoHoDangNoItNhatMotKhoanTrongThongKe(thongKeList);

        // --- Delta metric cards ---
        LocalDateTime dauThang = thangHienTai.atDay(1).atStartOfDay();
        LocalDateTime cuoiNgay = homNay.atTime(23, 59, 59);
        long deltaHo       = hoGiaDinhRepository.countByNgayTaoBetween(dauThang, cuoiNgay);
        long deltaNhanKhau = nhanKhauRepository.countByNgayTaoBetween(dauThang, cuoiNgay);

        // --- Progress bar ---
        int tiLeDaThuInt = 0;
        int tiLeConNoInt = 0;
        if (tongTienYeuCau.compareTo(BigDecimal.ZERO) > 0) {
            tiLeDaThuInt = tongTienDaThu.multiply(BigDecimal.valueOf(100))
                    .divide(tongTienYeuCau, 0, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(100)).intValue();
            tiLeConNoInt = tongTienConThieu.multiply(BigDecimal.valueOf(100))
                    .divide(tongTienYeuCau, 0, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(100 - tiLeDaThuInt)).intValue();
        }

        // --- Bar chart 6 tháng ---
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartValues = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = thangHienTai.minusMonths(i);
            chartLabels.add("T" + ym.getMonthValue() + "/" + ym.getYear());
            chartValues.add(baoCaoThanhToanService.tongTienDaThuTheoThang(ym));
        }

        // --- Khoản thu sắp đến hạn (30 ngày tới) ---
        List<KhoanThu> sapHanList = khoanThuRepository
                .findByHanNopBetweenOrderByHanNop(homNay, homNay.plusDays(30))
                .stream().limit(5).collect(Collectors.toList());

        // --- Top 3 hộ nợ nhiều nhất ---
        List<NoPhiHoDto> topNoList = baoCaoThanhToanService
                .getBangNoPhi(null, null, BigDecimal.ZERO)
                .stream()
                .sorted(Comparator.comparing(NoPhiHoDto::getTongNo).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // --- Hoạt động gần đây (admin only, rendered via sec:authorize) ---
        List<AuditLog> hanhDongGanDay = auditLogRepository.findTop5ByOrderByThoiGianDesc();

        model.addAttribute("tongHoGiaDinh",   hoGiaDinhRepository.count());
        model.addAttribute("tongNhanKhau",    nhanKhauRepository.count());
        model.addAttribute("tongKhoanThu",    khoanThuRepository.count());
        model.addAttribute("tongTienThangNay",  tongTienThangNay);
        model.addAttribute("tongTienThangTruoc", tongTienThangTruoc);
        model.addAttribute("tienTangThangNay",
                tongTienThangTruoc.compareTo(BigDecimal.ZERO) > 0
                && tongTienThangNay.compareTo(tongTienThangTruoc) > 0);
        model.addAttribute("tienGiamThangNay",
                tongTienThangTruoc.compareTo(BigDecimal.ZERO) > 0
                && tongTienThangNay.compareTo(tongTienThangTruoc) < 0);
        model.addAttribute("deltaHoThangNay",       deltaHo);
        model.addAttribute("deltaNhanKhauThangNay", deltaNhanKhau);
        model.addAttribute("thang",           thangHienTai.toString());
        model.addAttribute("thongKeList",     thongKeList);
        model.addAttribute("tongTienYeuCau",  tongTienYeuCau);
        model.addAttribute("tongTienDaThu",   tongTienDaThu);
        model.addAttribute("tongTienConThieu", tongTienConThieu);
        model.addAttribute("tongSoHoDangNo",  tongSoHoDangNo);
        model.addAttribute("tiLeDaThuInt",    tiLeDaThuInt);
        model.addAttribute("tiLeConNoInt",    tiLeConNoInt);
        model.addAttribute("chartLabels",     chartLabels);
        model.addAttribute("chartValues",     chartValues);
        model.addAttribute("sapHanList",      sapHanList);
        model.addAttribute("topNoList",       topNoList);
        model.addAttribute("hanhDongGanDay",  hanhDongGanDay);
        model.addAttribute("ngayHomNay",      homNay);

        return "dashboard";
    }
}
