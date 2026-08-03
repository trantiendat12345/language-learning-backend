package com.languagelearning.language_learning_backend.user.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import com.languagelearning.language_learning_backend.user.dto.response.AdminUserProgressResponse;
import com.languagelearning.language_learning_backend.user.dto.response.UserResponse;
import com.languagelearning.language_learning_backend.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Bắt buộc role ADMIN — chặn ở SecurityConfig (`/api/admin/**` -> hasRole("ADMIN")), không dùng @PreAuthorize. */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword, @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(adminUserService.getUsers(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.getUserById(id));
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<AdminUserProgressResponse> getUserProgress(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.getUserProgress(id));
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<UserResponse> activateUser(@PathVariable Long id) {
        return ApiResponse.success(CommonMessage.SUCCESS, adminUserService.activateUser(id));
    }

    @PutMapping("/{id}/disable")
    public ApiResponse<UserResponse> disableUser(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentAdmin) {
        return ApiResponse.success(CommonMessage.SUCCESS, adminUserService.disableUser(id, currentAdmin.getUserId()));
    }

    @PutMapping("/{id}/lock")
    public ApiResponse<UserResponse> lockUser(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentAdmin) {
        return ApiResponse.success(CommonMessage.SUCCESS, adminUserService.lockUser(id, currentAdmin.getUserId()));
    }
}
