package com.languagelearning.language_learning_backend.auth.controller;

import com.languagelearning.language_learning_backend.auth.dto.request.ForgotPasswordRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.LoginRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.RegisterRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.ResetPasswordRequest;
import com.languagelearning.language_learning_backend.auth.dto.response.AccessTokenResponse;
import com.languagelearning.language_learning_backend.auth.dto.response.AuthResponse;
import com.languagelearning.language_learning_backend.auth.service.AuthService;
import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint cho đăng ký/đăng nhập/đăng xuất/refresh token/quên-đặt lại mật khẩu/xác thực email.
 * Hầu hết public (permitAll trong SecurityConfig), riêng `/logout` bắt buộc có Access Token
 * hợp lệ (`authenticated()` trong SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";

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
        setRefreshTokenCookie(httpResponse, response.getRefreshToken());
        return ApiResponse.success(CommonMessage.AUTH_LOGIN_SUCCESS, response);
    }

    /** Đọc refresh token từ cookie httpOnly — không rotate cookie, chỉ cấp accessToken mới. */
    @PostMapping("/refresh-token")
    public ApiResponse<AccessTokenResponse> refreshToken(
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String rawRefreshToken) {
        AccessTokenResponse response = authService.refreshAccessToken(rawRefreshToken);
        return ApiResponse.success(CommonMessage.AUTH_REFRESH_TOKEN_SUCCESS, response);
    }

    /** Yêu cầu Access Token hợp lệ (SecurityConfig: `/api/auth/logout` là `authenticated()`). */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String rawRefreshToken,
            HttpServletResponse httpResponse) {
        authService.logout(currentUser.getUserId(), rawRefreshToken);
        clearRefreshTokenCookie(httpResponse);
        return ApiResponse.success(CommonMessage.AUTH_LOGOUT_SUCCESS, null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success(CommonMessage.AUTH_FORGOT_PASSWORD_SUCCESS, null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success(CommonMessage.AUTH_RESET_PASSWORD_SUCCESS, null);
    }

    @GetMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ApiResponse.success(CommonMessage.AUTH_VERIFY_EMAIL_SUCCESS, null);
    }

    private void setRefreshTokenCookie(HttpServletResponse httpResponse, String rawRefreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** Xoá cookie bằng cách set lại cùng name/path với Max-Age=0 — trình duyệt tự xoá ngay. */
    private void clearRefreshTokenCookie(HttpServletResponse httpResponse) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
