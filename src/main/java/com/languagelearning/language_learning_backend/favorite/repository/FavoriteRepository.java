package com.languagelearning.language_learning_backend.favorite.repository;

import com.languagelearning.language_learning_backend.favorite.entity.Favorite;
import com.languagelearning.language_learning_backend.favorite.enums.FavoriteTargetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, FavoriteTargetType targetType, Long targetId);

    List<Favorite> findAllByUserIdOrderByFavoritedAtDesc(Long userId);
}
