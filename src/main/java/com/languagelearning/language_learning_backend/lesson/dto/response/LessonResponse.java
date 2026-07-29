package com.languagelearning.language_learning_backend.lesson.dto.response;

import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Đầy đủ cho GET /api/lessons/{id}. Chunk hiện tại (Course+Lesson) CHƯA có nội dung
 * Vocabulary/Grammar gắn kèm (2 entity đó làm ở chunk sau) nên chưa có phân biệt preview/đầy
 * đủ theo trạng thái Enroll — trả nguyên vẹn field Lesson cho mọi request hợp lệ, xem
 * docs/PROJECT_OVERVIEW.md mục 13 phần giới hạn phạm vi.
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
}
