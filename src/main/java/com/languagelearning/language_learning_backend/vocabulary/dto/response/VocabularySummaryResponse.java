package com.languagelearning.language_learning_backend.vocabulary.dto.response;

import com.languagelearning.language_learning_backend.vocabulary.enums.VocabularyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Rút gọn cho danh sách/search (GET /api/vocabularies) — không có example. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularySummaryResponse {

    private Long id;
    private String languageCode;
    private String word;
    private String meaning;
    private String ipa;
    private String imageUrl;
    private String wordType;
    private VocabularyStatus status;
}
