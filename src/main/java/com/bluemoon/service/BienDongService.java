package com.bluemoon.service;

import com.bluemoon.dao.BienDongRepository;
import com.bluemoon.model.BienDong;
import com.bluemoon.model.LoaiBienDong;
import com.bluemoon.model.TinhTrangCuTru;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BienDongService {

    private final BienDongRepository bienDongRepository;
    private final NhanKhauService    nhanKhauService;
    private final AuditLogService    auditLogService;

    public List<BienDong> findByNhanKhau(Integer idNhanKhau) {
        return bienDongRepository.findByNhanKhauIdOrderByNgayBienDongDesc(idNhanKhau);
    }

    public List<BienDong> findByHoGiaDinh(Integer idHoGiaDinh) {
        return bienDongRepository.findByNhanKhauHoGiaDinhIdOrderByNgayBienDongDesc(idHoGiaDinh);
    }

    @Transactional
    public BienDong save(BienDong bienDong) {
        BienDong saved = bienDongRepository.save(bienDong);
        var nk = saved.getNhanKhau();
        switch (saved.getLoaiBienDong()) {
            case TAM_TRU    -> nk.setTinhTrang(TinhTrangCuTru.TAM_TRU);
            case TAM_VANG   -> nk.setTinhTrang(TinhTrangCuTru.TAM_VANG);
            case CHUYEN_DI  -> nk.setTinhTrang(TinhTrangCuTru.CHUYEN_DI);
            case CHUYEN_DEN -> nk.setTinhTrang(TinhTrangCuTru.THUONG_TRU);
        }
        nhanKhauService.saveRaw(nk);
        String user = currentUser();
        log.info("[AUDIT] Biến động: nhanKhau={}, loai={}, ngay={}, user={}",
                nk.getHoTen(), saved.getLoaiBienDong(), saved.getNgayBienDong(), user);
        auditLogService.log("Biến động", "Nhân khẩu",
                "hoTen=" + nk.getHoTen() + ", loai=" + saved.getLoaiBienDong()
                + ", ngay=" + saved.getNgayBienDong()
                + (saved.getNgayKetThuc() != null ? ", ketThuc=" + saved.getNgayKetThuc() : ""), user);
        xuLyHetHan();
        return saved;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void xuLyHetHanKhoiDong() {
        xuLyHetHan();
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void xuLyHetHan() {
        LocalDate today = LocalDate.now();
        var expired = bienDongRepository.findByLoaiBienDongInAndNgayKetThucIsNotNullAndNgayKetThucBefore(
                List.of(LoaiBienDong.TAM_TRU, LoaiBienDong.TAM_VANG), today);
        for (BienDong bd : expired) {
            var nk = bd.getNhanKhau();
            if (bd.getLoaiBienDong() == LoaiBienDong.TAM_VANG && nk.getTinhTrang() == TinhTrangCuTru.TAM_VANG) {
                nk.setTinhTrang(TinhTrangCuTru.THUONG_TRU);
                nhanKhauService.saveRaw(nk);
                log.info("[AUDIT] Hết hạn TAM_VANG → THUONG_TRU: nhanKhau={}, ngayKetThuc={}, user=system",
                        nk.getHoTen(), bd.getNgayKetThuc());
                auditLogService.log("Hết hạn", "Nhân khẩu",
                        "hoTen=" + nk.getHoTen() + ", TAM_VANG → THUONG_TRU, ngayKetThuc=" + bd.getNgayKetThuc(),
                        "system");
            } else if (bd.getLoaiBienDong() == LoaiBienDong.TAM_TRU && nk.getTinhTrang() == TinhTrangCuTru.TAM_TRU) {
                nk.setTinhTrang(TinhTrangCuTru.CHUYEN_DI);
                nhanKhauService.saveRaw(nk);
                log.info("[AUDIT] Hết hạn TAM_TRU → CHUYEN_DI: nhanKhau={}, ngayKetThuc={}, user=system",
                        nk.getHoTen(), bd.getNgayKetThuc());
                auditLogService.log("Hết hạn", "Nhân khẩu",
                        "hoTen=" + nk.getHoTen() + ", TAM_TRU → CHUYEN_DI, ngayKetThuc=" + bd.getNgayKetThuc(),
                        "system");
            }
        }
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
