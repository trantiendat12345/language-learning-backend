package com.languagelearning.language_learning_backend.language.controller;

import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.language.dto.response.LanguageResponse;
import com.languagelearning.language_learning_backend.language.service.LanguageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint public - permitAll trong SecurityConfig. */
@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ApiResponse<List<LanguageResponse>> getActiveLanguages() {
        return ApiResponse.success(languageService.getActiveLanguages());
    }
}
