package com.languagelearning.language_learning_backend.grammar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Rút gọn cho GET /api/admin/lessons/{lessonId}/grammars — không có explanation/example. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarSummaryResponse {

    private Long id;
    private String title;
    private String pattern;
    private String difficulty;
    private int displayOrder;
}
