package com.languagelearning.language_learning_backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kết quả đăng nhập thành công (POST /api/auth/login). `accessToken` trả trong JSON body
 * như bình thường; `refreshToken` chỉ dùng nội bộ để AuthController set vào cookie httpOnly
 * (`@JsonIgnore` để không lộ ra JSON response — client không bao giờ đọc refresh token qua
 * JS, tránh bị đánh cắp qua XSS) — xem docs/PROJECT_OVERVIEW.md mục 8.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    @JsonIgnore
    private String refreshToken;
}
