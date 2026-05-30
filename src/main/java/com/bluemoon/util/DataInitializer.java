package com.bluemoon.util;

import com.bluemoon.dao.NguoiDungRepository;
import com.bluemoon.model.NguoiDung;
import com.bluemoon.model.VaiTro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.fullname:Quản trị viên}")
    private String adminFullname;

    @Override
    public void run(ApplicationArguments args) {
        if (nguoiDungRepository.existsByTenDangNhap(adminUsername)) {
            return;
        }

        NguoiDung admin = new NguoiDung();
        admin.setTenDangNhap(adminUsername);
        admin.setMatKhau(passwordEncoder.encode(adminPassword));
        admin.setHoTen(adminFullname);
        admin.setVaiTro(VaiTro.admin);
        admin.setNgayTao(LocalDateTime.now());

        nguoiDungRepository.save(admin);
        log.info("Đã tạo tài khoản admin: {}", adminUsername);
    }
}
