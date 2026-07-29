package com.languagelearning.language_learning_backend.course.dto.response;

import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonSummaryResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Đầy đủ cho GET /api/courses/{id} — kèm danh sách Lesson theo displayOrder. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private Long languageId;
    private String languageCode;
    private String languageName;
    private String title;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private String difficulty;
    private Integer estimatedMinutes;
    private int displayOrder;
    private CourseStatus status;
    private List<LessonSummaryResponse> lessons;
}
