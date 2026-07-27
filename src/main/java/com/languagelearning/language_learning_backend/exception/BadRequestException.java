package com.languagelearning.language_learning_backend.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Ném ra khi request sai logic nghiệp vụ (khác lỗi validate field do Bean Validation tự bắt).
 * GlobalExceptionHandler trả về HTTP 400 kèm errorCode BAD_REQUEST.
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException() {
        this(ErrorMessage.BAD_REQUEST);
    }

    /**
     * Dùng cho exception con cần giữ nguyên HTTP 400 nhưng thay errorCode/message riêng
     * (vd confirmPassword không khớp password) thay vì luôn dùng errorCode BAD_REQUEST mặc định.
     */
    protected BadRequestException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
