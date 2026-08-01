package com.languagelearning.language_learning_backend.notification.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.notification.dto.response.NotificationResponse;
import com.languagelearning.language_learning_backend.notification.service.NotificationService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Toàn bộ endpoint protected (mặc định anyRequest().authenticated(), không cần khai báo riêng SecurityConfig). */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(notificationService.getMyNotifications(currentUser.getUserId()));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.markAsRead(currentUser.getUserId(), id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.markAllAsRead(currentUser.getUserId());
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }
}
