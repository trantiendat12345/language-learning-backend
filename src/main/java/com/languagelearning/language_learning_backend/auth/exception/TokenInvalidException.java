package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.UnauthorizedException;

/**
 * Ném ra khi Refresh Token/Verification Token không tồn tại, sai định dạng, hoặc (với
 * Refresh Token) đã bị revoke.
 */
public class TokenInvalidException extends UnauthorizedException {

    public TokenInvalidException() {
        super(ErrorCode.AUTH_TOKEN_INVALID, ErrorMessage.AUTH_TOKEN_INVALID);
    }
}
