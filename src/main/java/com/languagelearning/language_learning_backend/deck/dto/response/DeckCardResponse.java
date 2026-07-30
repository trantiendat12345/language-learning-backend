package com.languagelearning.language_learning_backend.deck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Vocabulary như được gắn vào 1 Deck cụ thể (kèm displayOrder theo bảng join DeckCard). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckCardResponse {

    private Long id;
    private Long vocabularyId;
    private String word;
    private String meaning;
    private String ipa;
    private String imageUrl;
    private String wordType;
    private int displayOrder;
}
