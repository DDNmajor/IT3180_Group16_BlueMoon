package com.bluemoon.service;

import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.TrangThaiThanhToan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSchedulerService {

    private final ThanhToanRepository thanhToanRepository;
    private final EmailService        emailService;
    private final AuditLogService     auditLogService;

    // Thứ Hai hàng tuần 8:00 — nhắc tất cả hộ còn nợ
    @Scheduled(cron = "0 0 8 * * MON")
    public void nhacNoHangTuan() {
        log.info("[SCHEDULER] Bắt đầu gửi email nhắc nợ hàng tuần");
        guiNhacTheoNhom(
                thanhToanRepository.findByTrangThai(TrangThaiThanhToan.CON_NO),
                "Nhắc phí hàng tuần");
    }

    // Hàng ngày 8:00 — nhắc hộ có khoản sắp đến hạn (1–3 ngày)
    @Scheduled(cron = "0 0 8 * * *")
    public void nhacGanHan() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate plus3    = LocalDate.now().plusDays(3);
        List<ThanhToan> sapHan = thanhToanRepository
                .findByTrangThaiAndKhoanThuHanNopBetween(TrangThaiThanhToan.CON_NO, tomorrow, plus3);
        if (sapHan.isEmpty()) return;
        log.info("[SCHEDULER] Nhắc sắp hạn: {} khoản", sapHan.size());
        guiNhacTheoNhom(sapHan, "Phí sắp đến hạn nộp");
    }

    // Hàng ngày 9:00 — thông báo hộ có khoản vừa quá hạn hôm qua
    @Scheduled(cron = "0 0 9 * * *")
    public void nhacVuaQuaHan() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<ThanhToan> quaHan = thanhToanRepository
                .findByTrangThaiAndKhoanThuHanNop(TrangThaiThanhToan.CON_NO, yesterday);
        if (quaHan.isEmpty()) return;
        log.info("[SCHEDULER] Nhắc vừa quá hạn: {} khoản", quaHan.size());
        guiNhacTheoNhom(quaHan, "Thông báo phí đã quá hạn nộp");
    }

    private void guiNhacTheoNhom(List<ThanhToan> danhSach, String tieuDe) {
        Map<Integer, List<ThanhToan>> byHo = danhSach.stream()
                .filter(tt -> tt.getHoGiaDinh() != null
                           && tt.getHoGiaDinh().getEmail() != null
                           && !tt.getHoGiaDinh().getEmail().isBlank())
                .collect(Collectors.groupingBy(tt -> tt.getHoGiaDinh().getId()));

        byHo.forEach((idHo, list) -> {
            HoGiaDinh ho = list.get(0).getHoGiaDinh();
            emailService.guiEmailNhacNoAsync(
                    ho.getEmail(), ho.getSoCanHo(), ho.getChuHo(), list, tieuDe);
        });

        if (!byHo.isEmpty()) {
            log.info("[SCHEDULER] {}: gửi {} email", tieuDe, byHo.size());
            auditLogService.log(tieuDe, "Email",
                    "Gửi " + byHo.size() + " email tới các hộ còn nợ", "system");
        }
    }
}
