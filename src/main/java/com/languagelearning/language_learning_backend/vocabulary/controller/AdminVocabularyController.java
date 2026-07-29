package com.languagelearning.language_learning_backend.vocabulary.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyCreateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.request.VocabularyUpdateRequest;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularyResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularySummaryResponse;
import com.languagelearning.language_learning_backend.vocabulary.service.VocabularyService;
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

/** Quản trị Vocabulary hệ thống - bắt buộc role ADMIN. */
@RestController
@RequestMapping("/api/admin/vocabularies")
@RequiredArgsConstructor
public class AdminVocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ApiResponse<PageResponse<VocabularySummaryResponse>> getAllVocabularies(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(vocabularyService.getAllVocabulariesForAdmin(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<VocabularyResponse> getVocabularyById(@PathVariable Long id) {
        return ApiResponse.success(vocabularyService.getVocabularyByIdForAdmin(id));
    }

    @PostMapping
    public ApiResponse<VocabularyResponse> createVocabulary(@Valid @RequestBody VocabularyCreateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, vocabularyService.createVocabulary(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<VocabularyResponse> updateVocabulary(
            @PathVariable Long id, @Valid @RequestBody VocabularyUpdateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, vocabularyService.updateVocabulary(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVocabulary(@PathVariable Long id) {
        vocabularyService.deleteVocabulary(id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }
}
