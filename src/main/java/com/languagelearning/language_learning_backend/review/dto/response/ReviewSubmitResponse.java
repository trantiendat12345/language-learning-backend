package com.languagelearning.language_learning_backend.review.dto.response;

import com.languagelearning.language_learning_backend.review.enums.MasteryLevel;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Trả về từ POST /api/review/{vocabularyId} — trạng thái UserVocabularyProgress mới nhất sau khi áp dụng rating. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmitResponse {

    private Long vocabularyId;
    private int repetitionCount;
    private float easeFactor;
    private int intervalDays;
    private LocalDate nextReviewDate;
    private LocalDate lastReviewDate;
    private int forgotCount;
    private MasteryLevel masteryLevel;
}
