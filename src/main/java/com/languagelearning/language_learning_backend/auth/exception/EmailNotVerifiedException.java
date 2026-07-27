package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.UnauthorizedException;

/**
 * Ném ra khi đăng nhập bằng tài khoản có status PENDING_VERIFICATION (chưa xác thực email).
 */
public class EmailNotVerifiedException extends UnauthorizedException {

    public EmailNotVerifiedException() {
        super(ErrorCode.AUTH_EMAIL_NOT_VERIFIED, ErrorMessage.AUTH_EMAIL_NOT_VERIFIED);
    }
}
