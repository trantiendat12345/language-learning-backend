package com.languagelearning.language_learning_backend.auth.controller;

import com.languagelearning.language_learning_backend.auth.dto.request.LoginRequest;
import com.languagelearning.language_learning_backend.auth.dto.request.RegisterRequest;
import com.languagelearning.language_learning_backend.auth.dto.response.AuthResponse;
import com.languagelearning.language_learning_backend.auth.service.AuthService;
import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint công khai (permitAll trong SecurityConfig) cho đăng ký/đăng nhập.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ApiResponse.success(CommonMessage.AUTH_REGISTER_SUCCESS, response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success(CommonMessage.AUTH_LOGIN_SUCCESS, response);
    }
}
