package com.languagelearning.language_learning_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Lớp cha cho mọi exception nghiệp vụ tự viết trong hệ thống. Mỗi exception con chỉ cần
 * cố định sẵn httpStatus + errorCode của riêng nó trong constructor; GlobalExceptionHandler
 * chỉ cần 1 handler chung cho lớp này để đọc lại 3 thông tin đó và trả về đúng ApiErrorResponse,
 * không phải sửa handler mỗi khi có thêm exception mới.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected BusinessException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
