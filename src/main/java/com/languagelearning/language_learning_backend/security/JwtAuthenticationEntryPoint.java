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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Trả 401 dạng ApiErrorResponse chuẩn khi request chưa đăng nhập gọi API protected —
 * thay cho trang lỗi mặc định của Spring Security, để FE và tester luôn nhận đúng 1 định
 * dạng lỗi cho mọi API (xem docs/dev/ERROR_CODE_CATALOG.md).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .errorCode(ErrorCode.UNAUTHORIZED)
                .message(ErrorMessage.UNAUTHORIZED)
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
