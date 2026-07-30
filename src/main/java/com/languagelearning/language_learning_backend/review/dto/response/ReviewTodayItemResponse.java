package com.languagelearning.language_learning_backend.review.dto.response;

import com.languagelearning.language_learning_backend.review.enums.MasteryLevel;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 1 từ đến hạn ôn trong GET /api/review/today — sắp xếp từ quá hạn lâu nhất trước (nextReviewDate tăng dần). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewTodayItemResponse {

    private Long vocabularyId;
    private String word;
    private String meaning;
    private String ipa;
    private String imageUrl;
    private String wordType;
    private LocalDate nextReviewDate;
    private MasteryLevel masteryLevel;
}
