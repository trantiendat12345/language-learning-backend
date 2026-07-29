package com.languagelearning.language_learning_backend.course.dto.response;

import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Rút gọn cho danh sách (GET /api/courses) — không có description đầy đủ. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryResponse {

    private Long id;
    private String languageCode;
    private String title;
    private String slug;
    private String thumbnailUrl;
    private String difficulty;
    private Integer estimatedMinutes;
    private CourseStatus status;
}
