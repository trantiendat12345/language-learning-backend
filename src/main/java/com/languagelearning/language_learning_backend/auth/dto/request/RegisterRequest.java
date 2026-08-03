package com.languagelearning.language_learning_backend.auth.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu client gửi lên khi đăng ký tài khoản mới (POST /api/auth/register).
 * confirmPassword chỉ dùng để validate khớp với password ở tầng Service, không lưu xuống DB.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = ValidationMessage.USERNAME_REQUIRED)
    @Size(min = 3, max = 50, message = ValidationMessage.USERNAME_SIZE)
    @Pattern(regexp = "^\\S+$", message = ValidationMessage.USERNAME_NO_WHITESPACE)
    private String username;

    @NotBlank(message = ValidationMessage.EMAIL_REQUIRED)
    @Email(message = ValidationMessage.EMAIL_INVALID)
    @Size(max = 255, message = ValidationMessage.EMAIL_SIZE)
    private String email;

    @NotBlank(message = ValidationMessage.PASSWORD_REQUIRED)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = ValidationMessage.PASSWORD_PATTERN)
    private String password;

    @NotBlank(message = ValidationMessage.CONFIRM_PASSWORD_REQUIRED)
    private String confirmPassword;
}
