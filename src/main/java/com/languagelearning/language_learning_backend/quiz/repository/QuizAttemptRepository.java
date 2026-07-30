package com.languagelearning.language_learning_backend.quiz.repository;

import com.languagelearning.language_learning_backend.quiz.entity.QuizAttempt;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Page<QuizAttempt> findAllByUserIdOrderByCompletedAtDesc(Long userId, Pageable pageable);

    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);
}
