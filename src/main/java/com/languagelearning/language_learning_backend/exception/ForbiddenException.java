package com.languagelearning.language_learning_backend.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Ném ra khi user đã đăng nhập hợp lệ nhưng không đủ quyền thực hiện thao tác
 * (sai role, hoặc không phải chủ sở hữu dữ liệu — xem quy tắc ownership ở CLAUDE.md).
 * GlobalExceptionHandler trả về HTTP 403 kèm errorCode FORBIDDEN.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException() {
        this(ErrorMessage.FORBIDDEN);
    }
}
