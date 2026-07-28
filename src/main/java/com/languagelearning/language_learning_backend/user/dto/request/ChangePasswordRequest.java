package com.languagelearning.language_learning_backend.user.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu client gửi lên khi đổi mật khẩu lúc đã đăng nhập (PUT /api/users/me/password).
 * Khác Reset Password ở chỗ cần xác thực currentPassword thay vì token gửi qua email.
 */
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = ValidationMessage.CURRENT_PASSWORD_REQUIRED)
    private String currentPassword;

    @NotBlank(message = ValidationMessage.NEW_PASSWORD_REQUIRED)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = ValidationMessage.PASSWORD_PATTERN)
    private String newPassword;

    @NotBlank(message = ValidationMessage.CONFIRM_NEW_PASSWORD_REQUIRED)
    private String confirmPassword;
}
