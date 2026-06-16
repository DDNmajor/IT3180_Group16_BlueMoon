package com.bluemoon.util;

import com.bluemoon.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@EnableWebSecurity
@EnableAsync
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationFailureHandler authFailureHandler() {
        return (request, response, exception) -> {
            String redirect = (exception instanceof DisabledException)
                    ? "/login?disabled"
                    : "/login?error";
            response.sendRedirect(redirect);
        };
    }

    @Bean
    public AuthenticationSuccessHandler authSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/dashboard");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/actuator/health").permitAll()
                .requestMatchers("/thu-ho/gen", "/thu-ho/gen/**").permitAll()
                .requestMatchers("/nguoi-dung/**", "/audit-log/**", "/thung-rac/**").hasRole("admin")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authSuccessHandler())
                .failureHandler(authFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}
