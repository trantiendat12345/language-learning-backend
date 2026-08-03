package com.languagelearning.language_learning_backend.language.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import com.languagelearning.language_learning_backend.language.enums.LanguageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu Admin gửi lên khi sửa Language (PUT /api/admin/languages/{id}). Không có `code` -
 * xem lý do ở LanguageCreateRequest.
 */
@Getter
@Setter
public class LanguageUpdateRequest {

    @NotBlank(message = ValidationMessage.LANGUAGE_NAME_REQUIRED)
    @Size(max = 100, message = ValidationMessage.LANGUAGE_NAME_SIZE)
    private String name;

    @Size(max = 500, message = ValidationMessage.LANGUAGE_FLAG_ICON_URL_SIZE)
    @SafeUrl
    private String flagIconUrl;

    @NotNull(message = ValidationMessage.LANGUAGE_STATUS_REQUIRED)
    private LanguageStatus status;
}
