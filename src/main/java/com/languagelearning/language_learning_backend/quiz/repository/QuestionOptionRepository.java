package com.languagelearning.language_learning_backend.quiz.repository;

import com.languagelearning.language_learning_backend.quiz.entity.QuestionOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findAllByQuestionId(Long questionId);

    List<QuestionOption> findAllByQuestionIdIn(List<Long> questionIds);
}
