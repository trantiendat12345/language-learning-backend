package com.languagelearning.language_learning_backend.quiz.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** requestedCount &gt; actualCount = nguồn không đủ câu theo yêu cầu (TC-QUIZ-003) — FE tự hiển thị cảnh báo dựa vào 2 field này. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizGenerateResponse {

    private List<QuizQuestionResponse> questions;
    private int requestedCount;
    private int actualCount;
}
