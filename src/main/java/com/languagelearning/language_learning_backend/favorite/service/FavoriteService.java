package com.languagelearning.language_learning_backend.favorite.service;

import com.languagelearning.language_learning_backend.favorite.dto.request.FavoriteCreateRequest;
import com.languagelearning.language_learning_backend.favorite.dto.response.FavoriteResponse;
import java.util.List;

public interface FavoriteService {

    /**
     * Danh sách Favorite của currentUser, sắp xếp mới nhất trước. Đối tượng gốc đã bị xoá mềm
     * hoặc không còn tồn tại được ẩn khỏi kết quả (không crash) - xem TC-FAV-009.
     */
    List<FavoriteResponse> getMyFavorites(Long userId);

    /**
     * Idempotent - favorite trùng (userId, targetType, targetId) trả về bản ghi đã có, không
     * tạo trùng (quyết định chốt khi code, xem TC-FAV-002). Deck PRIVATE không phải của
     * currentUser -> 404 (không tiết lộ tồn tại, cùng quy tắc `DeckServiceImpl.getDeckById`).
     */
    FavoriteResponse addFavorite(Long userId, FavoriteCreateRequest request);

    /** Ownership check - chỉ chủ sở hữu Favorite mới xoá được. */
    void removeFavorite(Long userId, Long favoriteId);
}
