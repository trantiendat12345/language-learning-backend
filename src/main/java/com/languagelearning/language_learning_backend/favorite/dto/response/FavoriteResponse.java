package com.languagelearning.language_learning_backend.favorite.dto.response;

import com.languagelearning.language_learning_backend.favorite.enums.FavoriteTargetType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** title/imageUrl resolve từ entity gốc theo targetType tại thời điểm gọi API (không denormalize - D1). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {

    private Long id;
    private FavoriteTargetType targetType;
    private Long targetId;
    private String title;
    private String imageUrl;
    private LocalDateTime favoritedAt;
}
