package com.languagelearning.language_learning_backend.grammar.repository;

import com.languagelearning.language_learning_backend.grammar.entity.Grammar;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GrammarRepository extends JpaRepository<Grammar, Long>, JpaSpecificationExecutor<Grammar> {

    List<Grammar> findAllByLessonIdOrderByDisplayOrderAsc(Long lessonId);
}
