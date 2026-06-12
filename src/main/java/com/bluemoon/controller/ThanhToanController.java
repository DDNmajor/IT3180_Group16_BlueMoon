package com.bluemoon.controller;

import com.bluemoon.model.*;
import com.bluemoon.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanController {

    private final ThanhToanService   thanhToanService;
    private final HoGiaDinhService   hoGiaDinhService;
    private final KhoanThuService    khoanThuService;
    private final NguoiDungService   nguoiDungService;

    @GetMapping
    public String list(@RequestParam(required = false) Integer idHo,
                       @RequestParam(required = false) Integer idKhoan,
                       @RequestParam(required = false) String  timKiem,
                       @RequestParam(required = false) String  trangThai,
                       Model model) {

        List<ThanhToan> danhSach;

        if (idHo != null && idKhoan != null) {
            danhSach = thanhToanService.findByHoGiaDinhAndKhoanThu(idHo, idKhoan);
            model.addAttribute("hoGiaDinhFilter", hoGiaDinhService.findById(idHo));
            model.addAttribute("khoanThuFilter",  khoanThuService.findById(idKhoan));
        } else if (idHo != null) {
            danhSach = thanhToanService.findByHoGiaDinh(idHo);
            model.addAttribute("hoGiaDinhFilter", hoGiaDinhService.findById(idHo));
        } else if (idKhoan != null) {
            danhSach = thanhToanService.findByKhoanThu(idKhoan);
            model.addAttribute("khoanThuFilter", khoanThuService.findById(idKhoan));
        } else if (timKiem != null && !timKiem.isBlank()) {
            List<HoGiaDinh> matched = hoGiaDinhService.searchByCanHoOrChuHo(timKiem);
            danhSach = matched.stream()
                    .flatMap(h -> thanhToanService.findByHoGiaDinh(h.getId()).stream())
                    .collect(Collectors.toList());
            model.addAttribute("timKiem", timKiem);
        } else {
            danhSach = thanhToanService.findAll();
        }

        if (trangThai != null && !trangThai.isBlank()) {
            TrangThaiThanhToan ts = TrangThaiThanhToan.valueOf(trangThai);
            danhSach = danhSach.stream()
                    .filter(t -> t.getTrangThai() == ts)
                    .collect(Collectors.toList());
        }

        model.addAttribute("danhSach",        danhSach);
        model.addAttribute("danhSachHo",      hoGiaDinhService.findAll());
        model.addAttribute("danhSachKhoan",   khoanThuService.findAll());
        model.addAttribute("trangThaiFilter", (trangThai != null && !trangThai.isBlank()) ? trangThai : null);
        model.addAttribute("trangThaiValues", TrangThaiThanhToan.values());
        return "thanh-toan/list";
    }

    @GetMapping("/theo-doi")
    public String theoDoi(@RequestParam(required = false) String  thang,
                          @RequestParam(required = false) Integer idKhoanThu,
                          Model model) {

        YearMonth ym = (thang != null && !thang.isBlank())
                ? YearMonth.parse(thang)
                : YearMonth.now();

        List<KhoanThu> khoanThuTrongKy = khoanThuService.findByKyThu(
                ym.atDay(1), ym.atEndOfMonth());

        model.addAttribute("khoanThuTrongKy",  khoanThuTrongKy);
        model.addAttribute("thangFilter",      ym.toString());
        model.addAttribute("idKhoanThuFilter", idKhoanThu);

        if (!khoanThuTrongKy.isEmpty()) {
            KhoanThu selected = (idKhoanThu != null)
                    ? khoanThuService.findById(idKhoanThu)
                    : khoanThuTrongKy.get(0);

            List<ThanhToan> payments = thanhToanService.findByKhoanThu(selected.getId());

            Map<Integer, ThanhToan> paymentMap = payments.stream()
                    .collect(Collectors.toMap(
                            tt -> tt.getHoGiaDinh().getId(),
                            tt -> tt,
                            (a, b) -> a));

            boolean isTuNguyen = selected.getLoaiKhoanThu() != null
                    && selected.getLoaiKhoanThu().getLoaiApDung() == LoaiApDung.TU_NGUYEN;

            long soDaDong = payments.stream()
                    .filter(tt -> tt.getTrangThai() == TrangThaiThanhToan.DA_DONG
                               || tt.getTrangThai() == TrangThaiThanhToan.DONG_DU)
                    .count();
            long soConNo = payments.stream()
                    .filter(tt -> tt.getTrangThai() == TrangThaiThanhToan.CON_NO)
                    .count();

            model.addAttribute("selectedKhoanThu", selected);
            model.addAttribute("tatCaHo",    hoGiaDinhService.findAll());
            model.addAttribute("paymentMap", paymentMap);
            model.addAttribute("isTuNguyen", isTuNguyen);
            model.addAttribute("soDaDong",   soDaDong);
            model.addAttribute("soConNo",    soConNo);
        }

        return "thanh-toan/theo-doi";
    }

    @GetMapping("/them")
    public String themForm(@RequestParam(required = false) Integer idHo,
                           @RequestParam(required = false) Integer idKhoan,
                           @RequestParam(required = false) String  timKiemHo,
                           Model model) {

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setNgayNop(LocalDate.now());
        thanhToan.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);

        if (timKiemHo != null && !timKiemHo.isBlank()) {
            List<HoGiaDinh> ketQua = hoGiaDinhService.searchByCanHoOrChuHo(timKiemHo);
            model.addAttribute("timKiemHo",     timKiemHo);
            model.addAttribute("ketQuaTimKiem", ketQua);
            if (ketQua.size() == 1) {
                idHo = ketQua.get(0).getId();
            }
        }

        if (idHo != null) {
            thanhToan.setHoGiaDinh(hoGiaDinhService.findById(idHo));

            List<Integer> daDongIds = thanhToanService.findByHoGiaDinh(idHo).stream()
                    .filter(tt -> tt.getTrangThai() == TrangThaiThanhToan.DA_DONG
                               || tt.getTrangThai() == TrangThaiThanhToan.DONG_DU)
                    .map(tt -> tt.getKhoanThu().getId())
                    .collect(Collectors.toList());
            List<KhoanThu> availableFees = khoanThuService.findAll().stream()
                    .filter(kt -> !daDongIds.contains(kt.getId()))
                    .collect(Collectors.toList());
            model.addAttribute("danhSachKhoan", availableFees);
        } else {
            model.addAttribute("danhSachKhoan", khoanThuService.findAll());
        }

        if (idKhoan != null) {
            thanhToan.setKhoanThu(khoanThuService.findById(idKhoan));
        }

        model.addAttribute("thanhToan",      thanhToan);
        model.addAttribute("danhSachHo",     hoGiaDinhService.findAll());
        model.addAttribute("phuongThucList", PhuongThucThanhToan.values());
        model.addAttribute("today",          LocalDate.now().toString());
        return "thanh-toan/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute ThanhToan thanhToan,
                       Authentication auth,
                       RedirectAttributes ra) {

        Integer idHo    = thanhToan.getHoGiaDinh() != null ? thanhToan.getHoGiaDinh().getId() : null;
        Integer idKhoan = thanhToan.getKhoanThu()  != null ? thanhToan.getKhoanThu().getId()  : null;

        if (idHo == null) {
            ra.addFlashAttribute("errorMsg", "Vui lòng chọn hộ gia đình.");
            return "redirect:/thanh-toan/them";
        }
        if (idKhoan == null) {
            ra.addFlashAttribute("errorMsg", "Vui lòng chọn khoản thu.");
            return "redirect:/thanh-toan/them?idHo=" + idHo;
        }
        if (thanhToan.getSoTienDaNop() == null
                || thanhToan.getSoTienDaNop().compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("errorMsg", "Số tiền phải lớn hơn 0.");
            return "redirect:/thanh-toan/them?idHo=" + idHo + "&idKhoan=" + idKhoan;
        }
        if (thanhToan.getNgayNop() != null && thanhToan.getNgayNop().isAfter(LocalDate.now())) {
            ra.addFlashAttribute("errorMsg", "Ngày thanh toán không được lớn hơn ngày hiện tại.");
            return "redirect:/thanh-toan/them?idHo=" + idHo + "&idKhoan=" + idKhoan;
        }
        if (thanhToanService.daDongHoanTat(idHo, idKhoan)) {
            ra.addFlashAttribute("errorMsg", "Hộ gia đình này đã hoàn thành thanh toán khoản thu đã chọn.");
            return "redirect:/thanh-toan/them?idHo=" + idHo;
        }

        thanhToan.setHoGiaDinh(hoGiaDinhService.findById(idHo));
        thanhToan.setKhoanThu(khoanThuService.findById(idKhoan));

        try {
            thanhToan.setNguoiThu(nguoiDungService.findByTenDangNhap(auth.getName()));
        } catch (Exception ignored) {}

        if (thanhToan.getPhuongThuc() == null) {
            thanhToan.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);
        }

        thanhToanService.save(thanhToan);
        ra.addFlashAttribute("successMsg", "Ghi nhận thanh toán thành công.");
        return "redirect:/thanh-toan?idHo=" + idHo;
    }

    @PostMapping("/nop-them/{id}")
    public String nopThem(@PathVariable Integer id,
                          @RequestParam BigDecimal soTienThem,
                          @RequestParam(required = false) Integer idHo,
                          Authentication auth,
                          RedirectAttributes ra) {
        if (soTienThem == null || soTienThem.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("errorMsg", "Số tiền nộp thêm phải lớn hơn 0.");
            return idHo != null ? "redirect:/thanh-toan?idHo=" + idHo : "redirect:/thanh-toan";
        }
        NguoiDung nguoiThu = null;
        try { nguoiThu = nguoiDungService.findByTenDangNhap(auth.getName()); } catch (Exception ignored) {}
        ThanhToan saved = thanhToanService.nopThem(id, soTienThem, nguoiThu);
        ra.addFlashAttribute("successMsg",
                String.format("Đã cộng thêm %,.0f đ — Trạng thái mới: %s",
                        soTienThem.doubleValue(), saved.getTrangThai().getTenHienThi()));
        Integer redirectHo = idHo != null ? idHo
                : (saved.getHoGiaDinh() != null ? saved.getHoGiaDinh().getId() : null);
        return redirectHo != null ? "redirect:/thanh-toan?idHo=" + redirectHo : "redirect:/thanh-toan";
    }

    @PostMapping("/hoan-tien/{id}")
    public String baoDaHoanTien(@PathVariable Integer id,
                                @RequestParam(required = false) Integer idHo,
                                Authentication auth,
                                RedirectAttributes ra) {
        NguoiDung nguoiThu = null;
        try { nguoiThu = nguoiDungService.findByTenDangNhap(auth.getName()); } catch (Exception ignored) {}
        ThanhToan saved = thanhToanService.baoDaHoanTien(id, nguoiThu);
        ra.addFlashAttribute("successMsg", "Đã ghi nhận hoàn trả tiền thừa — khoản thu chuyển sang Đã đóng.");
        Integer redirectHo = idHo != null ? idHo
                : (saved.getHoGiaDinh() != null ? saved.getHoGiaDinh().getId() : null);
        return redirectHo != null ? "redirect:/thanh-toan?idHo=" + redirectHo : "redirect:/thanh-toan";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        boolean deleted = thanhToanService.delete(id);
        if (deleted) {
            ra.addFlashAttribute("successMsg", "Xóa bản ghi thanh toán thành công.");
        } else {
            ra.addFlashAttribute("successMsg", "Khoản thu bắt buộc đã được đặt lại trạng thái Còn nợ.");
        }
        return "redirect:/thanh-toan";
    }
}
