package com.languagelearning.language_learning_backend.quiz.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** selectedOptionId/typedAnswer đều nullable — bỏ trống cả 2 nghĩa là bỏ qua câu này (tính sai, xem QuizServiceImpl). */
@Getter
@Setter
public class QuizAnswerRequest {

    @NotNull(message = ValidationMessage.QUIZ_ANSWER_QUESTION_ID_REQUIRED)
    private Long questionId;

    private Long selectedOptionId;

    private String typedAnswer;
}
