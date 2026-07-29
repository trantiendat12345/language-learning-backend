package com.languagelearning.language_learning_backend.lesson.dto.response;

import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Rút gọn cho danh sách Lesson trong 1 Course — không có video/audio/description. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSummaryResponse {

    private Long id;
    private String title;
    private int displayOrder;
    private Integer estimatedMinutes;
    private LessonStatus status;
}
