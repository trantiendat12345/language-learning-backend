package com.languagelearning.language_learning_backend.deck.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.deck.dto.request.DeckCardAddRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckCreateRequest;
import com.languagelearning.language_learning_backend.deck.dto.request.DeckUpdateRequest;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckCardResponse;
import com.languagelearning.language_learning_backend.deck.dto.response.DeckResponse;
import com.languagelearning.language_learning_backend.deck.entity.Deck;
import com.languagelearning.language_learning_backend.deck.entity.DeckCard;
import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import com.languagelearning.language_learning_backend.deck.mapper.DeckMapper;
import com.languagelearning.language_learning_backend.deck.repository.DeckCardRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckCardRepository deckCardRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private UserRepository userRepository;

    private DeckServiceImpl deckService;

    @BeforeEach
    void setUp() {
        DeckMapper mapper = Mappers.getMapper(DeckMapper.class);
        deckService = new DeckServiceImpl(
                deckRepository, deckCardRepository, languageRepository, vocabularyRepository, userRepository, mapper);
    }

    private Language language() {
        Language language = new Language();
        language.setId(1L);
        language.setCode("en");
        return language;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    /** Set cả `owner` (association) lẫn `ownerId` (shadow field, insertable=false/updatable=false - bình thường chỉ Hibernate tự điền khi load từ DB thật, phải set tay ở unit test dùng plain object). */
    private Deck deck(Long id, Long ownerId, DeckVisibility visibility, DeckStatus status) {
        Deck deck = new Deck();
        deck.setId(id);
        deck.setOwner(user(ownerId));
        deck.setOwnerId(ownerId);
        deck.setLanguage(language());
        deck.setTitle("My Deck");
        deck.setVisibility(visibility);
        deck.setStatus(status);
        return deck;
    }

    @Test
    void createDeck_defaultsToPrivateVisibility() {
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language()));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user(100L)));
        when(deckRepository.save(any(Deck.class))).thenAnswer(invocation -> {
            Deck deck = invocation.getArgument(0);
            deck.setId(1L);
            return deck;
        });
        when(deckCardRepository.countByDeckId(1L)).thenReturn(0L);

        DeckCreateRequest request = new DeckCreateRequest();
        request.setLanguageId(1L);
        request.setTitle("My New Deck");

        DeckResponse response = deckService.createDeck(request, 100L);

        assertThat(response.getVisibility()).isEqualTo(DeckVisibility.PRIVATE);
        assertThat(response.getOwnerId()).isEqualTo(100L);
    }

    @Test
    void updateDeck_whenOwner_updatesFields() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deckCardRepository.countByDeckId(1L)).thenReturn(0L);

        DeckUpdateRequest request = new DeckUpdateRequest();
        request.setTitle("Updated Title");
        request.setVisibility(DeckVisibility.PUBLIC);
        request.setStatus(DeckStatus.ACTIVE);

        DeckResponse response = deckService.updateDeck(1L, request, 100L);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getVisibility()).isEqualTo(DeckVisibility.PUBLIC);
    }

    @Test
    void updateDeck_whenNotOwner_throwsOwnershipViolationException() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        assertThatThrownBy(() -> deckService.updateDeck(1L, new DeckUpdateRequest(), 999L))
                .isInstanceOf(OwnershipViolationException.class);
        verify(deckRepository, never()).save(any());
    }

    @Test
    void deleteDeck_whenNotOwner_throwsOwnershipViolationException() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        assertThatThrownBy(() -> deckService.deleteDeck(1L, 999L)).isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void deleteDeck_whenOwner_softDeletes() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenAnswer(invocation -> invocation.getArgument(0));

        deckService.deleteDeck(1L, 100L);

        assertThat(deck.isDeleted()).isTrue();
        assertThat(deck.getDeletedAt()).isNotNull();
    }

    @Test
    void getDeckById_whenPublicActive_visibleToAnonymous() {
        Deck deck = deck(1L, 100L, DeckVisibility.PUBLIC, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(deckCardRepository.countByDeckId(1L)).thenReturn(3L);

        DeckResponse response = deckService.getDeckById(1L, null);

        assertThat(response.getCardCount()).isEqualTo(3L);
    }

    @Test
    void getDeckById_whenPrivateAndNotOwner_throwsResourceNotFoundException() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        assertThatThrownBy(() -> deckService.getDeckById(1L, 999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDeckById_whenPrivateAndOwner_returnsResponse() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(deckCardRepository.countByDeckId(1L)).thenReturn(0L);

        DeckResponse response = deckService.getDeckById(1L, 100L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void addCard_withExistingVocabularyId_savesCard() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(50L);
        vocabulary.setWord("family");
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary));
        when(deckCardRepository.existsByDeckIdAndVocabularyId(1L, 50L)).thenReturn(false);
        when(deckCardRepository.save(any(DeckCard.class))).thenAnswer(invocation -> {
            DeckCard card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        DeckCardAddRequest request = new DeckCardAddRequest();
        request.setVocabularyId(50L);

        DeckCardResponse response = deckService.addCard(1L, request, 100L);

        assertThat(response.getWord()).isEqualTo("family");
    }

    @Test
    void addCard_whenDuplicateVocabulary_throwsDuplicateResourceException() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(50L);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(vocabularyRepository.findById(50L)).thenReturn(Optional.of(vocabulary));
        when(deckCardRepository.existsByDeckIdAndVocabularyId(1L, 50L)).thenReturn(true);

        DeckCardAddRequest request = new DeckCardAddRequest();
        request.setVocabularyId(50L);

        assertThatThrownBy(() -> deckService.addCard(1L, request, 100L)).isInstanceOf(DuplicateResourceException.class);
        verify(deckCardRepository, never()).save(any());
    }

    @Test
    void addCard_withCustomWord_createsNewVocabularyOwnedByCurrentUser() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user(100L)));
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(invocation -> {
            Vocabulary vocabulary = invocation.getArgument(0);
            vocabulary.setId(60L);
            return vocabulary;
        });
        when(deckCardRepository.existsByDeckIdAndVocabularyId(1L, 60L)).thenReturn(false);
        when(deckCardRepository.save(any(DeckCard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeckCardAddRequest request = new DeckCardAddRequest();
        request.setWord("testword123");
        request.setMeaning("nghĩa test");

        DeckCardResponse response = deckService.addCard(1L, request, 100L);

        assertThat(response.getWord()).isEqualTo("testword123");
        verify(vocabularyRepository).save(any(Vocabulary.class));
    }

    @Test
    void addCard_withoutVocabularyIdOrWord_throwsBadRequestException() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        assertThatThrownBy(() -> deckService.addCard(1L, new DeckCardAddRequest(), 100L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void addCard_whenNotOwner_throwsOwnershipViolationException() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));

        assertThatThrownBy(() -> deckService.addCard(1L, new DeckCardAddRequest(), 999L))
                .isInstanceOf(OwnershipViolationException.class);
    }

    @Test
    void removeCard_whenOwner_deletes() {
        Deck deck = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        DeckCard card = new DeckCard();
        card.setId(5L);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(deck));
        when(deckCardRepository.findByIdAndDeckId(5L, 1L)).thenReturn(Optional.of(card));

        deckService.removeCard(1L, 5L, 100L);

        verify(deckCardRepository).delete(card);
    }

    @Test
    void cloneDeck_copiesCardsAndSetsPrivateVisibility() {
        Deck source = deck(1L, 100L, DeckVisibility.PUBLIC, DeckStatus.ACTIVE);
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(50L);
        DeckCard sourceCard = new DeckCard();
        sourceCard.setVocabulary(vocabulary);
        sourceCard.setDisplayOrder(1);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(source));
        when(userRepository.findById(200L)).thenReturn(Optional.of(user(200L)));
        when(deckRepository.save(any(Deck.class))).thenAnswer(invocation -> {
            Deck clone = invocation.getArgument(0);
            clone.setId(2L);
            return clone;
        });
        when(deckCardRepository.findAllByDeckIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(sourceCard));
        when(deckCardRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(deckCardRepository.countByDeckId(2L)).thenReturn(1L);

        DeckResponse response = deckService.cloneDeck(1L, 200L);

        assertThat(response.getOwnerId()).isEqualTo(200L);
        assertThat(response.getVisibility()).isEqualTo(DeckVisibility.PRIVATE);
        assertThat(response.getCardCount()).isEqualTo(1L);
    }

    @Test
    void cloneDeck_whenSourcePrivateAndNotOwner_throwsResourceNotFoundException() {
        Deck source = deck(1L, 100L, DeckVisibility.PRIVATE, DeckStatus.ACTIVE);
        when(deckRepository.findById(1L)).thenReturn(Optional.of(source));

        assertThatThrownBy(() -> deckService.cloneDeck(1L, 999L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
