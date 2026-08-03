package com.languagelearning.language_learning_backend.lesson.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** courseId lấy từ path (POST /api/admin/courses/{courseId}/lessons), không nằm trong body. */
@Getter
@Setter
public class LessonCreateRequest {

    @NotBlank(message = ValidationMessage.LESSON_TITLE_REQUIRED)
    @Size(max = 200, message = ValidationMessage.LESSON_TITLE_SIZE)
    private String title;

    private String description;

    private int displayOrder;

    @Size(max = 500, message = ValidationMessage.LESSON_VIDEO_URL_SIZE)
    @SafeUrl
    private String videoUrl;

    @Size(max = 500, message = ValidationMessage.LESSON_AUDIO_URL_SIZE)
    @SafeUrl
    private String audioUrl;

    @Min(value = 0, message = ValidationMessage.LESSON_ESTIMATED_MINUTES_MIN)
    private Integer estimatedMinutes;
}
