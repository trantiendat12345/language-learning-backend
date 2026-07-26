package com.languagelearning.language_learning_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình bảo mật tối thiểu của Giai đoạn 1: chỉ đủ để mở công khai Swagger UI/OpenAPI docs
 * (mặc định Spring Security tự khoá toàn bộ endpoint khi chưa có config nào). Toàn bộ endpoint
 * khác vẫn yêu cầu authenticated. Class này sẽ được thay thế bằng JWT filter chain đầy đủ +
 * phân quyền theo từng endpoint ở Giai đoạn 2 (Auth) - xem docs/testing/06_ROLES_PERMISSIONS_MATRIX.md.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Đăng ký filter chain: permitAll cho Swagger, còn lại bắt buộc authenticated. */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
