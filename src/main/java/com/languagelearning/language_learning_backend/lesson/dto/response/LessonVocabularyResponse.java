package com.languagelearning.language_learning_backend.lesson.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Vocabulary như được gắn vào 1 Lesson cụ thể (kèm displayOrder theo bảng join LessonVocabulary). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonVocabularyResponse {

    private Long vocabularyId;
    private String word;
    private String meaning;
    private String ipa;
    private String imageUrl;
    private String wordType;
    private int displayOrder;
}
