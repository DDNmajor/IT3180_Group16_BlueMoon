package com.bluemoon.service;

import com.bluemoon.dao.LichSuEmailRepository;
import com.bluemoon.model.LichSuEmail;
import com.bluemoon.model.LoaiEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LichSuEmailService {

    private static final int MAX_RECORDS = 1000;

    private final LichSuEmailRepository lichSuEmailRepository;

    @Transactional
    public void ghiLichSu(String toEmail, String subject, String body,
                          LoaiEmail loaiEmail, boolean thanhCong,
                          String errorMessage, String soCanHo, String nguoiGui) {
        LichSuEmail ls = new LichSuEmail();
        ls.setToEmail(toEmail);
        ls.setSubject(subject);
        ls.setBody(body);
        ls.setLoaiEmail(loaiEmail);
        ls.setTrangThai(thanhCong ? "THANH_CONG" : "THAT_BAI");
        ls.setErrorMessage(errorMessage);
        ls.setSoCanHo(soCanHo);
        ls.setNguoiGui(nguoiGui);
        ls.setNgayGui(LocalDateTime.now());
        lichSuEmailRepository.save(ls);
    }

    public List<LichSuEmail> findWithFilter(String toEmail, String soCanHo,
                                            LoaiEmail loaiEmail, String trangThai,
                                            LocalDate tuNgay, LocalDate denNgay) {
        List<LichSuEmail> all = lichSuEmailRepository.findAllByOrderByNgayGuiDesc(
                PageRequest.of(0, MAX_RECORDS, Sort.by(Sort.Direction.DESC, "ngayGui")));
        return all.stream()
                .filter(e -> toEmail == null || toEmail.isBlank()
                        || e.getToEmail() != null && e.getToEmail().toLowerCase().contains(toEmail.toLowerCase()))
                .filter(e -> soCanHo == null || soCanHo.isBlank()
                        || soCanHo.equalsIgnoreCase(e.getSoCanHo()))
                .filter(e -> loaiEmail == null || loaiEmail == e.getLoaiEmail())
                .filter(e -> trangThai == null || trangThai.isBlank()
                        || trangThai.equals(e.getTrangThai()))
                .filter(e -> tuNgay == null || e.getNgayGui() != null
                        && !e.getNgayGui().toLocalDate().isBefore(tuNgay))
                .filter(e -> denNgay == null || e.getNgayGui() != null
                        && !e.getNgayGui().toLocalDate().isAfter(denNgay))
                .collect(Collectors.toList());
    }
}
