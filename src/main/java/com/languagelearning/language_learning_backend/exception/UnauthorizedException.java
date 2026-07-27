package com.languagelearning.language_learning_backend.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Ném ra khi request không có token hoặc token không hợp lệ/hết hạn.
 * GlobalExceptionHandler trả về HTTP 401 kèm errorCode UNAUTHORIZED.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        this(ErrorMessage.UNAUTHORIZED);
    }

    /**
     * Dùng cho exception con cần giữ nguyên HTTP 401 nhưng thay errorCode/message riêng
     * (vd sai mật khẩu, tài khoản bị khoá) thay vì luôn dùng errorCode UNAUTHORIZED mặc định.
     */
    protected UnauthorizedException(String errorCode, String message) {
        super(HttpStatus.UNAUTHORIZED, errorCode, message);
    }
}
