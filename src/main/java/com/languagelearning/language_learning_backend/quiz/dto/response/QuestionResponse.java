package com.languagelearning.language_learning_backend.quiz.dto.response;

import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Đầy đủ cho GET /api/admin/questions/{id} — kèm toàn bộ option (có đáp án đúng, chỉ Admin thấy). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private Long id;
    private QuestionSourceType sourceType;
    private Long sourceId;
    private Long languageId;
    private QuestionType type;
    private Long vocabularyId;
    private String promptText;
    private String promptAudioUrl;
    private String promptImageUrl;
    private String explanation;
    private String difficulty;
    private List<QuestionOptionResponse> options;
}
