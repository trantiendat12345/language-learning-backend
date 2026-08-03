package com.languagelearning.language_learning_backend.quiz.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUpdateRequest {

    @NotNull(message = ValidationMessage.QUESTION_SOURCE_TYPE_REQUIRED)
    private QuestionSourceType sourceType;

    @NotNull(message = ValidationMessage.QUESTION_SOURCE_ID_REQUIRED)
    private Long sourceId;

    @NotNull(message = ValidationMessage.QUESTION_LANGUAGE_ID_REQUIRED)
    private Long languageId;

    @NotNull(message = ValidationMessage.QUESTION_TYPE_REQUIRED)
    private QuestionType type;

    private Long vocabularyId;

    @Size(max = 2000, message = ValidationMessage.QUESTION_PROMPT_TEXT_SIZE)
    private String promptText;

    @Size(max = 500, message = ValidationMessage.QUESTION_PROMPT_AUDIO_URL_SIZE)
    @SafeUrl
    private String promptAudioUrl;

    @Size(max = 500, message = ValidationMessage.QUESTION_PROMPT_IMAGE_URL_SIZE)
    @SafeUrl
    private String promptImageUrl;

    private String explanation;

    @Size(max = 20, message = ValidationMessage.QUESTION_DIFFICULTY_SIZE)
    private String difficulty;

    @Valid
    private List<QuestionOptionRequest> options;
}
