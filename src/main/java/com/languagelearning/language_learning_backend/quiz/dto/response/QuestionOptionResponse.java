package com.languagelearning.language_learning_backend.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Admin-facing đầy đủ (có `correct`) — khác QuizOptionResponse dùng khi làm bài (ẩn đáp án đúng). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionResponse {

    private Long id;
    private String optionText;
    private String optionImageUrl;
    private boolean correct;
    private int displayOrder;
}
