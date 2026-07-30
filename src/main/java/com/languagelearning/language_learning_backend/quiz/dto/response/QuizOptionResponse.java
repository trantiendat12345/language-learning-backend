package com.languagelearning.language_learning_backend.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Dùng khi làm bài (GET quiz đã generate) — KHÔNG có `correct`, tránh lộ đáp án đúng cho client (TC-QUIZ-004). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionResponse {

    private Long id;
    private String optionText;
    private String optionImageUrl;
}
