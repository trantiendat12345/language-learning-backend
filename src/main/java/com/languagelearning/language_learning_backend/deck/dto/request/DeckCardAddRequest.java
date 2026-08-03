package com.languagelearning.language_learning_backend.deck.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 2 chế độ loại trừ nhau (TC-DECK-008/010): (1) `vocabularyId` có giá trị — dùng từ có sẵn
 * trong hệ thống; (2) `vocabularyId` để trống, bắt buộc có `word`+`meaning` — tạo
 * `Vocabulary(ownerId=currentUserId)` mới rồi liên kết. Validate 2 chế độ này ở Service (cross-field,
 * không làm được bằng Bean Validation thuần).
 */
@Getter
@Setter
public class DeckCardAddRequest {

    private Long vocabularyId;

    @Size(max = 200, message = ValidationMessage.DECK_CARD_WORD_SIZE)
    private String word;

    @Size(max = 500, message = ValidationMessage.DECK_CARD_MEANING_SIZE)
    private String meaning;

    @Size(max = 100, message = ValidationMessage.DECK_CARD_IPA_SIZE)
    private String ipa;

    @Size(max = 500, message = ValidationMessage.DECK_CARD_IMAGE_URL_SIZE)
    @SafeUrl
    private String imageUrl;

    private String exampleSentence;

    private String exampleTranslation;

    @Min(value = 0, message = ValidationMessage.DECK_CARD_DISPLAY_ORDER_MIN)
    private int displayOrder;
}
