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
}
