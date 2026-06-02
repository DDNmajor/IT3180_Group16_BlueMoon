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
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/thanh-toan")
@RequiredArgsConstructor
public class ThanhToanController {

    private final ThanhToanService   thanhToanService;
    private final HoGiaDinhService   hoGiaDinhService;
    private final KhoanThuService    khoanThuService;
    private final NguoiDungService   nguoiDungService;

    // ── UC006A / UC006B — Lịch sử thanh toán ─────────────────────

    @GetMapping
    public String list(@RequestParam(required = false) Integer idHo,
                       @RequestParam(required = false) Integer idKhoan,
                       @RequestParam(required = false) String  timKiem,
                       Model model) {

        List<ThanhToan> danhSach;

        if (idHo != null) {
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

        model.addAttribute("danhSach",     danhSach);
        model.addAttribute("danhSachHo",   hoGiaDinhService.findAll());
        model.addAttribute("danhSachKhoan", khoanThuService.findAll());
        return "thanh-toan/list";
    }

    // ── UC006 — Form ghi nhận thanh toán (GET) ───────────────────

    @GetMapping("/them")
    public String themForm(@RequestParam(required = false) Integer idHo,
                           @RequestParam(required = false) Integer idKhoan,
                           @RequestParam(required = false) String  timKiemHo,
                           Model model) {

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setNgayNop(LocalDate.now());
        thanhToan.setPhuongThuc(PhuongThucThanhToan.TIEN_MAT);

        // ── Tìm kiếm hộ gia đình (UC006 bước 2–3) ───────────────
        if (timKiemHo != null && !timKiemHo.isBlank()) {
            List<HoGiaDinh> ketQua = hoGiaDinhService.searchByCanHoOrChuHo(timKiemHo);
            model.addAttribute("timKiemHo",     timKiemHo);
            model.addAttribute("ketQuaTimKiem", ketQua);
            // Tự động chọn nếu kết quả duy nhất
            if (ketQua.size() == 1) {
                idHo = ketQua.get(0).getId();
            }
        }

        // ── Pre-select hộ, lọc khoản thu còn khả dụng ───────────
        if (idHo != null) {
            thanhToan.setHoGiaDinh(hoGiaDinhService.findById(idHo));

            // Loại bỏ khoản thu mà hộ đã DA_DONG hoặc DONG_DU
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

    // ── UC006 — Xử lý ghi nhận thanh toán (POST) ─────────────────

    @PostMapping("/them")
    public String them(@ModelAttribute ThanhToan thanhToan,
                       Authentication auth,
                       RedirectAttributes ra) {

        Integer idHo    = thanhToan.getHoGiaDinh() != null ? thanhToan.getHoGiaDinh().getId() : null;
        Integer idKhoan = thanhToan.getKhoanThu()  != null ? thanhToan.getKhoanThu().getId()  : null;

        // UC006-A: Hộ không tồn tại
        if (idHo == null) {
            ra.addFlashAttribute("errorMsg", "Vui lòng chọn hộ gia đình.");
            return "redirect:/thanh-toan/them";
        }
        // UC006-B: Khoản thu không tồn tại
        if (idKhoan == null) {
            ra.addFlashAttribute("errorMsg", "Vui lòng chọn khoản thu.");
            return "redirect:/thanh-toan/them?idHo=" + idHo;
        }
        // UC006-C: Số tiền không hợp lệ
        if (thanhToan.getSoTienDaNop() == null
                || thanhToan.getSoTienDaNop().compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("errorMsg", "Số tiền phải lớn hơn 0.");
            return "redirect:/thanh-toan/them?idHo=" + idHo + "&idKhoan=" + idKhoan;
        }
        // Ngày thanh toán không được lớn hơn ngày hiện tại
        if (thanhToan.getNgayNop() != null && thanhToan.getNgayNop().isAfter(LocalDate.now())) {
            ra.addFlashAttribute("errorMsg", "Ngày thanh toán không được lớn hơn ngày hiện tại.");
            return "redirect:/thanh-toan/them?idHo=" + idHo + "&idKhoan=" + idKhoan;
        }
        // UC006-D: Khoản thu đã được thanh toán (DA_DONG)
        if (thanhToanService.daDongHoanTat(idHo, idKhoan)) {
            ra.addFlashAttribute("errorMsg", "Hộ gia đình này đã hoàn thành thanh toán khoản thu đã chọn.");
            return "redirect:/thanh-toan/them?idHo=" + idHo;
        }

        // Resolve full objects (ModelAttribute chỉ bind id)
        thanhToan.setHoGiaDinh(hoGiaDinhService.findById(idHo));
        thanhToan.setKhoanThu(khoanThuService.findById(idKhoan));

        // Gán người thu = người đang đăng nhập
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

    // ── Nộp thêm (CON_NO) ────────────────────────────────────────

    @PostMapping("/nop-them/{id}")
    public String nopThem(@PathVariable Integer id,
                          @RequestParam BigDecimal soTienThem,
                          @RequestParam(required = false) Integer idHo,
                          RedirectAttributes ra) {
        if (soTienThem == null || soTienThem.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("errorMsg", "Số tiền nộp thêm phải lớn hơn 0.");
            return idHo != null ? "redirect:/thanh-toan?idHo=" + idHo : "redirect:/thanh-toan";
        }
        ThanhToan saved = thanhToanService.nopThem(id, soTienThem);
        ra.addFlashAttribute("successMsg",
                String.format("Đã cộng thêm %,.0f đ — Trạng thái mới: %s",
                        soTienThem.doubleValue(), saved.getTrangThai().getTenHienThi()));
        Integer redirectHo = idHo != null ? idHo
                : (saved.getHoGiaDinh() != null ? saved.getHoGiaDinh().getId() : null);
        return redirectHo != null ? "redirect:/thanh-toan?idHo=" + redirectHo : "redirect:/thanh-toan";
    }

    // ── Báo đã hoàn tiền (DONG_DU) ───────────────────────────────

    @PostMapping("/hoan-tien/{id}")
    public String baoDaHoanTien(@PathVariable Integer id,
                                @RequestParam(required = false) Integer idHo,
                                RedirectAttributes ra) {
        ThanhToan saved = thanhToanService.baoDaHoanTien(id);
        ra.addFlashAttribute("successMsg", "Đã ghi nhận hoàn trả tiền thừa — khoản thu chuyển sang Đã đóng.");
        Integer redirectHo = idHo != null ? idHo
                : (saved.getHoGiaDinh() != null ? saved.getHoGiaDinh().getId() : null);
        return redirectHo != null ? "redirect:/thanh-toan?idHo=" + redirectHo : "redirect:/thanh-toan";
    }

    // ── Xóa bản ghi ──────────────────────────────────────────────

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        thanhToanService.delete(id);
        ra.addFlashAttribute("successMsg", "Xóa bản ghi thanh toán thành công.");
        return "redirect:/thanh-toan";
    }
}
