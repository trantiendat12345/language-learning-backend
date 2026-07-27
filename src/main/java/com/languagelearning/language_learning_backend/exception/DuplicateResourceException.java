package com.languagelearning.language_learning_backend.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Ném ra khi Service phát hiện vi phạm ràng buộc duy nhất ở tầng nghiệp vụ
 * (vd username/email đã tồn tại) trước khi request chạm tới DB.
 * GlobalExceptionHandler trả về HTTP 409 kèm errorCode DUPLICATE_RESOURCE.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public DuplicateResourceException() {
        this(ErrorMessage.DUPLICATE_RESOURCE);
    }

    /**
     * Dùng cho exception con cần giữ nguyên HTTP 409 nhưng thay errorCode/message riêng
     * (vd username đã tồn tại, email đã tồn tại) thay vì luôn dùng errorCode DUPLICATE_RESOURCE mặc định.
     */
    protected DuplicateResourceException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
