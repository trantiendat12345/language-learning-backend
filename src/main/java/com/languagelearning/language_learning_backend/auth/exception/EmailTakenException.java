package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;

/**
 * Ném ra khi đăng ký với email đã tồn tại.
 */
public class EmailTakenException extends DuplicateResourceException {

    public EmailTakenException() {
        super(ErrorCode.AUTH_EMAIL_TAKEN, ErrorMessage.AUTH_EMAIL_TAKEN);
    }
}
