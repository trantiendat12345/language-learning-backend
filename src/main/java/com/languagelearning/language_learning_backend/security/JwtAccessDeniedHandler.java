package com.languagelearning.language_learning_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Trả 403 dạng ApiErrorResponse chuẩn khi đã đăng nhập hợp lệ nhưng không đủ quyền
 * (vd USER gọi endpoint /api/admin/**) — cùng lý do với JwtAuthenticationEntryPoint.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .code(HttpStatus.FORBIDDEN.value())
                .errorCode(ErrorCode.FORBIDDEN)
                .message(ErrorMessage.FORBIDDEN)
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
