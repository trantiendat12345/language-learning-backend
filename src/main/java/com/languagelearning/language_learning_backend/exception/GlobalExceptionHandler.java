package com.languagelearning.language_learning_backend.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import com.languagelearning.language_learning_backend.common.dto.ApiErrorResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Nơi duy nhất chuyển exception thành ApiErrorResponse cho toàn bộ API - Controller
 * không tự try/catch để tự trả lỗi riêng lẻ (xem CLAUDE.md mục "Quy tắc bắt buộc").
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt mọi exception nghiệp vụ tự viết (ResourceNotFoundException, ForbiddenException...),
     * đọc lại httpStatus/errorCode/message đã cố định sẵn trong từng exception con để trả về.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .code(ex.getHttpStatus().value())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    /**
     * Bắt lỗi validate của Bean Validation (@Valid trên Request DTO), gom toàn bộ field
     * lỗi thành danh sách errors[] để Frontend hiển thị lỗi ngay tại từng field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ApiErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        ApiErrorResponse body = ApiErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message(ErrorMessage.VALIDATION_ERROR)
                .errors(fieldErrors)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Bắt mọi exception chưa lường trước (bug, lỗi hạ tầng...). Log đầy đủ stack trace
     * phía server để debug, nhưng KHÔNG bao giờ trả chi tiết lỗi thật ra response
     * (tránh lộ thông tin nội bộ - xem docs/PROJECT_OVERVIEW.md mục 10.6).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error", ex);
        ApiErrorResponse body = ApiErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .message(ErrorMessage.INTERNAL_ERROR)
                .build();
        return ResponseEntity.internalServerError().body(body);
    }
}
