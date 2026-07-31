package com.languagelearning.language_learning_backend.favorite.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.deck.entity.Deck;
import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.exception.OwnershipViolationException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.favorite.dto.request.FavoriteCreateRequest;
import com.languagelearning.language_learning_backend.favorite.dto.response.FavoriteResponse;
import com.languagelearning.language_learning_backend.favorite.entity.Favorite;
import com.languagelearning.language_learning_backend.favorite.enums.FavoriteTargetType;
import com.languagelearning.language_learning_backend.favorite.repository.FavoriteRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    private FavoriteServiceImpl favoriteService;

    @BeforeEach
    void setUp() {
        favoriteService =
                new FavoriteServiceImpl(favoriteRepository, userRepository, courseRepository, deckRepository, vocabularyRepository);
    }

    private Course course() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("English A1");
        course.setThumbnailUrl("thumb.png");
        return course;
    }

    private Deck deck(Long ownerId, DeckVisibility visibility) {
        Deck deck = new Deck();
        deck.setId(2L);
        deck.setOwnerId(ownerId);
        deck.setTitle("TOEIC 600");
        deck.setVisibility(visibility);
        deck.setStatus(DeckStatus.ACTIVE);
        return deck;
    }

    private FavoriteCreateRequest request(FavoriteTargetType type, Long targetId) {
        FavoriteCreateRequest request = new FavoriteCreateRequest();
        request.setTargetType(type);
        request.setTargetId(targetId);
        return request;
    }

    @Test
    void addFavorite_newCourseFavorite_createsAndReturnsWithResolvedTitle() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course()));
        when(favoriteRepository.findByUserIdAndTargetTypeAndTargetId(100L, FavoriteTargetType.COURSE, 1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> {
            Favorite favorite = invocation.getArgument(0);
            favorite.setId(10L);
            return favorite;
        });

        FavoriteResponse response = favoriteService.addFavorite(100L, request(FavoriteTargetType.COURSE, 1L));

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("English A1");
        assertThat(response.getImageUrl()).isEqualTo("thumb.png");
    }

    @Test
    void addFavorite_duplicateTarget_returnsExistingWithoutCreatingNew() {
        Favorite existing = new Favorite();
        existing.setId(5L);
        existing.setTargetType(FavoriteTargetType.COURSE);
        existing.setTargetId(1L);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course()));
        when(favoriteRepository.findByUserIdAndTargetTypeAndTargetId(100L, FavoriteTargetType.COURSE, 1L))
                .thenReturn(Optional.of(existing));

        FavoriteResponse response = favoriteService.addFavorite(100L, request(FavoriteTargetType.COURSE, 1L));

        assertThat(response.getId()).isEqualTo(5L);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_targetNotFound_throwsResourceNotFoundException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFavorite(100L, request(FavoriteTargetType.COURSE, 999L)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_publicDeckOfAnotherUser_succeeds() {
        when(deckRepository.findById(2L)).thenReturn(Optional.of(deck(999L, DeckVisibility.PUBLIC)));
        when(favoriteRepository.findByUserIdAndTargetTypeAndTargetId(100L, FavoriteTargetType.DECK, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteResponse response = favoriteService.addFavorite(100L, request(FavoriteTargetType.DECK, 2L));

        assertThat(response.getTitle()).isEqualTo("TOEIC 600");
    }

    @Test
    void addFavorite_privateDeckOfAnotherUser_throwsResourceNotFoundException() {
        when(deckRepository.findById(2L)).thenReturn(Optional.of(deck(999L, DeckVisibility.PRIVATE)));

        assertThatThrownBy(() -> favoriteService.addFavorite(100L, request(FavoriteTargetType.DECK, 2L)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_ownPrivateDeck_succeeds() {
        when(deckRepository.findById(2L)).thenReturn(Optional.of(deck(100L, DeckVisibility.PRIVATE)));
        when(favoriteRepository.findByUserIdAndTargetTypeAndTargetId(100L, FavoriteTargetType.DECK, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteResponse response = favoriteService.addFavorite(100L, request(FavoriteTargetType.DECK, 2L));

        assertThat(response.getTitle()).isEqualTo("TOEIC 600");
    }

    @Test
    void addFavorite_vocabulary_resolvesWordAsTitle() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(3L);
        vocabulary.setWord("apple");
        vocabulary.setImageUrl("apple.png");
        when(vocabularyRepository.findById(3L)).thenReturn(Optional.of(vocabulary));
        when(favoriteRepository.findByUserIdAndTargetTypeAndTargetId(100L, FavoriteTargetType.VOCABULARY, 3L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteResponse response = favoriteService.addFavorite(100L, request(FavoriteTargetType.VOCABULARY, 3L));

        assertThat(response.getTitle()).isEqualTo("apple");
        assertThat(response.getImageUrl()).isEqualTo("apple.png");
    }

    @Test
    void getMyFavorites_targetDeleted_hidesItemWithoutCrashing() {
        Favorite favorite = new Favorite();
        favorite.setId(5L);
        favorite.setTargetType(FavoriteTargetType.COURSE);
        favorite.setTargetId(999L);
        when(favoriteRepository.findAllByUserIdOrderByFavoritedAtDesc(100L)).thenReturn(List.of(favorite));
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        List<FavoriteResponse> result = favoriteService.getMyFavorites(100L);

        assertThat(result).isEmpty();
    }

    @Test
    void getMyFavorites_mixedTargets_resolvesEachCorrectly() {
        Favorite courseFavorite = new Favorite();
        courseFavorite.setId(1L);
        courseFavorite.setTargetType(FavoriteTargetType.COURSE);
        courseFavorite.setTargetId(1L);
        Favorite deckFavorite = new Favorite();
        deckFavorite.setId(2L);
        deckFavorite.setTargetType(FavoriteTargetType.DECK);
        deckFavorite.setTargetId(2L);
        when(favoriteRepository.findAllByUserIdOrderByFavoritedAtDesc(100L)).thenReturn(List.of(courseFavorite, deckFavorite));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course()));
        when(deckRepository.findById(2L)).thenReturn(Optional.of(deck(100L, DeckVisibility.PUBLIC)));

        List<FavoriteResponse> result = favoriteService.getMyFavorites(100L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("English A1");
        assertThat(result.get(1).getTitle()).isEqualTo("TOEIC 600");
    }

    @Test
    void removeFavorite_owner_deletesSuccessfully() {
        Favorite favorite = new Favorite();
        favorite.setId(5L);
        favorite.setUserId(100L);
        when(favoriteRepository.findById(5L)).thenReturn(Optional.of(favorite));

        favoriteService.removeFavorite(100L, 5L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavorite_notOwner_throwsOwnershipViolationException() {
        Favorite favorite = new Favorite();
        favorite.setId(5L);
        favorite.setUserId(999L);
        when(favoriteRepository.findById(5L)).thenReturn(Optional.of(favorite));

        assertThatThrownBy(() -> favoriteService.removeFavorite(100L, 5L)).isInstanceOf(OwnershipViolationException.class);
        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void removeFavorite_notFound_throwsResourceNotFoundException() {
        when(favoriteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.removeFavorite(100L, 999L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
