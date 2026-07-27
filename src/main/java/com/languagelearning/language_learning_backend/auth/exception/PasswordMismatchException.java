package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.BadRequestException;

/**
 * Ném ra khi đăng ký với confirmPassword không khớp password.
 */
public class PasswordMismatchException extends BadRequestException {

    public PasswordMismatchException() {
        super(ErrorCode.AUTH_PASSWORD_MISMATCH, ErrorMessage.AUTH_PASSWORD_MISMATCH);
    }
}
