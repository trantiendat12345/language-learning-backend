package com.languagelearning.language_learning_backend.common.constant;

/**
 * Tap hop toan bo ma loi (errorCode) dung trong ApiErrorResponse tren toan he thong.
 * Moi hang so o day phai khop chinh xac voi bang tai docs/dev/ERROR_CODE_CATALOG.md -
 * khi them 1 errorCode moi, them dong tuong ung vao file do trong cung thay doi.
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    // Auth-specific (Giai đoạn 2)
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_ACCOUNT_DISABLED = "AUTH_ACCOUNT_DISABLED";
    public static final String AUTH_ACCOUNT_LOCKED = "AUTH_ACCOUNT_LOCKED";
    public static final String AUTH_EMAIL_NOT_VERIFIED = "AUTH_EMAIL_NOT_VERIFIED";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
    public static final String AUTH_TOKEN_INVALID = "AUTH_TOKEN_INVALID";
    public static final String AUTH_TOKEN_ALREADY_USED = "AUTH_TOKEN_ALREADY_USED";
    public static final String AUTH_USERNAME_TAKEN = "AUTH_USERNAME_TAKEN";
    public static final String AUTH_EMAIL_TAKEN = "AUTH_EMAIL_TAKEN";
    public static final String AUTH_PASSWORD_MISMATCH = "AUTH_PASSWORD_MISMATCH";
}
