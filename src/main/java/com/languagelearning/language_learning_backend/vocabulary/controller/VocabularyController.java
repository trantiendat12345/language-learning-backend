package com.languagelearning.language_learning_backend.vocabulary.controller;

import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.common.dto.PageResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularyResponse;
import com.languagelearning.language_learning_backend.vocabulary.dto.response.VocabularySummaryResponse;
import com.languagelearning.language_learning_backend.vocabulary.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint public - permitAll trong SecurityConfig (chỉ GET), chỉ trả từ hệ thống ACTIVE. */
@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ApiResponse<PageResponse<VocabularySummaryResponse>> getVocabularies(
            @RequestParam(required = false) Long languageId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(vocabularyService.getSystemVocabularies(languageId, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<VocabularyResponse> getVocabularyById(@PathVariable Long id) {
        return ApiResponse.success(vocabularyService.getSystemVocabularyById(id));
    }
}
