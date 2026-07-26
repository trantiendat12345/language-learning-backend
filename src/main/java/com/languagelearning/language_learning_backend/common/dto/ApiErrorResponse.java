package com.languagelearning.language_learning_backend.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cấu trúc response lỗi dùng chung cho mọi API - GlobalExceptionHandler là nơi duy nhất
 * tạo ra object này. Gồm HTTP status (code), mã lỗi nghiệp vụ cụ thể (errorCode) để Frontend
 * phân biệt các nguyên nhân khác nhau của cùng 1 HTTP status, message hiển thị cho người dùng,
 * và danh sách lỗi theo từng field (errors) khi là lỗi validate nhiều field cùng lúc.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private int code;
    private String errorCode;
    private String message;
    private List<FieldError> errors;

    /** Mô tả lỗi của 1 field cụ thể trong request, dùng khi validate thất bại nhiều field. */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
