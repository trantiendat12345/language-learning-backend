package com.languagelearning.language_learning_backend.grammar.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrammarExampleRequest {

    @NotBlank(message = ValidationMessage.GRAMMAR_EXAMPLE_TEXT_REQUIRED)
    private String exampleText;

    private String translation;

    private String note;
}
