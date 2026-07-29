package com.languagelearning.language_learning_backend.lesson.dto.response;

import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Đầy đủ cho GET /api/lessons/{id}, kèm Vocabulary (qua LessonVocabulary, theo displayOrder)
 * và Grammar (đầy đủ, gồm example) gắn với Lesson. Chunk hiện tại CHƯA có `CourseEnrollment`
 * nên chưa phân biệt preview/đầy đủ theo trạng thái Enroll — trả nguyên vẹn nội dung cho mọi
 * request hợp lệ tới Lesson PUBLISHED, xem docs/PROJECT_OVERVIEW.md mục 13 phần giới hạn phạm vi.
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
    private List<LessonVocabularyResponse> vocabularies;
    private List<GrammarResponse> grammars;
}
