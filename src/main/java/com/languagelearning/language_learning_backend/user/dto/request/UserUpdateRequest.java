package com.languagelearning.language_learning_backend.user.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu client gửi lên khi sửa hồ sơ cá nhân (PUT /api/users/me). Chỉ gồm các field được
 * phép tự sửa theo docs/testing/12_FRS_TC_USER_PROFILE.md mục 1.2 — cố tình KHÔNG có
 * username/email/password/xp/coin/role/status để client không thể gửi lên đổi các field này
 * dù có cố tình nhét thêm vào JSON (Jackson bỏ qua field không khai báo trong DTO).
 */
@Getter
@Setter
public class UserUpdateRequest {

    @Size(max = 100, message = ValidationMessage.DISPLAY_NAME_SIZE)
    private String displayName;

    @Size(max = 500, message = ValidationMessage.AVATAR_URL_SIZE)
    private String avatarUrl;

    @Past(message = ValidationMessage.BIRTHDAY_MUST_BE_PAST)
    private LocalDate birthday;

    @Size(max = 20, message = ValidationMessage.GENDER_SIZE)
    private String gender;

    @Size(max = 100, message = ValidationMessage.COUNTRY_SIZE)
    private String country;

    @Size(max = 20, message = ValidationMessage.CURRENT_LEVEL_SIZE)
    private String currentLevel;
}
