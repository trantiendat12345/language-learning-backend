package com.languagelearning.language_learning_backend.deck.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckCardAddRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckCreateRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckUpdateRequest;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckCardResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckSummaryResponse;
import com.languagelearning.language_learning_backend.deck.service.DeckService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * `GET /mine` protected (khai báo riêng TRƯỚC trong SecurityConfig, giống `/api/auth/logout`,
 * vì rơi vào rule GET permitAll rộng hơn của `/api/decks/**`). Các `GET` còn lại permitAll —
 * `/{id}` và `/{id}/cards` tự gating theo `enrolled`/ownership bên trong Service (dùng
 * `@AuthenticationPrincipal` nullable), xem DeckServiceImpl.isVisibleTo. Toàn bộ POST/PUT/DELETE
 * protected mặc định (`anyRequest().authenticated()`), kiểm ownership ở Service.
 */
@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public ApiResponse<PageResponse<DeckSummaryResponse>> searchPublicDecks(
            @RequestParam(required = false) String keyword, @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(deckService.searchPublicDecks(keyword, pageable));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<DeckSummaryResponse>> getMyDecks(
            @AuthenticationPrincipal CustomUserDetails currentUser, @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(deckService.getMyDecks(currentUser.getUserId(), pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeckResponse> getDeckById(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long currentUserId = currentUser == null ? null : currentUser.getUserId();
        return ApiResponse.success(deckService.getDeckById(id, currentUserId));
    }

    @GetMapping("/{id}/cards")
    public ApiResponse<List<DeckCardResponse>> getDeckCards(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long currentUserId = currentUser == null ? null : currentUser.getUserId();
        return ApiResponse.success(deckService.getDeckCards(id, currentUserId));
    }

    @PostMapping
    public ApiResponse<DeckResponse> createDeck(
            @Valid @RequestBody DeckCreateRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(CommonMessage.SUCCESS, deckService.createDeck(request, currentUser.getUserId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<DeckResponse> updateDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(CommonMessage.SUCCESS, deckService.updateDeck(id, request, currentUser.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeck(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        deckService.deleteDeck(id, currentUser.getUserId());
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }

    @PostMapping("/{id}/cards")
    public ApiResponse<DeckCardResponse> addCard(
            @PathVariable Long id,
            @Valid @RequestBody DeckCardAddRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(CommonMessage.SUCCESS, deckService.addCard(id, request, currentUser.getUserId()));
    }

    @DeleteMapping("/{id}/cards/{cardId}")
    public ApiResponse<Void> removeCard(
            @PathVariable Long id, @PathVariable Long cardId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        deckService.removeCard(id, cardId, currentUser.getUserId());
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }

    @PostMapping("/{id}/clone")
    public ApiResponse<DeckResponse> cloneDeck(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(CommonMessage.SUCCESS, deckService.cloneDeck(id, currentUser.getUserId()));
    }
}
