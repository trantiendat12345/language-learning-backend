package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.UnauthorizedException;

/**
 * Ném ra khi Refresh Token/Verification Token đã quá hạn `expiresAt`.
 */
public class TokenExpiredException extends UnauthorizedException {

    public TokenExpiredException() {
        super(ErrorCode.AUTH_TOKEN_EXPIRED, ErrorMessage.AUTH_TOKEN_EXPIRED);
    }
}
