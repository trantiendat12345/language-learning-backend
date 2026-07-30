package com.languagelearning.language_learning_backend.quiz.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionCreateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.request.QuestionUpdateRequest;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionResponse;
import com.languagelearning.language_learning_backend.quiz.dto.response.QuestionSummaryResponse;
import com.languagelearning.language_learning_backend.quiz.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Quản trị ngân hàng Question - bắt buộc role ADMIN. Không có endpoint public riêng, Question chỉ lộ ra qua Quiz generate (đã ẩn đáp án đúng). */
@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ApiResponse<PageResponse<QuestionSummaryResponse>> getAllQuestions(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(questionService.getAllQuestionsForAdmin(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionResponse> getQuestionById(@PathVariable Long id) {
        return ApiResponse.success(questionService.getQuestionByIdForAdmin(id));
    }

    @PostMapping
    public ApiResponse<QuestionResponse> createQuestion(@Valid @RequestBody QuestionCreateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, questionService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionResponse> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionUpdateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }
}
