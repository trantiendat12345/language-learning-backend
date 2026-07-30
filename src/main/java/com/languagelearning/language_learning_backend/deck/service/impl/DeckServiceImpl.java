package com.languagelearning.language_learning_backend.deck.service.impl;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckCardAddRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckCreateRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckUpdateRequest;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckCardResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckSummaryResponse;
import com.languagelearning.language_learning_backend.deck.entity.Deck;
import com.languagelearning.language_learning_backend.deck.entity.DeckCard;
import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import com.languagelearning.language_learning_backend.deck.mapper.DeckMapper;
import com.languagelearning.language_learning_backend.deck.repository.DeckCardRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckSpecification;
import com.languagelearning.language_learning_backend.deck.service.DeckService;
import com.languagelearning.language_learning_backend.exception.BadRequestException;
import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;
import com.languagelearning.language_learning_backend.exception.OwnershipViolationException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.language.repository.LanguageRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeckServiceImpl implements DeckService {

    private static final String VOCABULARY_ALREADY_IN_DECK_MESSAGE = "Từ vựng đã có trong Deck này";
    private static final String VOCABULARY_ID_OR_WORD_REQUIRED_MESSAGE =
            "Phải cung cấp vocabularyId (từ có sẵn) hoặc word+meaning (tạo từ mới)";

    private final DeckRepository deckRepository;
    private final DeckCardRepository deckCardRepository;
    private final LanguageRepository languageRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final DeckMapper deckMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckSummaryResponse> searchPublicDecks(String keyword, Pageable pageable) {
        Specification<Deck> spec = Specification.allOf(
                DeckSpecification.hasVisibility(DeckVisibility.PUBLIC),
                DeckSpecification.hasStatus(DeckStatus.ACTIVE),
                DeckSpecification.titleContains(keyword));
        Page<DeckSummaryResponse> page = deckRepository.findAll(spec, pageable).map(this::toSummaryResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckSummaryResponse> getMyDecks(Long currentUserId, Pageable pageable) {
        Page<DeckSummaryResponse> page = deckRepository
                .findAll(DeckSpecification.hasOwnerId(currentUserId), pageable)
                .map(this::toSummaryResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public DeckResponse getDeckById(Long id, Long currentUserId) {
        Deck deck = findDeckOrThrow(id);
        if (!isVisibleTo(deck, currentUserId)) {
            // Không tiết lộ Deck Private của người khác tồn tại - trả cùng lỗi với id không tồn tại.
            throw new ResourceNotFoundException();
        }
        return toResponse(deck);
    }

    @Override
    @Transactional
    public DeckResponse createDeck(DeckCreateRequest request, Long currentUserId) {
        Language language = languageRepository.findById(request.getLanguageId()).orElseThrow(ResourceNotFoundException::new);
        User owner = userRepository.findById(currentUserId).orElseThrow(ResourceNotFoundException::new);

        Deck deck = new Deck();
        deck.setOwner(owner);
        deck.setLanguage(language);
        deck.setTitle(request.getTitle());
        deck.setDescription(request.getDescription());
        deck.setCoverImageUrl(request.getCoverImageUrl());
        deck.setVisibility(request.getVisibility() == null ? DeckVisibility.PRIVATE : request.getVisibility());
        deck = deckRepository.save(deck);
        return toResponse(deck);
    }

    @Override
    @Transactional
    public DeckResponse updateDeck(Long id, DeckUpdateRequest request, Long currentUserId) {
        Deck deck = findDeckOrThrow(id);
        requireOwnership(deck, currentUserId);

        deck.setTitle(request.getTitle());
        deck.setDescription(request.getDescription());
        deck.setCoverImageUrl(request.getCoverImageUrl());
        deck.setVisibility(request.getVisibility());
        deck.setStatus(request.getStatus());
        deck = deckRepository.save(deck);
        return toResponse(deck);
    }

    /** Soft-delete (D9) - không ảnh hưởng UserVocabularyProgress vì progress khoá theo (user, vocabulary), độc lập Deck (D2). */
    @Override
    @Transactional
    public void deleteDeck(Long id, Long currentUserId) {
        Deck deck = findDeckOrThrow(id);
        requireOwnership(deck, currentUserId);
        deck.setDeleted(true);
        deck.setDeletedAt(LocalDateTime.now());
        deckRepository.save(deck);
    }

    @Override
    @Transactional
    public DeckCardResponse addCard(Long deckId, DeckCardAddRequest request, Long currentUserId) {
        Deck deck = findDeckOrThrow(deckId);
        requireOwnership(deck, currentUserId);

        Vocabulary vocabulary = resolveVocabulary(deck, request, currentUserId);
        if (deckCardRepository.existsByDeckIdAndVocabularyId(deckId, vocabulary.getId())) {
            throw new DuplicateResourceException(VOCABULARY_ALREADY_IN_DECK_MESSAGE);
        }

        DeckCard card = new DeckCard();
        card.setDeck(deck);
        card.setVocabulary(vocabulary);
        card.setDisplayOrder(request.getDisplayOrder());
        card = deckCardRepository.save(card);
        return toCardResponse(card, vocabulary);
    }

    @Override
    @Transactional
    public void removeCard(Long deckId, Long cardId, Long currentUserId) {
        Deck deck = findDeckOrThrow(deckId);
        requireOwnership(deck, currentUserId);
        DeckCard card = deckCardRepository.findByIdAndDeckId(cardId, deckId).orElseThrow(ResourceNotFoundException::new);
        deckCardRepository.delete(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeckCardResponse> getDeckCards(Long deckId, Long currentUserId) {
        Deck deck = findDeckOrThrow(deckId);
        if (!isVisibleTo(deck, currentUserId)) {
            throw new ResourceNotFoundException();
        }
        return deckCardRepository.findAllByDeckIdOrderByDisplayOrderAsc(deckId).stream()
                .map(card -> toCardResponse(card, card.getVocabulary()))
                .toList();
    }

    @Override
    @Transactional
    public DeckResponse cloneDeck(Long id, Long currentUserId) {
        Deck source = findDeckOrThrow(id);
        if (!isVisibleTo(source, currentUserId)) {
            throw new ResourceNotFoundException();
        }
        User owner = userRepository.findById(currentUserId).orElseThrow(ResourceNotFoundException::new);

        Deck clone = new Deck();
        clone.setOwner(owner);
        clone.setLanguage(source.getLanguage());
        clone.setTitle(source.getTitle());
        clone.setDescription(source.getDescription());
        clone.setCoverImageUrl(source.getCoverImageUrl());
        clone.setVisibility(DeckVisibility.PRIVATE);
        clone.setClonedFromDeck(source);
        clone = deckRepository.save(clone);

        Deck savedClone = clone;
        List<DeckCard> newCards = deckCardRepository.findAllByDeckIdOrderByDisplayOrderAsc(id).stream()
                .map(sourceCard -> {
                    DeckCard newCard = new DeckCard();
                    newCard.setDeck(savedClone);
                    newCard.setVocabulary(sourceCard.getVocabulary());
                    newCard.setDisplayOrder(sourceCard.getDisplayOrder());
                    return newCard;
                })
                .toList();
        deckCardRepository.saveAll(newCards);

        return toResponse(clone);
    }

    private Vocabulary resolveVocabulary(Deck deck, DeckCardAddRequest request, Long currentUserId) {
        if (request.getVocabularyId() != null) {
            return vocabularyRepository.findById(request.getVocabularyId()).orElseThrow(ResourceNotFoundException::new);
        }
        if (request.getWord() == null || request.getWord().isBlank() || request.getMeaning() == null || request.getMeaning().isBlank()) {
            throw new BadRequestException(VOCABULARY_ID_OR_WORD_REQUIRED_MESSAGE);
        }

        User owner = userRepository.findById(currentUserId).orElseThrow(ResourceNotFoundException::new);
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setLanguage(deck.getLanguage());
        vocabulary.setOwner(owner);
        vocabulary.setWord(request.getWord());
        vocabulary.setMeaning(request.getMeaning());
        vocabulary.setIpa(request.getIpa());
        vocabulary.setImageUrl(request.getImageUrl());
        vocabulary.setExampleSentence(request.getExampleSentence());
        vocabulary.setExampleTranslation(request.getExampleTranslation());
        return vocabularyRepository.save(vocabulary);
    }

    /** PUBLIC+ACTIVE thấy được bởi mọi người (kể cả ẩn danh); còn lại chỉ chủ sở hữu. */
    private boolean isVisibleTo(Deck deck, Long currentUserId) {
        boolean publicActive = deck.getVisibility() == DeckVisibility.PUBLIC && deck.getStatus() == DeckStatus.ACTIVE;
        boolean owner = currentUserId != null && currentUserId.equals(deck.getOwnerId());
        return publicActive || owner;
    }

    private void requireOwnership(Deck deck, Long currentUserId) {
        if (!deck.getOwnerId().equals(currentUserId)) {
            throw new OwnershipViolationException();
        }
    }

    private Deck findDeckOrThrow(Long id) {
        return deckRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    private DeckResponse toResponse(Deck deck) {
        return deckMapper.toResponse(deck, deckCardRepository.countByDeckId(deck.getId()));
    }

    private DeckSummaryResponse toSummaryResponse(Deck deck) {
        return deckMapper.toSummaryResponse(deck, deckCardRepository.countByDeckId(deck.getId()));
    }

    private DeckCardResponse toCardResponse(DeckCard card, Vocabulary vocabulary) {
        return DeckCardResponse.builder()
                .id(card.getId())
                .vocabularyId(vocabulary.getId())
                .word(vocabulary.getWord())
                .meaning(vocabulary.getMeaning())
                .ipa(vocabulary.getIpa())
                .imageUrl(vocabulary.getImageUrl())
                .wordType(vocabulary.getWordType())
                .displayOrder(card.getDisplayOrder())
                .build();
    }
}
