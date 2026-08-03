package com.languagelearning.language_learning_backend.search.controller;

import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.search.dto.response.SearchResponse;
import com.languagelearning.language_learning_backend.search.enums.SearchResultType;
import com.languagelearning.language_learning_backend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** GET permitAll trong SecurityConfig - tìm kiếm nội dung public, không cần đăng nhập. */
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/search")
    public ApiResponse<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) SearchResultType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(searchService.search(q, type, pageable));
    }
}
