package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.model.HoGiaDinh;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoGiaDinhService {

    private final HoGiaDinhRepository hoGiaDinhRepository;

    public List<HoGiaDinh> findAll() {
        return hoGiaDinhRepository.findAll();
    }

    public HoGiaDinh findById(Integer id) {
        return hoGiaDinhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hộ gia đình id=" + id));
    }

    public HoGiaDinh findBySoCanHo(String soCanHo) {
        return hoGiaDinhRepository.findBySoCanHo(soCanHo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy căn hộ: " + soCanHo));
    }

    public List<HoGiaDinh> search(String keyword) {
        return hoGiaDinhRepository.findByChuHoContainingIgnoreCase(keyword);
    }

    public List<HoGiaDinh> searchByCanHoOrChuHo(String keyword) {
        return hoGiaDinhRepository.findBySoCanHoContainingIgnoreCaseOrChuHoContainingIgnoreCase(keyword, keyword);
    }

    public boolean existsBySoCanHo(String soCanHo) {
        return hoGiaDinhRepository.existsBySoCanHo(soCanHo);
    }

    @Transactional
    public HoGiaDinh save(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh.getId() == null) {
            hoGiaDinh.setNgayTao(LocalDateTime.now());
        }
        return hoGiaDinhRepository.save(hoGiaDinh);
    }

    @Transactional
    public void delete(Integer id) {
        hoGiaDinhRepository.deleteById(id);
    }
}
