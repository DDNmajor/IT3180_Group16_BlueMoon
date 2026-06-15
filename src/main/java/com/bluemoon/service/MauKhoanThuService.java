package com.bluemoon.service;

import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.LoaiKhoanThuRepository;
import com.bluemoon.dao.MauKhoanThuRepository;
import com.bluemoon.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MauKhoanThuService {

    private final MauKhoanThuRepository mauKhoanThuRepository;
    private final KhoanThuRepository    khoanThuRepository;
    private final KhoanThuService       khoanThuService;
    private final LoaiKhoanThuRepository loaiKhoanThuRepository;
    private final AuditLogService       auditLogService;

    public List<MauKhoanThu> findAll() {
        return mauKhoanThuRepository.findAll();
    }

    public MauKhoanThu findById(Integer id) {
        return mauKhoanThuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu khoản thu id=" + id));
    }

    public List<KhoanThu> findKhoanThuCuaMau(Integer mauId) {
        return khoanThuRepository.findByMauKhoanThuIdOrderByKyThuDesc(mauId);
    }

    @Transactional
    public MauKhoanThu save(MauKhoanThu mau) {
        if (mau.getLoaiKhoanThu() != null && mau.getLoaiKhoanThu().getId() != null) {
            LoaiKhoanThu loai = loaiKhoanThuRepository.findById(mau.getLoaiKhoanThu().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại khoản thu"));
            if (loai.getLoaiApDung() != LoaiApDung.BAT_BUOC_DINH_KY) {
                throw new IllegalArgumentException("Mẫu thu định kỳ chỉ áp dụng cho loại Bắt buộc - Định kỳ.");
            }
            mau.setLoaiKhoanThu(loai);
        }

        boolean isNew = (mau.getId() == null);
        if (isNew) {
            if (mauKhoanThuRepository.existsByMaMauPrefix(mau.getMaMauPrefix())) {
                throw new IllegalArgumentException(
                        "Tiền tố mã \"" + mau.getMaMauPrefix() + "\" đã tồn tại.");
            }
        } else {
            if (mauKhoanThuRepository.existsByMaMauPrefixAndIdNot(mau.getMaMauPrefix(), mau.getId())) {
                throw new IllegalArgumentException(
                        "Tiền tố mã \"" + mau.getMaMauPrefix() + "\" đã tồn tại.");
            }
        }

        MauKhoanThu saved = mauKhoanThuRepository.save(mau);
        String action = isNew ? "Tạo" : "Sửa";
        String user = currentUser();
        log.info("[AUDIT] {} mẫu khoản thu: id={}, tenMau={}, prefix={}, user={}",
                action, saved.getId(), saved.getTenMau(), saved.getMaMauPrefix(), user);
        auditLogService.log(action, "Mẫu khoản thu",
                "id=" + saved.getId() + ", tenMau=" + saved.getTenMau()
                + ", prefix=" + saved.getMaMauPrefix(), user);
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        MauKhoanThu mau = findById(id);
        // ON DELETE SET NULL — KhoanThu đã tạo vẫn giữ nguyên
        mauKhoanThuRepository.deleteById(id);
        String user = currentUser();
        log.info("[AUDIT] Xóa mẫu khoản thu: id={}, tenMau={}, user={}", id, mau.getTenMau(), user);
        auditLogService.log("Xóa", "Mẫu khoản thu",
                "id=" + id + ", tenMau=" + mau.getTenMau(), user);
    }

    @Transactional
    public boolean toggleActive(Integer id) {
        MauKhoanThu mau = findById(id);
        mau.setActive(!mau.isActive());
        mauKhoanThuRepository.save(mau);
        String trangThai = mau.isActive() ? "Kích hoạt" : "Tạm dừng";
        String user = currentUser();
        log.info("[AUDIT] {} mẫu khoản thu: id={}, tenMau={}, user={}", trangThai, id, mau.getTenMau(), user);
        auditLogService.log(trangThai, "Mẫu khoản thu",
                "id=" + id + ", tenMau=" + mau.getTenMau(), user);
        return mau.isActive();
    }

    @Transactional
    public KhoanThu taoKhoanThuChoKy(Integer mauId, YearMonth ym) {
        MauKhoanThu mau = findById(mauId);
        LocalDate kyThu = ym.atDay(1);
        if (khoanThuRepository.existsByMauKhoanThuIdAndKyThu(mauId, kyThu)) {
            throw new IllegalStateException(
                    "Kỳ " + ym.format(DateTimeFormatter.ofPattern("MM/yyyy"))
                    + " đã được tạo cho mẫu \"" + mau.getTenMau() + "\".");
        }
        KhoanThu kt = buildKhoanThu(mau, ym);
        KhoanThu saved = khoanThuService.save(kt);
        log.info("[AUDIT] Tạo thủ công kỳ {} từ mẫu: id={}, tenMau={}, user={}",
                ym, mauId, mau.getTenMau(), currentUser());
        return saved;
    }

    @Scheduled(cron = "0 0 8 28 * *")
    public void autoTaoThangTiepTheo() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        log.info("[AUTO] Bắt đầu tạo khoản thu định kỳ cho tháng {}", nextMonth);
        taoChoTatCaMauActive(nextMonth, "Auto-tạo");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void kiemTraKhiKhoiDong() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth nextMonth    = currentMonth.plusMonths(1);
        log.info("[AUTO] Startup check: kiểm tra kỳ {} và {}", currentMonth, nextMonth);

        taoChoTatCaMauActive(currentMonth, "Auto-bù khởi động");
        if (LocalDate.now().getDayOfMonth() >= 28) {
            taoChoTatCaMauActive(nextMonth, "Auto-bù khởi động");
        }
    }

    private void taoChoTatCaMauActive(YearMonth ym, String nguon) {
        List<MauKhoanThu> mauList = mauKhoanThuRepository.findByActiveTrue();
        int taoDuoc = 0;
        for (MauKhoanThu mau : mauList) {
            try {
                LocalDate kyThu = ym.atDay(1);
                if (khoanThuRepository.existsByMauKhoanThuIdAndKyThu(mau.getId(), kyThu)) {
                    continue; // already exists
                }
                KhoanThu kt = buildKhoanThu(mau, ym);
                khoanThuService.save(kt);
                taoDuoc++;
                log.info("[AUTO] {} — tạo kỳ {} từ mẫu '{}'", nguon, ym, mau.getTenMau());
            } catch (Exception e) {
                log.error("[AUTO] {} — lỗi tạo kỳ {} từ mẫu '{}': {}",
                        nguon, ym, mau.getTenMau(), e.getMessage());
            }
        }
        if (taoDuoc > 0) {
            log.info("[AUDIT] Auto-apply mẫu khoản thu: nguon={}, ky={}, soKhoan={}, user=system",
                    nguon, ym, taoDuoc);
            auditLogService.log("Auto-apply", "Mẫu khoản thu",
                    nguon + ": tạo " + taoDuoc + " khoản thu cho kỳ " + ym, "system");
        }
    }

    private KhoanThu buildKhoanThu(MauKhoanThu mau, YearMonth ym) {
        LocalDate kyThu = ym.atDay(1);
        KhoanThu kt = new KhoanThu();
        kt.setMaKhoanThu(mau.getMaMauPrefix() + "-" + ym.toString());
        kt.setTenKhoanThu(mau.getTenMau() + " "
                + ym.format(DateTimeFormatter.ofPattern("MM/yyyy")));
        kt.setLoaiKhoanThu(mau.getLoaiKhoanThu());
        kt.setLoaiTinhPhi(mau.getLoaiTinhPhi() != null ? mau.getLoaiTinhPhi() : com.bluemoon.model.LoaiTinhPhi.FIXED);
        kt.setSoTien(mau.getSoTien());
        kt.setDonGiaPerM2(mau.getDonGiaPerM2());
        kt.setGiaXeMay(mau.getGiaXeMay());
        kt.setGiaOto(mau.getGiaOto());
        kt.setDonVi(mau.getDonVi());
        kt.setKyThu(kyThu);
        kt.setMauKhoanThu(mau);
        if (mau.getSoNgayHanNop() != null) {
            kt.setHanNop(kyThu.plusDays(mau.getSoNgayHanNop()));
        }
        return kt;
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
