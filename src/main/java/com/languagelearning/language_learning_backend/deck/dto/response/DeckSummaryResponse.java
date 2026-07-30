package com.languagelearning.language_learning_backend.deck.dto.response;

import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Rút gọn cho danh sách (search public / My Decks) — không có description đầy đủ. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckSummaryResponse {

    private Long id;
    private String title;
    private String coverImageUrl;
    private String languageCode;
    private DeckVisibility visibility;
    private DeckStatus status;
    private long cardCount;
}
