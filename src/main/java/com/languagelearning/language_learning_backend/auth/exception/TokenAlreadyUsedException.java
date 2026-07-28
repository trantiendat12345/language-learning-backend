package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.BadRequestException;

/**
 * Ném ra khi Verification Token (EMAIL_VERIFY/PASSWORD_RESET) đã có `usedAt`, bị dùng lại.
 */
public class TokenAlreadyUsedException extends BadRequestException {

    public TokenAlreadyUsedException() {
        super(ErrorCode.AUTH_TOKEN_ALREADY_USED, ErrorMessage.AUTH_TOKEN_ALREADY_USED);
    }
}
