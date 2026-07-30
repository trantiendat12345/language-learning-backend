package com.languagelearning.language_learning_backend.review.service;

import com.languagelearning.language_learning_backend.review.dto.request.ReviewSubmitRequest;
import com.languagelearning.language_learning_backend.review.dto.response.ReviewSubmitResponse;
import com.languagelearning.language_learning_backend.review.dto.response.ReviewTodayItemResponse;
import java.util.List;

public interface ReviewService {

    /** Danh sách Vocabulary có UserVocabularyProgress.nextReviewDate <= hôm nay (theo timezone currentUser), sắp xếp quá hạn lâu nhất trước. */
    List<ReviewTodayItemResponse> getTodayReviews(Long userId);

    /**
     * Chưa có UserVocabularyProgress cho (user, vocabulary) → tạo mới với giá trị khởi tạo
     * trước khi áp dụng rating (D2 — khoá theo cặp user+vocabulary, không theo nguồn học).
     * Luôn ghi thêm 1 dòng ReviewLog (append-only).
     */
    ReviewSubmitResponse submitReview(Long vocabularyId, ReviewSubmitRequest request, Long userId);
}
