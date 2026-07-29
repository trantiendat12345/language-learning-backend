package com.languagelearning.language_learning_backend.grammar.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** lessonId lấy từ path (POST /api/admin/lessons/{lessonId}/grammars), không nằm trong body. */
@Getter
@Setter
public class GrammarCreateRequest {

    @NotBlank(message = ValidationMessage.GRAMMAR_TITLE_REQUIRED)
    @Size(max = 200, message = ValidationMessage.GRAMMAR_TITLE_SIZE)
    private String title;

    @Size(max = 255, message = ValidationMessage.GRAMMAR_PATTERN_SIZE)
    private String pattern;

    private String explanation;

    @Size(max = 20, message = ValidationMessage.GRAMMAR_DIFFICULTY_SIZE)
    private String difficulty;

    @Min(value = 0, message = ValidationMessage.GRAMMAR_DISPLAY_ORDER_MIN)
    private int displayOrder;

    @Valid
    private List<GrammarExampleRequest> examples;
}
