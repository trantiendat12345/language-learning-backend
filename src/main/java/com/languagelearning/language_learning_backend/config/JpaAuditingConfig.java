package com.languagelearning.language_learning_backend.config;

import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Bật Spring Data JPA Auditing để tự động điền createdAt/createdBy/updatedAt/updatedBy
 * cho mọi entity kế thừa AuditableEntity, thông qua bean auditorProvider bên dưới.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Cung cấp userId của người đang thao tác cho createdBy/updatedBy - đọc từ
     * SecurityContext do JwtAuthenticationFilter set. Request public/chưa đăng nhập có
     * principal là "anonymousUser" (String, không phải CustomUserDetails) nên trả về rỗng,
     * hợp lý vì createdBy/updatedBy không có ý nghĩa với hành động ẩn danh (vd tự đăng ký).
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
                return Optional.empty();
            }
            return Optional.of(userDetails.getUserId());
        };
    }
}
