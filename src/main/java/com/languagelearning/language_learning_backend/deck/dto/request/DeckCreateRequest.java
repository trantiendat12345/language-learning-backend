package com.languagelearning.language_learning_backend.deck.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** ownerId luôn lấy từ SecurityContext (không nằm trong body). visibility bỏ trống = mặc định PRIVATE. */
@Getter
@Setter
public class DeckCreateRequest {

    @NotNull(message = ValidationMessage.DECK_LANGUAGE_ID_REQUIRED)
    private Long languageId;

    @NotBlank(message = ValidationMessage.DECK_TITLE_REQUIRED)
    @Size(max = 200, message = ValidationMessage.DECK_TITLE_SIZE)
    private String title;

    private String description;

    @Size(max = 500, message = ValidationMessage.DECK_COVER_IMAGE_URL_SIZE)
    private String coverImageUrl;

    private DeckVisibility visibility;
}
