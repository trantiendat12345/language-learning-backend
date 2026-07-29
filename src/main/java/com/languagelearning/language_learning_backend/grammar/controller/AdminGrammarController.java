package com.languagelearning.language_learning_backend.grammar.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.grammar.dto.request.GrammarUpdateRequest;
import com.languagelearning.language_learning_backend.grammar.dto.response.GrammarResponse;
import com.languagelearning.language_learning_backend.grammar.service.GrammarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Quản trị 1 Grammar theo id trực tiếp - bắt buộc role ADMIN. Tạo Grammar mới xem AdminLessonController. */
@RestController
@RequestMapping("/api/admin/grammars")
@RequiredArgsConstructor
public class AdminGrammarController {

    private final GrammarService grammarService;

    @GetMapping("/{id}")
    public ApiResponse<GrammarResponse> getGrammarById(@PathVariable Long id) {
        return ApiResponse.success(grammarService.getGrammarByIdForAdmin(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<GrammarResponse> updateGrammar(@PathVariable Long id, @Valid @RequestBody GrammarUpdateRequest request) {
        return ApiResponse.success(CommonMessage.SUCCESS, grammarService.updateGrammar(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGrammar(@PathVariable Long id) {
        grammarService.deleteGrammar(id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }
}
