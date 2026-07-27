package com.languagelearning.language_learning_backend.auth.controller;

import com.languagelearning.language_learning_backend.auth.dto.request.LoginRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.RegisterRequest;
import com.languagelearning.language_learning_backend.auth.dto.response.AuthResponse;
import com.languagelearning.language_learning_backend.auth.service.AuthService;
import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint công khai (permitAll trong SecurityConfig) cho đăng ký/đăng nhập.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthService authService;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ApiResponse.success(CommonMessage.AUTH_REGISTER_SUCCESS, response);
    }

    /**
     * accessToken trả trong JSON body như bình thường. refreshToken KHÔNG nằm trong JSON
     * (xem `AuthResponse.refreshToken` có `@JsonIgnore`) — set vào cookie httpOnly để JS phía
     * Frontend không đọc/lộ được token này (chống XSS đánh cắp refresh token, xem
     * docs/PROJECT_OVERVIEW.md mục 8).
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, response.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success(CommonMessage.AUTH_LOGIN_SUCCESS, response);
    }
}
