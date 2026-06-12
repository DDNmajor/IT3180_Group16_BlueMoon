package com.bluemoon.controller;

import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.service.BienDongService;
import com.bluemoon.service.HoGiaDinhService;
import com.bluemoon.service.KhoanThuService;
import com.bluemoon.service.PhuongTienService;
import com.bluemoon.service.ThanhToanService;

import java.math.BigDecimal;
import java.util.Objects;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.bluemoon.service.ExcelImportService;

@Controller
@RequestMapping("/ho-gia-dinh")
@RequiredArgsConstructor
public class HoGiaDinhController {

    private final HoGiaDinhService  hoGiaDinhService;
    private final ThanhToanService  thanhToanService;
    private final BienDongService   bienDongService;
    private final PhuongTienService phuongTienService;
    private final KhoanThuService   khoanThuService;
    private final ExcelImportService excelImportService;

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "Vui lòng chọn file Excel để import.");
            return "redirect:/ho-gia-dinh";
        }
        String result = excelImportService.importHoGiaDinh(file);
        if (result.contains("Lỗi đọc file Excel")) {
            ra.addFlashAttribute("errorMsg", result);
        } else {
            ra.addFlashAttribute("infoMsg", result.replace("\n", "<br>")); // Hiển thị xuống dòng trên giao diện
        }
        return "redirect:/ho-gia-dinh";
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            var danhSach = hoGiaDinhService.search(search);
            model.addAttribute("danhSach", danhSach);
            model.addAttribute("search", search);
            if (danhSach.isEmpty()) {
                model.addAttribute("emptyMsg", "Không tìm thấy kết quả cho \"" + search + "\".");
            }
        } else {
            model.addAttribute("danhSach", hoGiaDinhService.findAll());
        }
        return "ho-gia-dinh/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            HoGiaDinh ho = hoGiaDinhService.findById(id);
            model.addAttribute("hoGiaDinh",          ho);
            model.addAttribute("lichSuThanhToan",    thanhToanService.findByHoGiaDinh(id));
            model.addAttribute("lichSuBienDong",     bienDongService.findByHoGiaDinh(id));
            model.addAttribute("danhSachPhuongTien", phuongTienService.findByHoGiaDinh(id));
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy hộ gia đình.");
            return "redirect:/ho-gia-dinh";
        }
        return "ho-gia-dinh/detail";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("hoGiaDinh", new HoGiaDinh());
        return "ho-gia-dinh/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("hoGiaDinh") HoGiaDinh hoGiaDinh,
                       BindingResult bindingResult, RedirectAttributes ra) {

        if (!bindingResult.hasFieldErrors("soCanHo")
                && hoGiaDinhService.existsBySoCanHo(hoGiaDinh.getSoCanHo())) {
            bindingResult.rejectValue("soCanHo", "duplicate", "Số căn hộ đã tồn tại");
        }
        if (bindingResult.hasErrors()) return "ho-gia-dinh/form";

        hoGiaDinhService.save(hoGiaDinh);
        ra.addFlashAttribute("successMsg", "Thêm hộ gia đình thành công.");
        return "redirect:/ho-gia-dinh";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("hoGiaDinh", hoGiaDinhService.findById(id));
            return "ho-gia-dinh/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy hộ gia đình.");
            return "redirect:/ho-gia-dinh";
        }
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Integer id,
                      @Valid @ModelAttribute("hoGiaDinh") HoGiaDinh hoGiaDinh,
                      BindingResult bindingResult, RedirectAttributes ra) {

        if (!bindingResult.hasFieldErrors("soCanHo")
                && hoGiaDinhService.existsBySoCanHoForOther(hoGiaDinh.getSoCanHo(), id)) {
            bindingResult.rejectValue("soCanHo", "duplicate", "Số căn hộ đã tồn tại");
        }
        if (bindingResult.hasErrors()) return "ho-gia-dinh/form";

        BigDecimal dienTichCu = hoGiaDinhService.findById(id).getDienTich();
        hoGiaDinh.setId(id);
        hoGiaDinhService.save(hoGiaDinh);

        if (!Objects.equals(dienTichCu, hoGiaDinh.getDienTich())) {
            int flagged = khoanThuService.recalculatePerM2ForHo(hoGiaDinh, dienTichCu);
            if (flagged > 0) {
                ra.addFlashAttribute("warnMsg",
                        "Đã cập nhật hộ gia đình và điều chỉnh khoản phí m². "
                        + flagged + " khoản đang nộp dở cần xem lại trong lịch sử thanh toán.");
            } else {
                ra.addFlashAttribute("successMsg",
                        "Đã cập nhật hộ gia đình và điều chỉnh khoản phí theo diện tích mới.");
            }
        } else {
            ra.addFlashAttribute("successMsg", "Cập nhật hộ gia đình thành công.");
        }
        return "redirect:/ho-gia-dinh/" + id;
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            hoGiaDinhService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa hộ gia đình thành công.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/ho-gia-dinh";
    }
}
