package com.languagelearning.language_learning_backend.quiz.repository;

import com.languagelearning.language_learning_backend.quiz.entity.QuizAttemptAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Long> {

    List<QuizAttemptAnswer> findAllByQuizAttemptIdOrderByDisplayOrderAsc(Long quizAttemptId);
}
