package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.BadRequestException;

/**
 * Ném ra khi đổi mật khẩu (PUT /api/users/me/password) nhưng newPassword trùng
 * currentPassword — quyết định chốt khi code cho mục "cần xác nhận" ở
 * docs/testing/12_FRS_TC_USER_PROFILE.md mục 1.3.
 */
public class NewPasswordSameAsCurrentException extends BadRequestException {

    public NewPasswordSameAsCurrentException() {
        super(ErrorCode.AUTH_NEW_PASSWORD_SAME_AS_CURRENT, ErrorMessage.AUTH_NEW_PASSWORD_SAME_AS_CURRENT);
    }
}
