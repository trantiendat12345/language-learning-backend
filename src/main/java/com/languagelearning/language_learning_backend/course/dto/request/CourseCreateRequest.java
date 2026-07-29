package com.languagelearning.language_learning_backend.course.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseCreateRequest {

    @NotNull(message = ValidationMessage.COURSE_LANGUAGE_ID_REQUIRED)
    private Long languageId;

    @NotBlank(message = ValidationMessage.COURSE_TITLE_REQUIRED)
    @Size(max = 200, message = ValidationMessage.COURSE_TITLE_SIZE)
    private String title;

    @NotBlank(message = ValidationMessage.COURSE_SLUG_REQUIRED)
    @Size(max = 200, message = ValidationMessage.COURSE_SLUG_SIZE)
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = ValidationMessage.COURSE_SLUG_PATTERN)
    private String slug;

    private String description;

    @Size(max = 500, message = ValidationMessage.COURSE_THUMBNAIL_URL_SIZE)
    private String thumbnailUrl;

    @Size(max = 20, message = ValidationMessage.COURSE_DIFFICULTY_SIZE)
    private String difficulty;

    @Min(value = 0, message = ValidationMessage.COURSE_ESTIMATED_MINUTES_MIN)
    private Integer estimatedMinutes;

    private int displayOrder;
}
