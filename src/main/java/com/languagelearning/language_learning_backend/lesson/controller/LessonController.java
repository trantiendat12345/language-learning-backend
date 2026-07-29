package com.languagelearning.language_learning_backend.lesson.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.service.LessonService;
import com.languagelearning.language_learning_backend.progress.dto.response.LessonCompleteResponse;
import com.languagelearning.language_learning_backend.progress.service.LessonProgressService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * `GET /{id}` public (permitAll trong SecurityConfig) — `@AuthenticationPrincipal` vẫn được
 * `JwtAuthenticationFilter` set nếu có token hợp lệ dù route permitAll, nên vẫn lấy được
 * currentUserId (null nếu chưa đăng nhập) để xác định gating theo Enroll, xem LessonResponse.
 * `POST /{id}/complete` protected (rơi vào `anyRequest().authenticated()` mặc định, không
 * cần khai báo riêng trong SecurityConfig vì không phải GET).
 */
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;

    @GetMapping("/{id}")
    public ApiResponse<LessonResponse> getLessonById(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long currentUserId = currentUser == null ? null : currentUser.getUserId();
        return ApiResponse.success(lessonService.getPublishedLessonById(id, currentUserId));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<LessonCompleteResponse> completeLesson(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(CommonMessage.SUCCESS, lessonProgressService.completeLesson(id, currentUser.getUserId()));
    }
}
