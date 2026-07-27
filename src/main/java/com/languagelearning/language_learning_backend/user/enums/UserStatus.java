package com.languagelearning.language_learning_backend.user.enums;

/** Trạng thái tài khoản, ảnh hưởng trực tiếp tới việc đăng nhập được hay không. */
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    DISABLED,
    LOCKED
}
