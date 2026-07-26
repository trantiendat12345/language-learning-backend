package com.languagelearning.language_learning_backend.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Ném ra khi Service tìm 1 bản ghi theo id nhưng không tồn tại (hoặc đã bị soft-delete).
 * GlobalExceptionHandler bắt exception này và trả về HTTP 404 kèm errorCode RESOURCE_NOT_FOUND.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException() {
        this(ErrorMessage.RESOURCE_NOT_FOUND);
    }
}
