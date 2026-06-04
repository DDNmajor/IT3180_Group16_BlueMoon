package com.bluemoon.service;

import com.bluemoon.dao.BienDongRepository;
import com.bluemoon.model.BienDong;
import com.bluemoon.model.TinhTrangCuTru;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BienDongService {

    private final BienDongRepository bienDongRepository;
    private final NhanKhauService nhanKhauService;

    public List<BienDong> findByNhanKhau(Integer idNhanKhau) {
        return bienDongRepository.findByNhanKhauIdOrderByNgayBienDongDesc(idNhanKhau);
    }

    public List<BienDong> findByHoGiaDinh(Integer idHoGiaDinh) {
        return bienDongRepository.findByNhanKhauHoGiaDinhIdOrderByNgayBienDongDesc(idHoGiaDinh);
    }

    @Transactional
    public BienDong save(BienDong bienDong) {
        BienDong saved = bienDongRepository.save(bienDong);
        // Cập nhật tình trạng cư trú của nhân khẩu
        var nk = saved.getNhanKhau();
        switch (saved.getLoaiBienDong()) {
            case TAM_TRU    -> nk.setTinhTrang(TinhTrangCuTru.TAM_TRU);
            case TAM_VANG   -> nk.setTinhTrang(TinhTrangCuTru.TAM_VANG);
            case CHUYEN_DI  -> nk.setTinhTrang(TinhTrangCuTru.CHUYEN_DI);
            case CHUYEN_DEN -> nk.setTinhTrang(TinhTrangCuTru.THUONG_TRU);
        }
        nhanKhauService.saveRaw(nk);
        log.info("[AUDIT] Biến động: nhanKhau={}, loai={}, ngay={}, user={}",
                nk.getHoTen(), saved.getLoaiBienDong(), saved.getNgayBienDong(), currentUser());
        return saved;
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
