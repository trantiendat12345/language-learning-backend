package com.languagelearning.language_learning_backend.grammar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarExampleResponse {

    private Long id;
    private String exampleText;
    private String translation;
    private String note;
}
