package com.languagelearning.language_learning_backend.lesson.repository;

import com.languagelearning.language_learning_backend.lesson.entity.LessonVocabulary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonVocabularyRepository extends JpaRepository<LessonVocabulary, Long> {

    List<LessonVocabulary> findAllByLessonIdOrderByDisplayOrderAsc(Long lessonId);

    boolean existsByLessonIdAndVocabularyId(Long lessonId, Long vocabularyId);

    Optional<LessonVocabulary> findByLessonIdAndVocabularyId(Long lessonId, Long vocabularyId);
}
