package com.languagelearning.language_learning_backend.user.dto.response;

import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Thông tin đầy đủ của User trả về cho client - KHÔNG chứa passwordHash, dùng cho response
 * của register và GET/PUT /api/users/me.
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
    private String avatarUrl;
    private LocalDate birthday;
    private String gender;
    private String country;
    private String currentLevel;
    private int xp;
    private int currentStreak;
    private int longestStreak;
    private int coin;
    private String timezone;
    private UserStatus status;
}
