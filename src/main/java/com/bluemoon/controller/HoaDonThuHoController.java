package com.bluemoon.controller;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.model.LoaiDichVuThuHo;
import com.bluemoon.model.TrangThaiHoaDonThuHo;
import com.bluemoon.service.HoaDonThuHoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/thu-ho")
@RequiredArgsConstructor
public class HoaDonThuHoController {

    private final HoaDonThuHoService hoaDonService;
    private final HoGiaDinhRepository hoRepo;

    @GetMapping
    public String list(
            @RequestParam(required = false) Integer idHo,
            @RequestParam(required = false) String loaiDichVu,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String thang,
            Model model) {

        LoaiDichVuThuHo loaiEnum  = loaiDichVu != null && !loaiDichVu.isBlank()
                ? LoaiDichVuThuHo.valueOf(loaiDichVu) : null;
        TrangThaiHoaDonThuHo ttEnum = trangThai != null && !trangThai.isBlank()
                ? TrangThaiHoaDonThuHo.valueOf(trangThai) : null;
        YearMonth ym = thang != null && !thang.isBlank() ? YearMonth.parse(thang) : null;

        model.addAttribute("danhSach",      hoaDonService.findWithFilter(idHo, loaiEnum, ttEnum, ym));
        model.addAttribute("danhSachHo",    hoRepo.findAll());
        model.addAttribute("loaiDichVuAll", LoaiDichVuThuHo.values());
        model.addAttribute("trangThaiAll",  TrangThaiHoaDonThuHo.values());
        model.addAttribute("idHoFilter",       idHo);
        model.addAttribute("loaiDichVuFilter", loaiEnum);
        model.addAttribute("trangThaiFilter",  ttEnum);
        model.addAttribute("thangFilter",      ym != null ? ym.toString() : null);
        model.addAttribute("choThanhToan",     TrangThaiHoaDonThuHo.CHO_THANH_TOAN);
        return "thu-ho/list";
    }

    @GetMapping("/tao")
    public String taoForm(Model model) {
        model.addAttribute("danhSachHo",    hoRepo.findAll());
        model.addAttribute("loaiDichVuAll", LoaiDichVuThuHo.values());
        model.addAttribute("kyMacDinh",     YearMonth.now().toString());
        model.addAttribute("hanMacDinh",    YearMonth.now().atEndOfMonth().toString());
        return "thu-ho/form";
    }

    @PostMapping("/tao")
    public String tao(
            @RequestParam Integer idHo,
            @RequestParam String loaiDichVu,
            @RequestParam String ky,
            @RequestParam BigDecimal soTien,
            @RequestParam(required = false) String hanThanhToan,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes ra) {
        try {
            YearMonth ymKy = YearMonth.parse(ky);
            LocalDate han  = hanThanhToan != null && !hanThanhToan.isBlank()
                    ? LocalDate.parse(hanThanhToan) : null;
            hoaDonService.taoHoaDon(idHo, LoaiDichVuThuHo.valueOf(loaiDichVu),
                    ymKy, soTien, han, ghiChu);
            ra.addFlashAttribute("successMsg", "Đã tạo hóa đơn thu hộ thành công.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/thu-ho";
    }

    @GetMapping("/gen")
    public String genForm(Model model) {
        model.addAttribute("loaiDichVuAll", LoaiDichVuThuHo.values());
        model.addAttribute("kyMacDinh",     YearMonth.now().toString());
        model.addAttribute("hanMacDinh",    YearMonth.now().atEndOfMonth().toString());
        return "thu-ho/gen";
    }

    @PostMapping("/gen")
    public String gen(
            @RequestParam List<String> loaiDichVu,
            @RequestParam String ky,
            @RequestParam(required = false) String hanThanhToan,
            RedirectAttributes ra) {
        try {
            YearMonth ymKy = YearMonth.parse(ky);
            LocalDate han  = hanThanhToan != null && !hanThanhToan.isBlank()
                    ? LocalDate.parse(hanThanhToan) : null;

            int tongCount = 0;
            List<String> tenDvList = new java.util.ArrayList<>();
            for (String dv : loaiDichVu) {
                LoaiDichVuThuHo loaiEnum = LoaiDichVuThuHo.valueOf(dv);
                tongCount += hoaDonService.genHangLoat(loaiEnum, ymKy, han);
                tenDvList.add(loaiEnum.getTenHienThi());
            }
            ra.addFlashAttribute("successMsg",
                    "Đã gen " + tongCount + " hóa đơn ("
                    + String.join(", ", tenDvList) + ") kỳ "
                    + ymKy.toString().replace("-", "/") + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi khi gen dữ liệu: " + e.getMessage());
        }
        return "redirect:/thu-ho/gen";
    }

    @PostMapping("/{id}/gui-email")
    public String guiEmail(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            hoaDonService.guiEmailThongBao(id);
            ra.addFlashAttribute("successMsg", "Đã gửi email thông báo hóa đơn.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/thu-ho";
    }

    @PostMapping("/gui-email-hang-loat")
    public String guiEmailHangLoat(
            @RequestParam(required = false) String loaiDichVu,
            @RequestParam(required = false) String thang,
            RedirectAttributes ra) {
        try {
            LoaiDichVuThuHo loaiEnum = loaiDichVu != null && !loaiDichVu.isBlank()
                    ? LoaiDichVuThuHo.valueOf(loaiDichVu) : null;
            YearMonth ym = thang != null && !thang.isBlank() ? YearMonth.parse(thang) : null;
            int count = hoaDonService.guiEmailHangLoat(loaiEnum, ym);
            ra.addFlashAttribute("successMsg", "Đã gửi email cho " + count + " hóa đơn chưa gửi.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/thu-ho";
    }

    @PostMapping("/{id}/xac-nhan")
    public String xacNhan(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        try {
            hoaDonService.xacNhanThanhToan(id, auth.getName());
            ra.addFlashAttribute("successMsg", "Đã xác nhận thanh toán và gửi email biên nhận.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/thu-ho";
    }

    @PostMapping("/{id}/huy")
    public String huy(@PathVariable Integer id,
                      @RequestParam(required = false) String ghiChuHuy,
                      RedirectAttributes ra) {
        try {
            hoaDonService.huyHoaDon(id, ghiChuHuy);
            ra.addFlashAttribute("successMsg", "Đã hủy hóa đơn.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/thu-ho";
    }
}
