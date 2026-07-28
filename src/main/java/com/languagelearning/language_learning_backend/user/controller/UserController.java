package com.languagelearning.language_learning_backend.user.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import com.languagelearning.language_learning_backend.user.dto.request.ChangePasswordRequest;
import com.languagelearning.language_learning_backend.user.dto.request.UserUpdateRequest;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint hồ sơ cá nhân — toàn bộ protected (mặc định `anyRequest().authenticated()` ở
 * SecurityConfig, không cần khai báo riêng). `userId` luôn lấy từ `CustomUserDetails` do
 * `JwtAuthenticationFilter` set vào SecurityContext, không bao giờ nhận từ path/param —
 * đây là lý do endpoint dùng "/me" thay vì "/{userId}" (xem CLAUDE.md #6).
 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        UserResponse response = userService.getMyProfile(currentUser.getUserId());
        return ApiResponse.success(response);
    }

    @PutMapping
    public ApiResponse<UserResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateMyProfile(currentUser.getUserId(), request);
        return ApiResponse.success(CommonMessage.USER_PROFILE_UPDATE_SUCCESS, response);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getUserId(), request);
        return ApiResponse.success(CommonMessage.USER_CHANGE_PASSWORD_SUCCESS, null);
    }
}
