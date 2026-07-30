package com.languagelearning.language_learning_backend.quiz.dto.response;

import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Đầy đủ - trả về ngay sau khi nộp bài (POST) và khi xem chi tiết 1 attempt (GET /{id}). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptResponse {

    private Long id;
    private QuestionSourceType sourceType;
    private Long sourceId;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private float score;
    private float accuracy;
    private int durationSeconds;
    private int xpEarned;
    private LocalDateTime completedAt;
    private List<QuizAttemptAnswerResponse> answers;
}
