package com.languagelearning.language_learning_backend.vocabulary.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Không có ownerId — endpoint Admin chỉ tạo từ hệ thống (owner luôn null), xem Vocabulary.java. */
@Getter
@Setter
public class VocabularyCreateRequest {

    @NotNull(message = ValidationMessage.VOCABULARY_LANGUAGE_ID_REQUIRED)
    private Long languageId;

    @NotBlank(message = ValidationMessage.VOCABULARY_WORD_REQUIRED)
    @Size(max = 200, message = ValidationMessage.VOCABULARY_WORD_SIZE)
    private String word;

    @NotBlank(message = ValidationMessage.VOCABULARY_MEANING_REQUIRED)
    @Size(max = 500, message = ValidationMessage.VOCABULARY_MEANING_SIZE)
    private String meaning;

    @Size(max = 100, message = ValidationMessage.VOCABULARY_IPA_SIZE)
    private String ipa;

    @Size(max = 500, message = ValidationMessage.VOCABULARY_AUDIO_URL_SIZE)
    @SafeUrl
    private String pronunciationAudioUrl;

    @Size(max = 30, message = ValidationMessage.VOCABULARY_WORD_TYPE_SIZE)
    private String wordType;

    @Size(max = 500, message = ValidationMessage.VOCABULARY_IMAGE_URL_SIZE)
    @SafeUrl
    private String imageUrl;

    @Size(max = 20, message = ValidationMessage.VOCABULARY_DIFFICULTY_SIZE)
    private String difficulty;

    private String exampleSentence;

    private String exampleTranslation;

    @Min(value = 0, message = ValidationMessage.VOCABULARY_FREQUENCY_RANK_MIN)
    private Integer frequencyRank;
}
