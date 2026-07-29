package com.languagelearning.language_learning_backend.grammar.repository;

import com.languagelearning.language_learning_backend.grammar.entity.GrammarExample;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarExampleRepository extends JpaRepository<GrammarExample, Long> {

    List<GrammarExample> findAllByGrammarId(Long grammarId);
}
