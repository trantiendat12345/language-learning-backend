package com.languagelearning.language_learning_backend.config;

import com.languagelearning.language_learning_backend.security.JwtAccessDeniedHandler;
import com.languagelearning.language_learning_backend.security.JwtAuthenticationEntryPoint;
import com.languagelearning.language_learning_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật đầy đủ bằng JWT (thay thế bản tối thiểu của Giai đoạn 1). Stateless
 * (không session), public cho hầu hết `/api/auth/**` + Swagger, còn lại bắt buộc
 * authenticated. Riêng `/api/auth/logout` bắt buộc `authenticated()` (khai báo TRƯỚC matcher
 * `/api/auth/**` rộng hơn — Spring Security dùng rule khớp ĐẦU TIÊN, không phải rule cụ thể
 * nhất) vì logout cần biết currentUserId để kiểm tra ownership Refresh Token trước khi revoke.
 * `JwtAuthenticationFilter` chạy trước `UsernamePasswordAuthenticationFilter` để đọc token
 * từ header Authorization trước khi Spring Security xử lý xác thực mặc định.
 * `.cors(Customizer.withDefaults())` tự lấy bean `CorsConfigurationSource` khai báo ở
 * `CorsConfig` — bắt buộc phải bật vì `/api/auth/login` set cookie refresh token httpOnly,
 * nếu không bật CORS đúng cách trình duyệt sẽ chặn cookie khi Frontend gọi từ origin khác.
 * `/api/admin/**` bắt buộc `hasRole("ADMIN")` — MVP chỉ check Role (D7), chưa check
 * permission chi tiết dù schema `role_permission` đã sẵn sàng cho việc đó sau này.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /** Mã hoá mật khẩu một chiều bằng BCrypt — không bao giờ lưu password dạng plaintext. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/api/auth/logout")
                        .authenticated()
                        .requestMatchers("/api/auth/**")
                        .permitAll()
                        .requestMatchers("/api/languages")
                        .permitAll()
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
