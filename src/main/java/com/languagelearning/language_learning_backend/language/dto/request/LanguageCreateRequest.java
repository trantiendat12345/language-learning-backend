package com.languagelearning.language_learning_backend.language.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.common.validation.SafeUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu Admin gửi lên khi tạo Language mới (POST /api/admin/languages). `code` chỉ set
 * được lúc tạo — không cho sửa qua LanguageUpdateRequest vì Course/Vocabulary tham chiếu
 * theo `languageId` (FK), không theo `code`, nhưng đổi `code` sau khi đã dùng vẫn dễ gây
 * nhầm lẫn cho dữ liệu/tài liệu tham chiếu bằng code (vd test data, API filter).
 */
@Getter
@Setter
public class LanguageCreateRequest {

    @NotBlank(message = ValidationMessage.LANGUAGE_CODE_REQUIRED)
    @Size(max = 10, message = ValidationMessage.LANGUAGE_CODE_SIZE)
    private String code;

    @NotBlank(message = ValidationMessage.LANGUAGE_NAME_REQUIRED)
    @Size(max = 100, message = ValidationMessage.LANGUAGE_NAME_SIZE)
    private String name;

    @Size(max = 500, message = ValidationMessage.LANGUAGE_FLAG_ICON_URL_SIZE)
    @SafeUrl
    private String flagIconUrl;
}
