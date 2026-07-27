package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.UnauthorizedException;

/**
 * Ném ra khi sai username/password lúc đăng nhập. Dùng chung 1 message cho cả 2 trường hợp
 * sai username và sai password để không lộ thông tin username có tồn tại hay không.
 */
public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super(ErrorCode.AUTH_INVALID_CREDENTIALS, ErrorMessage.AUTH_INVALID_CREDENTIALS);
    }
}
