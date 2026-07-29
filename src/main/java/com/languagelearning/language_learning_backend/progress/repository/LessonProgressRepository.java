package com.languagelearning.language_learning_backend.progress.repository;

import com.languagelearning.language_learning_backend.progress.entity.LessonProgress;
import com.languagelearning.language_learning_backend.progress.enums.LessonProgressStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    long countByUserIdAndStatusAndLessonIdIn(Long userId, LessonProgressStatus status, List<Long> lessonIds);
}
