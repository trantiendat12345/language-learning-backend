package com.languagelearning.language_learning_backend.quiz.service;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizGenerateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuizSubmitRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizAttemptSummaryResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuizGenerateResponse;
import org.springframework.data.domain.Pageable;

public interface QuizService {

    /**
     * 404 nếu nguồn không tồn tại/không PUBLISHED. Chunk hiện tại chỉ hỗ trợ sourceType=LESSON
     * — COURSE/DECK/VOCAB_LIST trả 400 (Deck chưa tồn tại tới Giai đoạn 5).
     */
    QuizGenerateResponse generateQuiz(QuizGenerateRequest request);

    /** 400 QUIZ_ANSWER_OUT_OF_SCOPE nếu có questionId không thuộc đúng sourceType/sourceId đã khai báo. */
    QuizAttemptResponse submitQuiz(QuizSubmitRequest request, Long userId);

    PageResponse<QuizAttemptSummaryResponse> getMyQuizAttempts(Long userId, Pageable pageable);

    /** 404 nếu attempt không tồn tại hoặc không thuộc currentUserId (không tiết lộ tồn tại của attempt người khác). */
    QuizAttemptResponse getMyQuizAttemptById(Long id, Long userId);
}
