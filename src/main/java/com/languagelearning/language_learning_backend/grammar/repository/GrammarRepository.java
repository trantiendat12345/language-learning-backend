package com.languagelearning.language_learning_backend.grammar.repository;

import com.languagelearning.language_learning_backend.grammar.entity.Grammar;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarRepository extends JpaRepository<Grammar, Long> {

    List<Grammar> findAllByLessonIdOrderByDisplayOrderAsc(Long lessonId);
}
