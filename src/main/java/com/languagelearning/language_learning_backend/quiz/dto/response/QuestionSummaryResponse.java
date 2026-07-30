package com.languagelearning.language_learning_backend.quiz.dto.response;

import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Rút gọn cho danh sách GET /api/admin/questions — không có option/explanation. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryResponse {

    private Long id;
    private QuestionSourceType sourceType;
    private Long sourceId;
    private QuestionType type;
    private String promptText;
    private String difficulty;
}
