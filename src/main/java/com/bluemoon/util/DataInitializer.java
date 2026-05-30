package com.bluemoon.util;

import com.bluemoon.dao.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final NguoiDungRepository nguoiDungRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        nguoiDungRepository.findAll().forEach(nd -> {
            // Nếu password chưa được hash (không bắt đầu bằng $2a$) thì hash lại
            if (!nd.getMatKhau().startsWith("$2a$")) {
                String hashed = passwordEncoder.encode(nd.getMatKhau());
                nd.setMatKhau(hashed);
                nguoiDungRepository.save(nd);
                log.info("Đã hash password cho người dùng: {}", nd.getTenDangNhap());
            }
        });
    }
}
