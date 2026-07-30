package com.languagelearning.language_learning_backend.quiz.enums;

/**
 * MVP (docs/PROJECT_OVERVIEW.md mục 11) chỉ chấm điểm được MULTIPLE_CHOICE/FILL_BLANK/TYPING
 * (so khớp 1 đáp án đúng duy nhất trên QuestionOption). LISTENING/MATCHING/REORDER/
 * IMAGE_CHOICE/AUDIO_CHOICE thuộc "Quiz nâng cao" Phase 2 — Admin CRUD vẫn tạo được đủ 8 loại
 * (khớp ERD) nhưng QuizServiceImpl.submitQuiz chưa chấm điểm đúng cho 5 loại đó, xem comment tại đó.
 */
public enum QuestionType {
    MULTIPLE_CHOICE,
    FILL_BLANK,
    TYPING,
    LISTENING,
    MATCHING,
    REORDER,
    IMAGE_CHOICE,
    AUDIO_CHOICE
}
