package com.languagelearning.language_learning_backend.auth.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu client gửi lên khi đăng nhập (POST /api/auth/login). Cho phép nhập username hoặc
 * email vào cùng 1 field usernameOrEmail (UserRepository.findByUsernameOrEmail xử lý cả 2).
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = ValidationMessage.USERNAME_REQUIRED)
    private String usernameOrEmail;

    @NotBlank(message = ValidationMessage.PASSWORD_REQUIRED)
    private String password;
}
