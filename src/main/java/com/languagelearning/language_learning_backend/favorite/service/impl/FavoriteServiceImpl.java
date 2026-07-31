package com.languagelearning.language_learning_backend.favorite.service.impl;

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
import com.languagelearning.language_learning_backend.favorite.service.FavoriteService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final DeckRepository deckRepository;
    private final VocabularyRepository vocabularyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(Long userId) {
        return favoriteRepository.findAllByUserIdOrderByFavoritedAtDesc(userId).stream()
                .map(favorite -> resolveTarget(favorite.getTargetType(), favorite.getTargetId())
                        .map(target -> toResponse(favorite, target))
                        .orElse(null))
                .filter(response -> response != null)
                .toList();
    }

    @Override
    @Transactional
    public FavoriteResponse addFavorite(Long userId, FavoriteCreateRequest request) {
        TargetInfo target = resolveTarget(request.getTargetType(), request.getTargetId())
                .orElseThrow(ResourceNotFoundException::new);

        if (request.getTargetType() == FavoriteTargetType.DECK && !isDeckVisibleTo(target.deck(), userId)) {
            // Không tiết lộ Deck Private của người khác tồn tại - cùng quy tắc DeckServiceImpl.getDeckById.
            throw new ResourceNotFoundException();
        }

        Favorite existing = favoriteRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, request.getTargetType(), request.getTargetId())
                .orElse(null);
        if (existing != null) {
            return toResponse(existing, target);
        }

        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setTargetType(request.getTargetType());
        favorite.setTargetId(request.getTargetId());
        favorite.setFavoritedAt(LocalDateTime.now());
        favorite = favoriteRepository.save(favorite);

        return toResponse(favorite, target);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId).orElseThrow(ResourceNotFoundException::new);
        if (!favorite.getUserId().equals(userId)) {
            throw new OwnershipViolationException();
        }
        favoriteRepository.delete(favorite);
    }

    private boolean isDeckVisibleTo(Deck deck, Long currentUserId) {
        boolean publicActive = deck.getVisibility() == DeckVisibility.PUBLIC && deck.getStatus() == DeckStatus.ACTIVE;
        boolean owner = currentUserId != null && currentUserId.equals(deck.getOwnerId());
        return publicActive || owner;
    }

    private Optional<TargetInfo> resolveTarget(FavoriteTargetType targetType, Long targetId) {
        return switch (targetType) {
            case COURSE -> courseRepository.findById(targetId).map(TargetInfo::of);
            case DECK -> deckRepository.findById(targetId).map(TargetInfo::of);
            case VOCABULARY -> vocabularyRepository.findById(targetId).map(TargetInfo::of);
        };
    }

    private FavoriteResponse toResponse(Favorite favorite, TargetInfo target) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .targetType(favorite.getTargetType())
                .targetId(favorite.getTargetId())
                .title(target.title())
                .imageUrl(target.imageUrl())
                .favoritedAt(favorite.getFavoritedAt())
                .build();
    }

    private record TargetInfo(String title, String imageUrl, Deck deck) {
        static TargetInfo of(Course course) {
            return new TargetInfo(course.getTitle(), course.getThumbnailUrl(), null);
        }

        static TargetInfo of(Deck deck) {
            return new TargetInfo(deck.getTitle(), deck.getCoverImageUrl(), deck);
        }

        static TargetInfo of(Vocabulary vocabulary) {
            return new TargetInfo(vocabulary.getWord(), vocabulary.getImageUrl(), null);
        }
    }
}
