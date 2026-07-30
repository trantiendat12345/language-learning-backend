package com.languagelearning.language_learning_backend.review.repository;

import com.languagelearning.language_learning_backend.review.entity.UserVocabularyProgress;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVocabularyProgressRepository extends JpaRepository<UserVocabularyProgress, Long> {

    Optional<UserVocabularyProgress> findByUserIdAndVocabularyId(Long userId, Long vocabularyId);

    List<UserVocabularyProgress> findAllByUserIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(
            Long userId, LocalDate today);
}
