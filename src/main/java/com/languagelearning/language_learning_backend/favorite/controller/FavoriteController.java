package com.languagelearning.language_learning_backend.favorite.controller;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiResponse;
import com.languagelearning.language_learning_backend.favorite.dto.request.FavoriteCreateRequest;
import com.languagelearning.language_learning_backend.favorite.dto.response.FavoriteResponse;
import com.languagelearning.language_learning_backend.favorite.service.FavoriteService;
import com.languagelearning.language_learning_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Toàn bộ endpoint protected (mặc định anyRequest().authenticated(), không cần khai báo riêng SecurityConfig). */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ApiResponse<List<FavoriteResponse>> getMyFavorites(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(favoriteService.getMyFavorites(currentUser.getUserId()));
    }

    @PostMapping
    public ApiResponse<FavoriteResponse> addFavorite(
            @Valid @RequestBody FavoriteCreateRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.success(CommonMessage.SUCCESS, favoriteService.addFavorite(currentUser.getUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeFavorite(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        favoriteService.removeFavorite(currentUser.getUserId(), id);
        return ApiResponse.success(CommonMessage.SUCCESS, null);
    }
}
