package com.languagelearning.language_learning_backend.deck.mapper;

import com.languagelearning.language_learning_backend.deck.dto.response.DeckResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckSummaryResponse;
import com.languagelearning.language_learning_backend.deck.entity.Deck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeckMapper {

    @Mapping(target = "ownerId", source = "deck.owner.id")
    @Mapping(target = "languageId", source = "deck.language.id")
    @Mapping(target = "languageCode", source = "deck.language.code")
    @Mapping(target = "clonedFromDeckId", source = "deck.clonedFromDeck.id")
    @Mapping(target = "cardCount", source = "cardCount")
    DeckResponse toResponse(Deck deck, long cardCount);

    @Mapping(target = "languageCode", source = "deck.language.code")
    @Mapping(target = "cardCount", source = "cardCount")
    DeckSummaryResponse toSummaryResponse(Deck deck, long cardCount);
}
