package com.languagelearning.language_learning_backend.quiz.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** selectedOptionId/typedAnswer đều nullable — bỏ trống cả 2 nghĩa là bỏ qua câu này (tính sai, xem QuizServiceImpl). */
@Getter
@Setter
public class QuizAnswerRequest {

    @NotNull(message = ValidationMessage.QUIZ_ANSWER_QUESTION_ID_REQUIRED)
    private Long questionId;

    private Long selectedOptionId;

    @Size(max = 500, message = ValidationMessage.QUIZ_ANSWER_TYPED_ANSWER_SIZE)
    private String typedAnswer;
}
