package com.languagelearning.language_learning_backend.quiz.dto.response;

import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Chi tiết 1 câu sau khi nộp bài — đã lộ đáp án đúng + explanation (khác lúc generate). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptAnswerResponse {

    private Long questionId;
    private QuestionType type;
    private String promptText;
    private String explanation;
    private Long selectedOptionId;
    private String typedAnswer;
    private Long correctOptionId;
    private boolean correct;
}
