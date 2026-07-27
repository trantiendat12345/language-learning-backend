package com.languagelearning.language_learning_backend.auth.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.exception.UnauthorizedException;

/**
 * Ném ra khi đăng nhập bằng tài khoản có status LOCKED.
 */
public class AccountLockedException extends UnauthorizedException {

    public AccountLockedException() {
        super(ErrorCode.AUTH_ACCOUNT_LOCKED, ErrorMessage.AUTH_ACCOUNT_LOCKED);
    }
}
