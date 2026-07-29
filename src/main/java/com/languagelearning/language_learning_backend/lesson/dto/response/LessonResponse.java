package com.languagelearning.language_learning_backend.lesson.dto.response;

import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Đầy đủ cho GET /api/lessons/{id}. `enrolled=true` (đã enroll Course chứa Lesson này, hoặc
 * gọi qua endpoint Admin) → `vocabularies`/`grammars` có đầy đủ nội dung; `enrolled=false`
 * (chưa login hoặc chưa enroll) → 2 field đó rỗng (preview), các field còn lại của Lesson vẫn
 * hiển thị bình thường. Field `enrolled` giúp FE phân biệt "preview vì chưa enroll" với
 * "Lesson thật sự chưa có Vocabulary/Grammar nào" (2 field rỗng
 * nhưng lý do khác nhau).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private int displayOrder;
    private String videoUrl;
    private String audioUrl;
    private Integer estimatedMinutes;
    private LessonStatus status;
    private boolean enrolled;
    private List<LessonVocabularyResponse> vocabularies;
    private List<GrammarResponse> grammars;
}
