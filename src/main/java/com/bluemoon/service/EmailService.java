package com.bluemoon.service;

import com.bluemoon.model.HoaDonThuHo;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.KhoanThu;
import com.bluemoon.model.LoaiEmail;
import com.bluemoon.model.ThanhToan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender     mailSender;
    private final AuditLogService    auditLogService;
    private final LichSuEmailService lichSuEmailService;

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

        String subject = "[BlueMoon] Thông báo khoản thu - " + khoanThu.getTenKhoanThu();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(ho.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("[AUDIT] Gửi email thông báo khoản thu: email={}, canHo={}, khoanThu={}",
                    ho.getEmail(), ho.getSoCanHo(), khoanThu.getMaKhoanThu());
            auditLogService.log("Gửi email", "HoGiaDinh",
                    "email=" + ho.getEmail() + ", canHo=" + ho.getSoCanHo()
                    + ", khoanThu=" + khoanThu.getMaKhoanThu(), "system");
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, body,
                    LoaiEmail.THONG_BAO_KHOAN_THU, true, null, ho.getSoCanHo(), "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email thất bại: email={}, canHo={}, khoanThu={}, loi={}",
                    ho.getEmail(), ho.getSoCanHo(), khoanThu.getMaKhoanThu(), e.getMessage());
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, body,
                    LoaiEmail.THONG_BAO_KHOAN_THU, false, e.getMessage(), ho.getSoCanHo(), "system");
        }
    }

    // Gửi email thông báo khoản thu mới + liệt kê toàn bộ CON_NO hiện tại của hộ
    @Async
    public void guiThongBaoKhoanThuTongHop(HoGiaDinh ho, KhoanThu khoanThuMoi,
                                            BigDecimal soTienYeuCau,
                                            List<ThanhToan> tatCaConNo) {
        if (ho.getEmail() == null || ho.getEmail().isBlank()) return;

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        BigDecimal soTien = soTienYeuCau != null ? soTienYeuCau : khoanThuMoi.getSoTien();
        String hanNopStr = khoanThuMoi.getHanNop() != null
                ? khoanThuMoi.getHanNop().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "Không giới hạn";

        StringBuilder body = new StringBuilder(String.format(
                "Kính gửi chủ hộ %s - Căn hộ %s,%n%n"
                + "Ban quản trị chung cư BlueMoon xin thông báo khoản thu mới:%n%n"
                + "  Tên khoản thu : %s%n"
                + "  Mã khoản thu  : %s%n"
                + "  Số tiền       : %s đ%n"
                + "  Hạn nộp       : %s%n",
                ho.getChuHo(), ho.getSoCanHo(),
                khoanThuMoi.getTenKhoanThu(), khoanThuMoi.getMaKhoanThu(),
                fmt.format(soTien != null ? soTien : BigDecimal.ZERO), hanNopStr));

        List<ThanhToan> khac = tatCaConNo.stream()
                .filter(tt -> !tt.getKhoanThu().getId().equals(khoanThuMoi.getId()))
                .collect(Collectors.toList());
        if (!khac.isEmpty()) {
            body.append("\nNgoài ra, hộ còn các khoản phí chưa hoàn tất:\n");
            for (ThanhToan tt : khac) {
                BigDecimal con = tt.getSoTienYeuCauHieuLuc().subtract(tt.getSoTienDaNop());
                body.append(String.format("  - %s: còn thiếu %s đ%n",
                        tt.getKhoanThu().getTenKhoanThu(), fmt.format(con)));
            }
        }

        body.append("\nVui lòng nộp phí đúng hạn tại văn phòng Ban quản trị.\n\n")
            .append("Trân trọng,\nBan quản trị chung cư BlueMoon");

        String subject = "[BlueMoon] Thông báo khoản thu - " + khoanThuMoi.getTenKhoanThu();
        String bodyStr = body.toString();
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(ho.getEmail());
            msg.setSubject(subject);
            msg.setText(bodyStr);
            mailSender.send(msg);
            log.info("[AUDIT] Gửi email thông báo khoản thu tổng hợp: email={}, canHo={}, khoanThu={}",
                    ho.getEmail(), ho.getSoCanHo(), khoanThuMoi.getMaKhoanThu());
            auditLogService.log("Gửi email", "HoGiaDinh",
                    "email=" + ho.getEmail() + ", canHo=" + ho.getSoCanHo()
                    + ", khoanThu=" + khoanThuMoi.getMaKhoanThu(), "system");
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, bodyStr,
                    LoaiEmail.THONG_BAO_KHOAN_THU, true, null, ho.getSoCanHo(), "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email tổng hợp thất bại: email={}, canHo={}, loi={}",
                    ho.getEmail(), ho.getSoCanHo(), e.getMessage());
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, bodyStr,
                    LoaiEmail.THONG_BAO_KHOAN_THU, false, e.getMessage(), ho.getSoCanHo(), "system");
        }
    }

    // Chào mừng hộ gia đình mới — liệt kê tất cả khoản phí được áp dụng
    @Async
    public void guiEmailChaoMungHoMoi(HoGiaDinh ho, List<KhoanThu> danhSachKhoan) {
        if (ho.getEmail() == null || ho.getEmail().isBlank()) return;

        StringBuilder body = new StringBuilder();
        body.append(String.format("Kính gửi chủ hộ %s - Căn hộ %s,%n%n", ho.getChuHo(), ho.getSoCanHo()));
        body.append("Chào mừng hộ gia đình đến với chung cư BlueMoon!\n");
        body.append("Hộ đã được ghi nhận vào hệ thống với các khoản phí bắt buộc sau:\n\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (KhoanThu kt : danhSachKhoan) {
            body.append(String.format("  - %s (Mã: %s)", kt.getTenKhoanThu(), kt.getMaKhoanThu()));
            if (kt.getHanNop() != null) body.append(", hạn nộp: ").append(kt.getHanNop().format(dtf));
            body.append("\n");
        }

        body.append("\nVui lòng liên hệ văn phòng Ban quản lý để biết thêm chi tiết thanh toán.\n\n")
            .append("Trân trọng,\nBan quản trị chung cư BlueMoon");

        String subject = "[BlueMoon] Chào mừng hộ gia đình - Căn hộ " + ho.getSoCanHo();
        String bodyStr = body.toString();
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(ho.getEmail());
            msg.setSubject(subject);
            msg.setText(bodyStr);
            mailSender.send(msg);
            log.info("[AUDIT] Gửi email chào mừng hộ mới: email={}, canHo={}", ho.getEmail(), ho.getSoCanHo());
            auditLogService.log("Gửi email chào mừng", "HoGiaDinh",
                    "email=" + ho.getEmail() + ", canHo=" + ho.getSoCanHo(), "system");
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, bodyStr,
                    LoaiEmail.CHAO_MUNG_HO_MOI, true, null, ho.getSoCanHo(), "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email chào mừng thất bại: email={}, canHo={}, loi={}",
                    ho.getEmail(), ho.getSoCanHo(), e.getMessage());
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, bodyStr,
                    LoaiEmail.CHAO_MUNG_HO_MOI, false, e.getMessage(), ho.getSoCanHo(), "system");
        }
    }

    // Nhắc nợ tự động từ scheduler — @Async vì gọi hàng loạt
    @Async
    public void guiEmailNhacNoAsync(String toEmail, String soCanHo, String chuHo,
                                     List<ThanhToan> danhSachNo, String tieuDe) {
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder body = new StringBuilder();
        body.append(String.format("Kính gửi chủ hộ %s - Căn hộ %s,%n%n", chuHo, soCanHo));
        body.append("Ban quản lý BlueMoon xin nhắc nhở các khoản phí chưa hoàn tất:\n\n");

        for (ThanhToan tt : danhSachNo) {
            BigDecimal con = tt.getSoTienYeuCauHieuLuc().subtract(tt.getSoTienDaNop());
            body.append(String.format("  - %s: còn thiếu %s đ",
                    tt.getKhoanThu().getTenKhoanThu(), fmt.format(con)));
            if (tt.getKhoanThu().getHanNop() != null) {
                body.append(", hạn nộp ").append(tt.getKhoanThu().getHanNop().format(dtf));
                if (tt.getKhoanThu().getHanNop().isBefore(LocalDate.now())) {
                    body.append(" (ĐÃ QUÁ HẠN)");
                }
            }
            body.append("\n");
        }

        body.append("\nVui lòng hoàn tất thanh toán sớm nhất.\n\n")
            .append("Trân trọng,\nBan quản lý BlueMoon");

        String subject = "[BlueMoon] " + tieuDe + " - Căn hộ " + soCanHo;
        String bodyStr = body.toString();
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(bodyStr);
            mailSender.send(msg);
            log.info("[AUDIT] Gửi email nhắc nợ auto: email={}, canHo={}, soKhoan={}",
                    toEmail, soCanHo, danhSachNo.size());
            auditLogService.log("Gửi email auto", "Email",
                    "email=" + toEmail + ", canHo=" + soCanHo + ", tieuDe=" + tieuDe, "system");
            lichSuEmailService.ghiLichSu(toEmail, subject, bodyStr,
                    LoaiEmail.NHAC_NO_TU_DONG, true, null, soCanHo, "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email auto thất bại: email={}, canHo={}, loi={}",
                    toEmail, soCanHo, e.getMessage());
            lichSuEmailService.ghiLichSu(toEmail, subject, bodyStr,
                    LoaiEmail.NHAC_NO_TU_DONG, false, e.getMessage(), soCanHo, "system");
        }
    }

    // ── THU HỘ ──────────────────────────────────────────────────────────────

    @Async
    public void guiEmailThuHoThongBao(HoaDonThuHo hd) {
        HoGiaDinh ho = hd.getHoGiaDinh();
        if (ho.getEmail() == null || ho.getEmail().isBlank()) return;

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        DateTimeFormatter dtfMonth = DateTimeFormatter.ofPattern("MM/yyyy");
        DateTimeFormatter dtfDate  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String kyStr  = hd.getKyThanhToan().format(dtfMonth);
        String hanStr = hd.getHanThanhToan() != null
                ? hd.getHanThanhToan().format(dtfDate) : "Không giới hạn";

        String body = String.format(
                "Kính gửi chủ hộ %s - Căn hộ %s,%n%n"
                + "Ban quản trị chung cư BlueMoon thông báo hóa đơn thu hộ từ %s:%n%n"
                + "  Mã hóa đơn   : %s%n"
                + "  Dịch vụ      : %s%n"
                + "  Kỳ thanh toán: %s%n"
                + "  Số tiền      : %s đ%n"
                + "  Hạn thanh toán: %s%n%n"
                + "Vui lòng nộp tiền trực tiếp tại văn phòng Ban quản lý. Ban quản lý sẽ xác nhận thanh toán và gửi email biên nhận sau khi nhận tiền.%n%n"
                + "Trân trọng,%n"
                + "Ban quản trị chung cư BlueMoon",
                ho.getChuHo(), ho.getSoCanHo(),
                hd.getLoaiDichVu().getNhaCungCap(),
                hd.getMaHoaDon(),
                hd.getLoaiDichVu().getTenHienThi(),
                kyStr,
                fmt.format(hd.getSoTien()),
                hanStr
        );

        String subject = "[BlueMoon] Hóa đơn thu hộ " + hd.getLoaiDichVu().getTenHienThi()
                + " tháng " + kyStr + " - Căn hộ " + ho.getSoCanHo();
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(ho.getEmail());
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[AUDIT] Gửi email thu hộ thông báo: email={}, canHo={}, ma={}",
                    ho.getEmail(), ho.getSoCanHo(), hd.getMaHoaDon());
            auditLogService.log("Gửi email thu hộ", "HoaDonThuHo",
                    "ma=" + hd.getMaHoaDon() + ", canHo=" + ho.getSoCanHo(), "system");
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, body,
                    LoaiEmail.THU_HO_THONG_BAO, true, null, ho.getSoCanHo(), "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email thu hộ thông báo thất bại: email={}, ma={}, loi={}",
                    ho.getEmail(), hd.getMaHoaDon(), e.getMessage());
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, body,
                    LoaiEmail.THU_HO_THONG_BAO, false, e.getMessage(), ho.getSoCanHo(), "system");
        }
    }

    @Async
    public void guiEmailThuHoXacNhan(HoaDonThuHo hd) {
        HoGiaDinh ho = hd.getHoGiaDinh();
        if (ho.getEmail() == null || ho.getEmail().isBlank()) return;

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        DateTimeFormatter dtfMonth = DateTimeFormatter.ofPattern("MM/yyyy");
        DateTimeFormatter dtfDate  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String kyStr      = hd.getKyThanhToan().format(dtfMonth);
        String xacNhanStr = hd.getNgayXacNhan() != null
                ? hd.getNgayXacNhan().format(dtfDate) : "—";
        String nguoiXnStr = hd.getNguoiXacNhan() != null
                ? hd.getNguoiXacNhan().getTenDangNhap() : "BQL";

        String body = String.format(
                "Kính gửi chủ hộ %s - Căn hộ %s,%n%n"
                + "Ban quản trị chung cư BlueMoon xác nhận đã nhận thanh toán hóa đơn thu hộ:%n%n"
                + "  Mã hóa đơn   : %s%n"
                + "  Dịch vụ      : %s (%s)%n"
                + "  Kỳ thanh toán: %s%n"
                + "  Số tiền      : %s đ%n"
                + "  Thời gian XN : %s%n"
                + "  Nhân viên XN : %s%n%n"
                + "Cảm ơn bạn đã thanh toán đúng hạn.%n%n"
                + "Trân trọng,%n"
                + "Ban quản trị chung cư BlueMoon",
                ho.getChuHo(), ho.getSoCanHo(),
                hd.getMaHoaDon(),
                hd.getLoaiDichVu().getTenHienThi(), hd.getLoaiDichVu().getNhaCungCap(),
                kyStr,
                fmt.format(hd.getSoTien()),
                xacNhanStr, nguoiXnStr
        );

        String subject = "[BlueMoon] Xác nhận thanh toán - " + hd.getMaHoaDon();
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(ho.getEmail());
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[AUDIT] Gửi email thu hộ xác nhận: email={}, canHo={}, ma={}",
                    ho.getEmail(), ho.getSoCanHo(), hd.getMaHoaDon());
            auditLogService.log("Gửi email thu hộ", "HoaDonThuHo",
                    "ma=" + hd.getMaHoaDon() + ", canHo=" + ho.getSoCanHo() + " (xác nhận)", "system");
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, body,
                    LoaiEmail.THU_HO_XAC_NHAN, true, null, ho.getSoCanHo(), "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email thu hộ xác nhận thất bại: email={}, ma={}, loi={}",
                    ho.getEmail(), hd.getMaHoaDon(), e.getMessage());
            lichSuEmailService.ghiLichSu(ho.getEmail(), subject, body,
                    LoaiEmail.THU_HO_XAC_NHAN, false, e.getMessage(), ho.getSoCanHo(), "system");
        }
    }

    // Nhắc nợ thủ công từ web (đồng bộ — cần biết kết quả ngay)
    public void guiEmailNhacNo(String toEmail, String soCanHo, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("[AUDIT] Gửi email nhắc nợ: email={}, canHo={}", toEmail, soCanHo);
            auditLogService.log("Gửi email nhắc nợ", "HoGiaDinh",
                    "email=" + toEmail + ", canHo=" + soCanHo, "system");
            lichSuEmailService.ghiLichSu(toEmail, subject, body,
                    LoaiEmail.NHAC_NO_THU_CONG, true, null, soCanHo, "system");
        } catch (Exception e) {
            log.warn("[MAIL] Gửi email nhắc nợ thất bại: email={}, canHo={}, loi={}",
                    toEmail, soCanHo, e.getMessage());
            lichSuEmailService.ghiLichSu(toEmail, subject, body,
                    LoaiEmail.NHAC_NO_THU_CONG, false, e.getMessage(), soCanHo, "system");
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }
}
