package com.languagelearning.language_learning_backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Access Token mới cấp lại từ Refresh Token còn hạn (POST /api/auth/refresh-token).
 * Không có refreshToken vì endpoint này không rotate Refresh Token — cookie hiện tại giữ
 * nguyên tới khi hết hạn hoặc user logout.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponse {

    private String accessToken;
}
