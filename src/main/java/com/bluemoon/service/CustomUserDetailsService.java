package com.bluemoon.service;

import com.bluemoon.dao.NguoiDungRepository;
import com.bluemoon.model.NguoiDung;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;

    @Override
    public UserDetails loadUserByUsername(String tenDangNhap) throws UsernameNotFoundException {
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + tenDangNhap));

        return User.builder()
                .username(nd.getTenDangNhap())
                .password(nd.getMatKhau())
                .roles(nd.getVaiTro().name())
                .disabled(Boolean.FALSE.equals(nd.getActive()))
                .build();
    }
}
