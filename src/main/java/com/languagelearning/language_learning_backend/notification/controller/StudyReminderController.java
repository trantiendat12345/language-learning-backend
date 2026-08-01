package com.languagelearning.language_learning_backend.notification.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderCreateRequest;
import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderUpdateRequest;
import com.languagelearning.language_learning_backend.notification.dto.response.StudyReminderResponse;
import com.languagelearning.language_learning_backend.notification.service.StudyReminderService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Toàn bộ endpoint protected (mặc định anyRequest().authenticated(), không cần khai báo riêng
 * SecurityConfig). Đặt path riêng `/api/reminders` thay vì lồng dưới `/api/notifications` —
 * FRS để mở "endpoint riêng tuỳ thiết kế cuối" (docs/testing/19_FRS_TC_NOTIFICATION_REMINDER.md
 * mục 1.2), chọn path phẳng cho nhất quán với `/api/favorites`.
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class StudyReminderController {

    private final StudyReminderService studyReminderService;

    @GetMapping
    public ApiResponse<List<StudyReminderResponse>> getMyReminders(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(studyReminderService.getMyReminders(currentUser.getUserId()));
    }

    @PostMapping
    public ApiResponse<StudyReminderResponse> createReminder(
            @Valid @RequestBody StudyReminderCreateRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(
                CommonMessage.SUCCESS, studyReminderService.createReminder(currentUser.getUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudyReminderResponse> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody StudyReminderUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(
                CommonMessage.SUCCESS, studyReminderService.updateReminder(currentUser.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReminder(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        studyReminderService.deleteReminder(currentUser.getUserId(), id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }
}
