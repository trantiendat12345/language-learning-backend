package com.languagelearning.language_learning_backend.course.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.course.enums.CourseStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Không có languageId/slug — 2 field này cố định từ lúc tạo, giống lý do Language.code. */
@Getter
@Setter
public class CourseUpdateRequest {

    @NotBlank(message = ValidationMessage.COURSE_TITLE_REQUIRED)
    @Size(max = 200, message = ValidationMessage.COURSE_TITLE_SIZE)
    private String title;

    private String description;

    @Size(max = 500, message = ValidationMessage.COURSE_THUMBNAIL_URL_SIZE)
    private String thumbnailUrl;

    @Size(max = 20, message = ValidationMessage.COURSE_DIFFICULTY_SIZE)
    private String difficulty;

    @Min(value = 0, message = ValidationMessage.COURSE_ESTIMATED_MINUTES_MIN)
    private Integer estimatedMinutes;

    private int displayOrder;

    @NotNull(message = ValidationMessage.COURSE_STATUS_REQUIRED)
    private CourseStatus status;
}
