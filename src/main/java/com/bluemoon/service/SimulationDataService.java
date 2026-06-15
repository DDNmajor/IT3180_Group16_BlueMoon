package com.bluemoon.service;

import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.LoaiDichVuThuHo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class SimulationDataService {

    // Seed deterministic theo căn hộ + dịch vụ + kỳ để cùng kỳ gen ra cùng số
    public BigDecimal sinhSoTienMoPhong(HoGiaDinh ho, LoaiDichVuThuHo loai, int nam, int thang) {
        long seed = (long) ho.getId() * 1000 + loai.ordinal() * 100 + nam * 12L + thang;
        Random rng = new Random(seed);

        long min = loai.getSoTienMin();
        long max = loai.getSoTienMax();

        // Làm tròn đến 1000 đ
        long raw = min + (long)(rng.nextDouble() * (max - min));
        long rounded = (raw / 1000) * 1000;
        return BigDecimal.valueOf(rounded);
    }
}
