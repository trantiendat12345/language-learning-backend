package com.languagelearning.language_learning_backend.deck.service;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckCardAddRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckCreateRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckUpdateRequest;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckCardResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckSummaryResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface DeckService {

    /** Chỉ trả Deck visibility=PUBLIC status=ACTIVE. */
    PageResponse<DeckSummaryResponse> searchPublicDecks(String keyword, Pageable pageable);

    PageResponse<DeckSummaryResponse> getMyDecks(Long currentUserId, Pageable pageable);

    /** 404 nếu không tồn tại/không PUBLIC+ACTIVE và currentUserId không phải chủ sở hữu (không tiết lộ Deck Private của người khác). currentUserId nullable (chưa đăng nhập). */
    DeckResponse getDeckById(Long id, Long currentUserId);

    DeckResponse createDeck(DeckCreateRequest request, Long currentUserId);

    /** 403 OWNERSHIP_VIOLATION nếu không phải chủ sở hữu. */
    DeckResponse updateDeck(Long id, DeckUpdateRequest request, Long currentUserId);

    void deleteDeck(Long id, Long currentUserId);

    /** 400 nếu không có vocabularyId lẫn word+meaning. 409 nếu từ đã có trong Deck. */
    DeckCardResponse addCard(Long deckId, DeckCardAddRequest request, Long currentUserId);

    void removeCard(Long deckId, Long cardId, Long currentUserId);

    /** Cùng quy tắc hiển thị với getDeckById. */
    List<DeckCardResponse> getDeckCards(Long deckId, Long currentUserId);

    /** 404 nếu Deck nguồn không tồn tại/không xem được (Private của người khác). Deck mới luôn PRIVATE. */
    DeckResponse cloneDeck(Long id, Long currentUserId);
}
