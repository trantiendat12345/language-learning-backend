package com.languagelearning.language_learning_backend.quiz.repository;

import com.languagelearning.language_learning_backend.quiz.entity.Question;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findAllBySourceTypeAndSourceId(QuestionSourceType sourceType, Long sourceId);
}
