package com.languagelearning.language_learning_backend.lesson.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Không có courseId — không cho đổi 1 Lesson sang Course khác qua endpoint này. */
@Getter
@Setter
public class LessonUpdateRequest {

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

    @NotNull(message = ValidationMessage.LESSON_STATUS_REQUIRED)
    private LessonStatus status;
}
