package com.languagelearning.language_learning_backend.quiz.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOptionRequest {

    @Size(max = 500, message = ValidationMessage.QUESTION_OPTION_TEXT_SIZE)
    private String optionText;

    @Size(max = 500, message = ValidationMessage.QUESTION_OPTION_IMAGE_URL_SIZE)
    @SafeUrl
    private String optionImageUrl;

    private boolean correct;

    @Min(value = 0, message = ValidationMessage.QUESTION_OPTION_DISPLAY_ORDER_MIN)
    private int displayOrder;
}
