package com.bluemoon.service;

import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.KhoanThu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AuditLogService auditLogService;

    @Value("${bluemoon.mail.from}")
    private String mailFrom;

    @Async
    public void guiThongBaoKhoanThu(HoGiaDinh ho, KhoanThu khoanThu, BigDecimal soTienYeuCau) {
        if (ho.getEmail() == null || ho.getEmail().isBlank()) {
            return;
        }

        BigDecimal soTien = soTienYeuCau != null ? soTienYeuCau : khoanThu.getSoTien();
        String soTienFormatted = NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                .format(soTien != null ? soTien : BigDecimal.ZERO);

        String hanNopFormatted = (khoanThu.getHanNop() != null)
                ? khoanThu.getHanNop().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Không giới hạn";

        String body = String.format(
                "Kính gửi chủ hộ %s - Căn hộ %s,%n%n"
                + "Ban quản trị chung cư BlueMoon xin thông báo khoản thu mới:%n%n"
                + "  Tên khoản thu : %s%n"
                + "  Mã khoản thu  : %s%n"
                + "  Số tiền       : %s đ%n"
                + "  Hạn nộp       : %s%n%n"
                + "Vui lòng nộp phí đúng hạn tại văn phòng Ban quản trị hoặc chuyển khoản qua tài khoản ngân hàng của Ban quản lý.%n%n"
                + "Trân trọng,%n"
                + "Ban quản trị chung cư BlueMoon",
                ho.getChuHo(), ho.getSoCanHo(),
                khoanThu.getTenKhoanThu(),
                khoanThu.getMaKhoanThu(),
                soTienFormatted,
                hanNopFormatted
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(ho.getEmail());
            message.setSubject("[BlueMoon] Thông báo khoản thu - " + khoanThu.getTenKhoanThu());
            message.setText(body);
            mailSender.send(message);

            log.info("[AUDIT] Gửi email thông báo khoản thu: email={}, canHo={}, khoanThu={}",
                    ho.getEmail(), ho.getSoCanHo(), khoanThu.getMaKhoanThu());
            auditLogService.log("Gửi email", "HoGiaDinh",
                    "email=" + ho.getEmail() + ", canHo=" + ho.getSoCanHo()
                    + ", khoanThu=" + khoanThu.getMaKhoanThu(), "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email thất bại: email={}, canHo={}, khoanThu={}, loi={}",
                    ho.getEmail(), ho.getSoCanHo(), khoanThu.getMaKhoanThu(), e.getMessage());
        }
    }
}
