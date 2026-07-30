package com.languagelearning.language_learning_backend.quiz.dto.response;

import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 1 câu hỏi trong danh sách trả về từ POST /api/quizzes/generate — không có đáp án đúng, không có explanation. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponse {

    private Long id;
    private QuestionType type;
    private String promptText;
    private String promptAudioUrl;
    private String promptImageUrl;
    private List<QuizOptionResponse> options;
}
