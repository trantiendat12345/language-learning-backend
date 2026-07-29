package com.languagelearning.language_learning_backend.progress.dto.response;

import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Trả về từ POST /api/courses/{id}/enroll. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollmentResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private EnrollmentStatus status;
    private int progressPercent;
    private LocalDateTime enrolledAt;
}
