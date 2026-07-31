package com.languagelearning.language_learning_backend.favorite.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.favorite.enums.FavoriteTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteCreateRequest {

    @NotNull(message = ValidationMessage.FAVORITE_TARGET_TYPE_REQUIRED)
    private FavoriteTargetType targetType;

    @NotNull(message = ValidationMessage.FAVORITE_TARGET_ID_REQUIRED)
    private Long targetId;
}
