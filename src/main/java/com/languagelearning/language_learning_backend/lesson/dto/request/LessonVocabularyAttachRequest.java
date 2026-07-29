package com.languagelearning.language_learning_backend.lesson.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** lessonId lấy từ path (POST /api/admin/lessons/{lessonId}/vocabularies), không nằm trong body. */
@Getter
@Setter
public class LessonVocabularyAttachRequest {

    @NotNull(message = ValidationMessage.LESSON_VOCABULARY_ID_REQUIRED)
    private Long vocabularyId;

    @Min(value = 0, message = ValidationMessage.LESSON_VOCABULARY_DISPLAY_ORDER_MIN)
    private int displayOrder;
}
