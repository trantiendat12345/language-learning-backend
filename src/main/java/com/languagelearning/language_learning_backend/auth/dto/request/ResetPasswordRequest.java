package com.languagelearning.language_learning_backend.auth.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu client gửi lên khi đặt lại mật khẩu bằng token nhận từ email
 * (POST /api/auth/reset-password).
 */
@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = ValidationMessage.TOKEN_REQUIRED)
    private String token;

    @NotBlank(message = ValidationMessage.NEW_PASSWORD_REQUIRED)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = ValidationMessage.PASSWORD_PATTERN)
    private String newPassword;

    @NotBlank(message = ValidationMessage.CONFIRM_NEW_PASSWORD_REQUIRED)
    private String confirmNewPassword;
}
