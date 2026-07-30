package com.languagelearning.language_learning_backend.quiz.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizGenerateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizSubmitRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptSummaryResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizGenerateResponse;
import com.languagelearning.language_learning_backend.quiz.service.QuizService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Toàn bộ endpoint protected (mặc định anyRequest().authenticated(), không cần khai báo riêng SecurityConfig). */
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    public ApiResponse<QuizGenerateResponse> generateQuiz(@Valid @RequestBody QuizGenerateRequest request) {
        return ApiResponse.success(quizService.generateQuiz(request));
    }

    @PostMapping("/attempts")
    public ApiResponse<QuizAttemptResponse> submitQuiz(
            @Valid @RequestBody QuizSubmitRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(CommonMessage.SUCCESS, quizService.submitQuiz(request, currentUser.getUserId()));
    }

    @GetMapping("/attempts")
    public ApiResponse<PageResponse<QuizAttemptSummaryResponse>> getMyQuizAttempts(
            @AuthenticationPrincipal CustomUserDetails currentUser, @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(quizService.getMyQuizAttempts(currentUser.getUserId(), pageable));
    }

    @GetMapping("/attempts/{id}")
    public ApiResponse<QuizAttemptResponse> getMyQuizAttemptById(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(quizService.getMyQuizAttemptById(id, currentUser.getUserId()));
    }
}
