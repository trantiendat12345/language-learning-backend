package com.languagelearning.language_learning_backend.deck.dto.response;

import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Đầy đủ cho GET /api/decks/{id} — danh sách từ xem riêng qua GET /api/decks/{id}/cards (tránh response phình to). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckResponse {

    private Long id;
    private Long ownerId;
    private Long languageId;
    private String languageCode;
    private String title;
    private String description;
    private String coverImageUrl;
    private DeckVisibility visibility;
    private Long clonedFromDeckId;
    private DeckStatus status;
    private long cardCount;
}
