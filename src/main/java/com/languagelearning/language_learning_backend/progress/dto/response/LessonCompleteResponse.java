package com.languagelearning.language_learning_backend.progress.dto.response;

import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import com.languagelearning.language_learning_backend.progress.enums.LessonProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Trả về từ POST /api/lessons/{id}/complete — cho FE biết ngay trạng thái mới của Lesson + Course cha, không cần gọi lại. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompleteResponse {

    private Long lessonId;
    private LessonProgressStatus lessonProgressStatus;
    private Long courseId;
    private int courseProgressPercent;
    private EnrollmentStatus courseStatus;
}
