package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.DuplicateResourceException;

/**
 * Ném ra khi đăng ký với username đã tồn tại.
 */
public class UsernameTakenException extends DuplicateResourceException {

    public UsernameTakenException() {
        super(ErrorCode.AUTH_USERNAME_TAKEN, ErrorMessage.AUTH_USERNAME_TAKEN);
    }
}
