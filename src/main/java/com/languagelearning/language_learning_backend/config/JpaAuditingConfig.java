package com.languagelearning.language_learning_backend.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Bật Spring Data JPA Auditing để tự động điền createdAt/createdBy/updatedAt/updatedBy
 * cho mọi entity kế thừa AuditableEntity, thông qua bean auditorProvider bên dưới.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Cung cấp userId của người đang thao tác cho createdBy/updatedBy. Hiện tại luôn trả về
     * rỗng vì module Auth (Giai đoạn 2) chưa tồn tại; sau khi có SecurityContext thật, sửa
     * hàm này để đọc userId từ token đăng nhập thay vì Optional.empty().
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return Optional::empty;
    }
}
