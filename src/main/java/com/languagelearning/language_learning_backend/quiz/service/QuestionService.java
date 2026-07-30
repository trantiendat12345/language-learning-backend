package com.languagelearning.language_learning_backend.quiz.service;

import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionCreateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionUpdateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface QuestionService {

    PageResponse<QuestionSummaryResponse> getAllQuestionsForAdmin(Pageable pageable);

    QuestionResponse getQuestionByIdForAdmin(Long id);

    /** 400 nếu type cần đáp án đúng duy nhất (MULTIPLE_CHOICE/FILL_BLANK/TYPING/IMAGE_CHOICE/AUDIO_CHOICE) mà không có đúng 1 option correct=true. */
    QuestionResponse createQuestion(QuestionCreateRequest request);

    QuestionResponse updateQuestion(Long id, QuestionUpdateRequest request);

    void deleteQuestion(Long id);
}
