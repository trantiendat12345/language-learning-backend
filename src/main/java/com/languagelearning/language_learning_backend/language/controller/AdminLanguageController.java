package com.languagelearning.language_learning_backend.language.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.language.dto.request.LanguageCreateRequest;
import com.languagelearning.language_learning_backend.language.dto.request.LanguageUpdateRequest;
import com.languagelearning.language_learning_backend.language.dto.response.LanguageResponse;
import com.languagelearning.language_learning_backend.language.service.LanguageService;
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

/** Quản trị Language - bắt buộc role ADMIN (SecurityConfig: `/api/admin/**` -> hasRole("ADMIN")). */
@RestController
@RequestMapping("/api/admin/languages")
@RequiredArgsConstructor
public class AdminLanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ApiResponse<List<LanguageResponse>> getAllLanguages() {
        return ApiResponse.success(languageService.getAllLanguagesForAdmin());
    }

    @GetMapping("/{id}")
    public ApiResponse<LanguageResponse> getLanguageById(@PathVariable Long id) {
        return ApiResponse.success(languageService.getLanguageByIdForAdmin(id));
    }

    @PostMapping
    public ApiResponse<LanguageResponse> createLanguage(@Valid @RequestBody LanguageCreateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, languageService.createLanguage(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<LanguageResponse> updateLanguage(
            @PathVariable Long id, @Valid @RequestBody LanguageUpdateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, languageService.updateLanguage(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLanguage(@PathVariable Long id) {
        languageService.deleteLanguage(id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }
}
