package com.languagelearning.language_learning_backend.review.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.review.dto.request.ReviewSubmitRequest;
import com.languagelearning.language_learning_backend.review.dto.response.ReviewSubmitResponse;
import com.languagelearning.language_learning_backend.review.dto.response.ReviewTodayItemResponse;
import com.languagelearning.language_learning_backend.review.service.ReviewService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Toàn bộ endpoint protected (mặc định anyRequest().authenticated(), không cần khai báo riêng SecurityConfig). */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/today")
    public ApiResponse<List<ReviewTodayItemResponse>> getTodayReviews(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(reviewService.getTodayReviews(currentUser.getUserId()));
    }

    @PostMapping("/{vocabularyId}")
    public ApiResponse<ReviewSubmitResponse> submitReview(
            @PathVariable Long vocabularyId,
            @Valid @RequestBody ReviewSubmitRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(
                CommonMessage.SUCCESS, reviewService.submitReview(vocabularyId, request, currentUser.getUserId()));
    }
}
