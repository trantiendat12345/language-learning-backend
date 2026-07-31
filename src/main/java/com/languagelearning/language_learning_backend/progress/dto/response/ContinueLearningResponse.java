package com.languagelearning.language_learning_backend.progress.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Khoá học + bài học kế tiếp để hiển thị nút "Continue Learning" trên Dashboard - null nếu user chưa enroll khoá nào đang IN_PROGRESS. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContinueLearningResponse {

    private Long courseId;
    private String courseTitle;
    private Long lessonId;
    private String lessonTitle;
}
