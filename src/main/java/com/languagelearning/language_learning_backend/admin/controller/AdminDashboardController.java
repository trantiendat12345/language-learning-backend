package com.languagelearning.language_learning_backend.admin.controller;

import com.languagelearning.language_learning_backend.admin.dto.response.AdminDashboardResponse;
import com.languagelearning.language_learning_backend.admin.service.AdminDashboardService;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Bắt buộc role ADMIN — chặn ở SecurityConfig (`/api/admin/**` -> hasRole("ADMIN")), không dùng @PreAuthorize. */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ApiResponse<AdminDashboardResponse> getDashboard() {
        return ApiResponse.success(adminDashboardService.getDashboard());
    }
}
