package com.languagelearning.language_learning_backend.quiz.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** questionCount bỏ trống = lấy Tất cả (tương đương "ALL" trong FRS — diễn giải sang kiểu Integer nullable cho gọn phía backend). */
@Getter
@Setter
public class QuizGenerateRequest {

    @NotNull(message = ValidationMessage.QUIZ_SOURCE_TYPE_REQUIRED)
    private QuestionSourceType sourceType;

    @NotNull(message = ValidationMessage.QUIZ_SOURCE_ID_REQUIRED)
    private Long sourceId;

    @Min(value = 1, message = ValidationMessage.QUIZ_QUESTION_COUNT_MIN)
    private Integer questionCount;
}
