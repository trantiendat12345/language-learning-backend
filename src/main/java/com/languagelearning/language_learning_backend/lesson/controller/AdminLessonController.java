package com.languagelearning.language_learning_backend.lesson.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarCreateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarSummaryResponse;
import com.languagelearning.language_learning_backend.grammar.service.GrammarService;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonUpdateRequest;
import com.languagelearning.language_learning_backend.lesson.dto.request.LessonVocabularyAttachRequest;
import com.languagelearning.language_learning_backend.lesson.dto.response.LessonResponse;
import com.languagelearning.language_learning_backend.lesson.service.LessonService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quản trị 1 Lesson theo id trực tiếp, cộng thêm quản lý Vocabulary/Grammar gắn vào Lesson đó
 * (nested resource, giống cách AdminCourseController quản lý Lesson lồng trong Course) - bắt
 * buộc role ADMIN. Tạo Lesson mới xem AdminCourseController.
 */
@RestController
@RequestMapping("/api/admin/lessons")
@RequiredArgsConstructor
public class AdminLessonController {

    private final LessonService lessonService;
    private final GrammarService grammarService;

    @GetMapping("/{id}")
    public ApiResponse<LessonResponse> getLessonById(@PathVariable Long id) {
        return ApiResponse.success(lessonService.getLessonByIdForAdmin(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<LessonResponse> updateLesson(@PathVariable Long id, @Valid @RequestBody LessonUpdateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, lessonService.updateLesson(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }

    @PostMapping("/{lessonId}/vocabularies")
    public ApiResponse<Void> attachVocabulary(
            @PathVariable Long lessonId, @Valid @RequestBody LessonVocabularyAttachRequest request) {
        lessonService.attachVocabularyToLesson(lessonId, request);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }

    @DeleteMapping("/{lessonId}/vocabularies/{vocabularyId}")
    public ApiResponse<Void> detachVocabulary(@PathVariable Long lessonId, @PathVariable Long vocabularyId) {
        lessonService.detachVocabularyFromLesson(lessonId, vocabularyId);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }

    @PostMapping("/{lessonId}/grammars")
    public ApiResponse<GrammarResponse> createGrammar(
            @PathVariable Long lessonId, @Valid @RequestBody GrammarCreateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, grammarService.createGrammar(lessonId, request));
    }

    @GetMapping("/{lessonId}/grammars")
    public ApiResponse<List<GrammarSummaryResponse>> getGrammarsByLesson(@PathVariable Long lessonId) {
        return ApiResponse.success(grammarService.getGrammarsByLessonForAdmin(lessonId));
    }
}
