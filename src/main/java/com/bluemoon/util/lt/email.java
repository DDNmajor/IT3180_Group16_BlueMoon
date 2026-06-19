package com.bluemoon.service;

import com.bluemoon.model.HoaDonThuHo;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.KhoanThu;
import com.bluemoon.model.ThanhToan;
import com.bluemoon.model.enums.LoaiEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tích hợp API gửi thư điện tử thông qua Brevo (Sendinblue) REST API.
 * Các phương thức gửi mail số lượng lớn được bọc @Async để chạy trên Thread Pool riêng,
 * tránh block luồng xử lý chính của người dùng (Non-blocking I/O).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${bluemoon.brevo.api-key}")
    private String apiKey;

    @Value("${bluemoon.mail.from}")
    private String fromEmail;

    private final RestClient restClient;
    private final LichSuEmailService lichSuEmailService;

    /**
     * Hàm lõi gọi REST API của Brevo.
     * Sử dụng cấu trúc JSON chuẩn của Transactional Email API.
     */
    private void sendViaBrevoAPI(String to, String subject, String htmlContent, LoaiEmail loai, String soCanHo) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("name", "Ban Quản Trị BlueMoon", "email", fromEmail));
            payload.put("to", List.of(Map.of("email", to)));
            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);

            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            lichSuEmailService.ghiLichSu(to, subject, htmlContent, loai, "THANH_CONG", null, soCanHo);
            log.info("[EMAIL - SUCCESS] Đã gửi thư [{}] tới {}", loai.name(), to);

        } catch (Exception e) {
            log.error("[EMAIL - FAILED] Không thể gửi thư tới {}: {}", to, e.getMessage());
            lichSuEmailService.ghiLichSu(to, subject, htmlContent, loai, "THAT_BAI", e.getMessage(), soCanHo);
            throw new RuntimeException("Lỗi gửi email API: " + e.getMessage());
        }
    }

    @Async
    public void guiThongBaoKhoanThu(HoGiaDinh ho, KhoanThu kt, BigDecimal soTienYeuCau) {
        if (ho.getEmail() == null || ho.getEmail().isBlank()) return;

        String subject = "[BlueMoon] Thông báo phí mới: " + kt.getTenKhoanThu();
        String html = String.format(
            "<h2>Xin chào cư dân căn hộ %s,</h2>" +
            "<p>Ban quản trị thông báo khoản phí mới được áp dụng:</p>" +
            "<ul><li><b>Khoản thu:</b> %s</li><li><b>Kỳ thu:</b> %s/%s</li>" +
            "<li><b>Số tiền:</b> %,.0f VNĐ</li><li><b>Hạn nộp:</b> %s</li></ul>" +
            "<p>Vui lòng đóng phí đúng hạn. Trân trọng!</p>",
            ho.getSoCanHo(), kt.getTenKhoanThu(), 
            kt.getKyThu().getMonthValue(), kt.getKyThu().getYear(),
            soTienYeuCau, kt.getHanNop()
        );
        sendViaBrevoAPI(ho.getEmail(), subject, html, LoaiEmail.THONG_BAO_KHOAN_THU, ho.getSoCanHo());
    }

    @Async
    public void guiEmailNhacNoAsync(String toEmail, String soCanHo, String chuHo, 
                                    List<ThanhToan> danhSachNo, String tieuDe) {
        if (toEmail == null || toEmail.isBlank()) return;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<h2>Kính gửi chủ hộ %s (Căn hộ %s),</h2>", chuHo, soCanHo));
        sb.append("<p>Hệ thống ghi nhận căn hộ đang có các khoản nợ phí sau:</p><ul>");
        
        BigDecimal tongNo = BigDecimal.ZERO;
        for (ThanhToan tt : danhSachNo) {
            BigDecimal conThieu = tt.getSoTienYeuCauHieuLuc().subtract(
                    tt.getSoTienDaNop() != null ? tt.getSoTienDaNop() : BigDecimal.ZERO);
            tongNo = tongNo.add(conThieu);
            sb.append(String.format("<li><b>%s</b>: %,.0f VNĐ (Hạn: %s)</li>", 
                      tt.getKhoanThu().getTenKhoanThu(), conThieu, tt.getKhoanThu().getHanNop()));
        }
        sb.append("</ul>");
        sb.append(String.format("<h3>Tổng số tiền cần thanh toán: %,.0f VNĐ</h3>", tongNo));

        sendViaBrevoAPI(toEmail, tieuDe, sb.toString(), LoaiEmail.NHAC_NO_TU_DONG, soCanHo);
    }
}