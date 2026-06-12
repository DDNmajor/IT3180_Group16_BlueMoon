package com.bluemoon.service;

import com.bluemoon.dao.HoGiaDinhRepository;
import com.bluemoon.dao.KhoanThuRepository;
import com.bluemoon.dao.ThanhToanRepository;
import com.bluemoon.dto.NoPhiChiTietDto;
import com.bluemoon.dto.NoPhiHoDto;
import com.bluemoon.dto.ThongKeKhoanThuDto;
import com.bluemoon.model.HoGiaDinh;
import com.bluemoon.model.KhoanThu;
import com.bluemoon.model.LoaiKhoanThu;
import com.bluemoon.model.ThanhToan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaoCaoThanhToanService {

    private final ThanhToanRepository thanhToanRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final KhoanThuRepository khoanThuRepository;

    public List<NoPhiHoDto> getBangNoPhi(String keyword, Integer idKhoanThu, BigDecimal noTren) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        BigDecimal minNo = noTren == null ? BigDecimal.ZERO : noTren;

        List<ThanhToan> tatCaThanhToan = thanhToanRepository.findAll();

        Map<Integer, LocalDate> ngayGanNhatTheoHo = new HashMap<>();

        for (ThanhToan t : tatCaThanhToan) {
            if (t.getHoGiaDinh() == null || t.getNgayNop() == null) {
                continue;
            }

            Integer idHo = t.getHoGiaDinh().getId();
            LocalDate ngay = toLocalDate(t.getNgayNop());

            if (ngay != null) {
                LocalDate current = ngayGanNhatTheoHo.get(idHo);
                if (current == null || ngay.isAfter(current)) {
                    ngayGanNhatTheoHo.put(idHo, ngay);
                }
            }
        }

        Map<Integer, NoPhiHoDto> result = new LinkedHashMap<>();

        for (ThanhToan tt : tatCaThanhToan) {
            if (tt.getHoGiaDinh() == null || tt.getKhoanThu() == null) {
                continue;
            }

            if (idKhoanThu != null && !Objects.equals(tt.getKhoanThu().getId(), idKhoanThu)) {
                continue;
            }

            BigDecimal yeuCau = laySoTienYeuCau(tt);
            BigDecimal daNop = nz(tt.getSoTienDaNop());
            BigDecimal conThieu = yeuCau.subtract(daNop);

            if (conThieu.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            HoGiaDinh ho = tt.getHoGiaDinh();

            if (!kw.isBlank()) {
                String soCanHo = safe(ho.getSoCanHo()).toLowerCase();
                String chuHo = safe(ho.getChuHo()).toLowerCase();

                if (!soCanHo.contains(kw) && !chuHo.contains(kw)) {
                    continue;
                }
            }

            NoPhiHoDto hoDto = result.computeIfAbsent(ho.getId(), id -> {
                NoPhiHoDto dto = new NoPhiHoDto();
                dto.setIdHo(ho.getId());
                dto.setSoCanHo(ho.getSoCanHo());
                dto.setChuHo(ho.getChuHo());
                dto.setEmail(layEmailHo(ho));
                dto.setNgayNopGanNhat(ngayGanNhatTheoHo.get(ho.getId()));
                dto.setTongNo(BigDecimal.ZERO);
                dto.setDanhSachNo(new ArrayList<>());
                return dto;
            });

            NoPhiChiTietDto chiTiet = new NoPhiChiTietDto(
                    tt.getKhoanThu().getId(),
                    tt.getKhoanThu().getTenKhoanThu(),
                    yeuCau,
                    daNop,
                    conThieu
            );

            hoDto.getDanhSachNo().add(chiTiet);
            hoDto.setTongNo(hoDto.getTongNo().add(conThieu));
        }

        return result.values()
                .stream()
                .filter(dto -> dto.getTongNo().compareTo(minNo) >= 0)
                .sorted(Comparator.comparing(
                        NoPhiHoDto::getSoCanHo,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .collect(Collectors.toList());
    }

    public NoPhiHoDto getNoPhiCuaHo(Integer idHo) {
        return getBangNoPhi(null, null, BigDecimal.ZERO)
                .stream()
                .filter(dto -> Objects.equals(dto.getIdHo(), idHo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hộ này hiện không có khoản nợ nào."));
    }

    public List<ThongKeKhoanThuDto> getThongKeKhoanDongGop(YearMonth thang, String loaiFilter) {
        YearMonth ym = thang == null ? YearMonth.now() : thang;
        String filter = loaiFilter == null ? "ALL" : loaiFilter;

        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<KhoanThu> khoanThus = khoanThuRepository.findByKyThuBetween(from, to);
        List<ThanhToan> tatCaThanhToan = thanhToanRepository.findAll();
        long tongSoHo = hoGiaDinhRepository.count();

        List<ThongKeKhoanThuDto> result = new ArrayList<>();

        for (KhoanThu kt : khoanThus) {
            String loai = layTenLoaiHienThi(kt);

            if (!matchLoaiFilter(loai, filter)) {
                continue;
            }

            List<ThanhToan> payments = tatCaThanhToan
                    .stream()
                    .filter(t -> t.getKhoanThu() != null)
                    .filter(t -> Objects.equals(t.getKhoanThu().getId(), kt.getId()))
                    .collect(Collectors.toList());

            BigDecimal tongYeuCau = payments
                    .stream()
                    .map(this::laySoTienYeuCau)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tongDaThu = payments
                    .stream()
                    .map(t -> nz(t.getSoTienDaNop()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal conThieu = tongYeuCau.subtract(tongDaThu);
            if (conThieu.compareTo(BigDecimal.ZERO) < 0) {
                conThieu = BigDecimal.ZERO;
            }

            long soDaDong = 0;
            long soConNo = 0;
            long soDongDu = 0;

            for (ThanhToan t : payments) {
                BigDecimal yeuCau = laySoTienYeuCau(t);
                BigDecimal daThu = nz(t.getSoTienDaNop());

                int cmp = daThu.compareTo(yeuCau);

                if (cmp == 0 && yeuCau.compareTo(BigDecimal.ZERO) > 0) {
                    soDaDong++;
                } else if (cmp < 0) {
                    soConNo++;
                } else if (cmp > 0) {
                    soDongDu++;
                }
            }

            long soHoCoBanGhi = payments
                    .stream()
                    .filter(t -> t.getHoGiaDinh() != null)
                    .map(t -> t.getHoGiaDinh().getId())
                    .distinct()
                    .count();

            long soChuaNop = "Tự nguyện".equals(loai)
                    ? Math.max(tongSoHo - soHoCoBanGhi, 0)
                    : 0;

            BigDecimal tiLe = BigDecimal.ZERO;
            if (tongYeuCau.compareTo(BigDecimal.ZERO) > 0) {
                tiLe = tongDaThu
                        .multiply(BigDecimal.valueOf(100))
                        .divide(tongYeuCau, 2, RoundingMode.HALF_UP);
            }

            ThongKeKhoanThuDto dto = new ThongKeKhoanThuDto(
                    kt.getId(),
                    kt.getTenKhoanThu(),
                    loai,
                    tongYeuCau,
                    tongDaThu,
                    conThieu,
                    soDaDong,
                    soConNo,
                    soDongDu,
                    soChuaNop,
                    tiLe
            );

            result.add(dto);
        }

        return result;
    }

    public BigDecimal tongTienDaThuThangNay() {
        YearMonth ym = YearMonth.now();

        return thanhToanRepository.findAll()
                .stream()
                .filter(t -> t.getNgayNop() != null)
                .filter(t -> {
                    LocalDate ngay = toLocalDate(t.getNgayNop());
                    return ngay != null && YearMonth.from(ngay).equals(ym);
                })
                .map(t -> nz(t.getSoTienDaNop()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal tongTienNo(List<NoPhiHoDto> list) {
        return list.stream()
                .map(NoPhiHoDto::getTongNo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal tongTienYeuCau(List<ThongKeKhoanThuDto> list) {
        return list.stream()
                .map(ThongKeKhoanThuDto::getTongTienYeuCau)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal tongTienDaThu(List<ThongKeKhoanThuDto> list) {
        return list.stream()
                .map(ThongKeKhoanThuDto::getTongTienDaThu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal tongTienConThieu(List<ThongKeKhoanThuDto> list) {
        return list.stream()
                .map(ThongKeKhoanThuDto::getConThieu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long tongSoHoDangNoItNhatMotKhoanTrongThongKe(List<ThongKeKhoanThuDto> thongKeList) {
        Set<Integer> idKhoanSet = thongKeList.stream()
                .map(ThongKeKhoanThuDto::getIdKhoanThu)
                .collect(Collectors.toSet());

        return thanhToanRepository.findAll()
                .stream()
                .filter(t -> t.getHoGiaDinh() != null && t.getKhoanThu() != null)
                .filter(t -> idKhoanSet.contains(t.getKhoanThu().getId()))
                .filter(t -> {
                    BigDecimal yeuCau = laySoTienYeuCau(t);
                    BigDecimal daNop = nz(t.getSoTienDaNop());
                    return yeuCau.subtract(daNop).compareTo(BigDecimal.ZERO) > 0;
                })
                .map(t -> t.getHoGiaDinh().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .size();
    }

    public String taoTieuDeEmail(NoPhiHoDto noPhi) {
        return "Nhắc thanh toán phí căn hộ " + safe(noPhi.getSoCanHo());
    }

    public String taoNoiDungEmail(NoPhiHoDto noPhi) {
        StringBuilder sb = new StringBuilder();

        sb.append("Kính gửi hộ gia đình căn hộ ")
                .append(safe(noPhi.getSoCanHo()))
                .append(",\n\n");

        sb.append("Ban quản lý BlueMoon xin thông báo hộ gia đình hiện còn các khoản phí chưa hoàn tất:\n\n");

        for (NoPhiChiTietDto ct : noPhi.getDanhSachNo()) {
            sb.append("- ")
                    .append(safe(ct.getTenKhoanThu()))
                    .append(": yêu cầu ")
                    .append(formatMoney(ct.getSoTienYeuCau()))
                    .append(", đã nộp ")
                    .append(formatMoney(ct.getSoTienDaNop()))
                    .append(", còn thiếu ")
                    .append(formatMoney(ct.getConThieu()))
                    .append("\n");
        }

        sb.append("\nTổng số tiền còn nợ: ")
                .append(formatMoney(noPhi.getTongNo()))
                .append("\n\n");

        sb.append("Quý hộ vui lòng hoàn tất thanh toán trong thời gian sớm nhất.\n");
        sb.append("Trân trọng,\nBan quản lý BlueMoon");

        return sb.toString();
    }

    private BigDecimal laySoTienYeuCau(ThanhToan thanhToan) {
        Object value = invokeGetter(thanhToan, "getSoTienYeuCau");

        if (value instanceof BigDecimal bd) {
            return nz(bd);
        }

        if (thanhToan.getKhoanThu() != null && thanhToan.getKhoanThu().getSoTien() != null) {
            return nz(thanhToan.getKhoanThu().getSoTien());
        }

        return BigDecimal.ZERO;
    }

    private String layEmailHo(HoGiaDinh ho) {
        Object email = invokeGetter(ho, "getEmail");
        return email == null ? "" : email.toString();
    }

    private boolean matchLoaiFilter(String loai, String filter) {
        if ("ALL".equalsIgnoreCase(filter)) {
            return true;
        }

        if ("BAT_BUOC".equalsIgnoreCase(filter)) {
            return "Bắt buộc".equals(loai);
        }

        if ("DOT_XUAT".equalsIgnoreCase(filter)) {
            return "Đột xuất".equals(loai);
        }

        if ("TU_NGUYEN".equalsIgnoreCase(filter)) {
            return "Tự nguyện".equals(loai);
        }

        return true;
    }

    private String layTenLoaiHienThi(KhoanThu khoanThu) {
        if (khoanThu == null || khoanThu.getLoaiKhoanThu() == null) {
            return "Không rõ";
        }

        LoaiKhoanThu loai = khoanThu.getLoaiKhoanThu();

        Object loaiApDung = invokeGetter(loai, "getLoaiApDung");
        if (loaiApDung != null) {
            String value = loaiApDung.toString();

            if (value.contains("TU_NGUYEN")) {
                return "Tự nguyện";
            }

            if (value.contains("DOT_XUAT")) {
                return "Đột xuất";
            }

            return "Bắt buộc";
        }

        String tenLoai = safe(loai.getTenLoai()).toLowerCase();

        if (tenLoai.contains("tự nguyện") || tenLoai.contains("tu nguyen")) {
            return "Tự nguyện";
        }

        if (tenLoai.contains("đột xuất") || tenLoai.contains("dot xuat")) {
            return "Đột xuất";
        }

        if (tenLoai.contains("bắt buộc") || tenLoai.contains("bat buoc")) {
            return "Bắt buộc";
        }

        return safe(loai.getTenLoai()).isBlank() ? "Không rõ" : loai.getTenLoai();
    }

    private Object invokeGetter(Object target, String methodName) {
        if (target == null) {
            return null;
        }

        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) {
            return ld;
        }

        if (value instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }

        return null;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatMoney(BigDecimal value) {
        BigDecimal v = nz(value).setScale(0, RoundingMode.HALF_UP);
        return String.format("%,.0f VNĐ", v);
    }
}