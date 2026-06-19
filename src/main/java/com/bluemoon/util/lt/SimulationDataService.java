package com.bluemoon.service;

import com.bluemoon.model.enums.LoaiDichVuThuHo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

/**
 * Service hỗ trợ sinh dữ liệu mô phỏng (Mock Data) cho phân hệ Thu hộ.
 * Sử dụng thuật toán Deterministic Random để đảm bảo tính nhất quán của dữ liệu.
 */
@Slf4j
@Service
public class SimulationDataService {

    /**
     * Sinh số tiền mô phỏng dựa trên Seed.
     * Seed được tạo bằng: Mã căn hộ + Mã dịch vụ + Tháng + Năm.
     * Đảm bảo rằng: Cùng 1 hộ, cùng 1 tháng, cùng 1 loại phí -> Lần nào gen lại cũng ra đúng 1 số tiền đó.
     */
    public BigDecimal sinhSoTienMoPhong(String soCanHo, LoaiDichVuThuHo dichVu, LocalDate kyThanhToan) {
        
        // 1. Tạo chuỗi định danh duy nhất (Seed String)
        String seedString = soCanHo + "_" + dichVu.name() + "_" + 
                            kyThanhToan.getMonthValue() + "_" + kyThanhToan.getYear();
                            
        // 2. Chuyển chuỗi thành mã Hash (Mã băm)
        long seed = seedString.hashCode();
        
        // 3. Khởi tạo đối tượng Random với Seed cố định
        Random random = new Random(seed);

        double min = dichVu.getSoTienMin();
        double max = dichVu.getSoTienMax();

        // 4. Sinh số tiền ngẫu nhiên trong khoảng Min - Max
        double randomAmount = min + (max - min) * random.nextDouble();

        // 5. Làm tròn số tiền (Ví dụ: điện nước thường làm tròn đến hàng nghìn)
        BigDecimal soTien = BigDecimal.valueOf(randomAmount);
        
        // Nếu là tiền điện/nước, làm tròn lên nghìn đồng
        if (dichVu == LoaiDichVuThuHo.DIEN || dichVu == LoaiDichVuThuHo.NUOC) {
            soTien = soTien.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(1000));
        } else {
            // Internet thường có giá cố định hơn, làm tròn chuẩn
            soTien = soTien.setScale(0, RoundingMode.HALF_UP);
        }

        return soTien;
    }
}