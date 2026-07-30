package com.languagelearning.language_learning_backend.quiz.dto.response;

import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Rút gọn cho GET /api/quizzes/attempts (lịch sử) — không có danh sách answers. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptSummaryResponse {

    private Long id;
    private QuestionSourceType sourceType;
    private Long sourceId;
    private int totalQuestions;
    private int correctAnswers;
    private float accuracy;
    private LocalDateTime completedAt;
}
