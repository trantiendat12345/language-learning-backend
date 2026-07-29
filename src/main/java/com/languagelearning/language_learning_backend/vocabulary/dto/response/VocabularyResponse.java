package com.languagelearning.language_learning_backend.vocabulary.dto.response;

import com.languagelearning.language_learning_backend.vocabulary.enums.VocabularyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Đầy đủ cho GET /api/vocabularies/{id}. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyResponse {

    private Long id;
    private Long languageId;
    private String languageCode;
    private String languageName;
    private String word;
    private String meaning;
    private String ipa;
    private String pronunciationAudioUrl;
    private String wordType;
    private String imageUrl;
    private String difficulty;
    private String exampleSentence;
    private String exampleTranslation;
    private Integer frequencyRank;
    private VocabularyStatus status;
}
