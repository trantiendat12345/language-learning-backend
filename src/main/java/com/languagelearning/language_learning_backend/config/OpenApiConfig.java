package com.languagelearning.language_learning_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Khai báo metadata cho Swagger UI (title, mô tả, version) và đăng ký sẵn scheme xác thực
 * "bearerAuth" (JWT Bearer Token) để nút Authorize trên Swagger UI hoạt động ngay khi
 * module Auth (Giai đoạn 2) sinh ra access token thật - không cần cấu hình lại lúc đó.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Language Learning Platform API",
                version = "v1",
                description = "API cho website hoc ngoai ngu - xem docs/PROJECT_OVERVIEW.md"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
