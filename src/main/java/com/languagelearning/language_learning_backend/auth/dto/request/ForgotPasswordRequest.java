package com.languagelearning.language_learning_backend.auth.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu client gửi lên khi quên mật khẩu (POST /api/auth/forgot-password).
 */
@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = ValidationMessage.EMAIL_REQUIRED)
    @Email(message = ValidationMessage.EMAIL_INVALID)
    @Size(max = 255, message = ValidationMessage.EMAIL_SIZE)
    private String email;
}
