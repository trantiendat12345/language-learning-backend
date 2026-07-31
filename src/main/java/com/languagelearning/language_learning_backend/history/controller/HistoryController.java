package com.languagelearning.language_learning_backend.history.controller;

import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.history.dto.response.ActivityHistoryResponse;
import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import com.languagelearning.language_learning_backend.history.service.ActivityHistoryService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Protected (mặc định anyRequest().authenticated(), không cần khai báo riêng SecurityConfig). */
@RestController
@RequiredArgsConstructor
public class HistoryController {

    private static final int DEFAULT_LIMIT = 50;

    private final ActivityHistoryService activityHistoryService;

    @GetMapping("/api/history/recent")
    public ApiResponse<List<ActivityHistoryResponse>> getRecentHistory(
            @RequestParam(required = false) ActivityAction action,
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(activityHistoryService.getMyHistory(currentUser.getUserId(), action, limit));
    }
}
