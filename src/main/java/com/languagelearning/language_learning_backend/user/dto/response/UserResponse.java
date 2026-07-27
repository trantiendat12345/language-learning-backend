package com.languagelearning.language_learning_backend.user.dto.response;

import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Thông tin User trả về cho client - KHÔNG chứa passwordHash, dùng cho response
 * của register và (về sau) GET /api/users/me.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String displayName;
    private UserStatus status;
}
