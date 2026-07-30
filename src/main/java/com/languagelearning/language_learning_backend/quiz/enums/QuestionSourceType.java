package com.languagelearning.language_learning_backend.quiz.enums;

/**
 * Nguồn sinh câu hỏi (D4/D5 — Question là ngân hàng câu hỏi dùng chung, không có Exercise/Quiz
 * tĩnh riêng). Chunk hiện tại chỉ thực sự hỗ trợ generate/submit Quiz cho `LESSON` — COURSE/DECK
 * (Deck chưa tồn tại, Giai đoạn 5)/VOCAB_LIST để dành khi các module đó xây xong, xem
 * QuizServiceImpl.
 */
public enum QuestionSourceType {
    LESSON,
    COURSE,
    DECK,
    VOCAB_LIST
}
