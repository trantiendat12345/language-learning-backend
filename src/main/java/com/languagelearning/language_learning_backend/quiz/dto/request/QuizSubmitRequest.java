package com.languagelearning.language_learning_backend.quiz.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitRequest {

    @NotNull(message = ValidationMessage.QUIZ_SOURCE_TYPE_REQUIRED)
    private QuestionSourceType sourceType;

    @NotNull(message = ValidationMessage.QUIZ_SOURCE_ID_REQUIRED)
    private Long sourceId;

    @Min(value = 0, message = ValidationMessage.QUIZ_DURATION_SECONDS_MIN)
    private int durationSeconds;

    @NotEmpty(message = ValidationMessage.QUIZ_ANSWER_QUESTION_ID_REQUIRED)
    @Valid
    private List<QuizAnswerRequest> answers;
}
