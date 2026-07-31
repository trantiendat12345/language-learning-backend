package com.languagelearning.language_learning_backend.progress.controller;

import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.progress.dto.response.ProgressDashboardResponse;
import com.languagelearning.language_learning_backend.progress.service.ProgressDashboardService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Toàn bộ endpoint protected (mặc định anyRequest().authenticated(), không cần khai báo riêng SecurityConfig). */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressDashboardController {

    private final ProgressDashboardService progressDashboardService;

    @GetMapping("/dashboard")
    public ApiResponse<ProgressDashboardResponse> getDashboard(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(progressDashboardService.getDashboard(currentUser.getUserId()));
    }
}
