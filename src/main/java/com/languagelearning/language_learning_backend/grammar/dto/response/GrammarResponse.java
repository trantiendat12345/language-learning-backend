package com.languagelearning.language_learning_backend.grammar.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Đầy đủ cho GET /api/admin/grammars/{id} và nhúng vào LessonResponse — kèm toàn bộ example. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarResponse {

    private Long id;
    private Long lessonId;
    private String title;
    private String pattern;
    private String explanation;
    private String difficulty;
    private int displayOrder;
    private List<GrammarExampleResponse> examples;
}
