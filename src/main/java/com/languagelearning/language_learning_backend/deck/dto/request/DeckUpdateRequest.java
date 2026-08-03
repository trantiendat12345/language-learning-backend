package com.languagelearning.language_learning_backend.deck.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeckUpdateRequest {

    @NotBlank(message = ValidationMessage.DECK_TITLE_REQUIRED)
    @Size(max = 200, message = ValidationMessage.DECK_TITLE_SIZE)
    private String title;

    private String description;

    @Size(max = 500, message = ValidationMessage.DECK_COVER_IMAGE_URL_SIZE)
    @SafeUrl
    private String coverImageUrl;

    @NotNull(message = ValidationMessage.DECK_VISIBILITY_REQUIRED)
    private DeckVisibility visibility;

    @NotNull(message = ValidationMessage.DECK_STATUS_REQUIRED)
    private DeckStatus status;
}
