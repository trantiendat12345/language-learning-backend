package com.languagelearning.language_learning_backend.common.dto;

import com.languagelearning.language_learning_backend.common.constant.CommonMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cấu trúc response thành công dùng chung cho mọi API - Controller trả về kiểu này
 * thay vì trả thẳng Entity/DTO trần (xem CLAUDE.md mục "Quy tắc bắt buộc" #8).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    /** Tạo response thành công với message mặc định "Success". */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(CommonMessage.SUCCESS)
                .data(data)
                .build();
    }

    /** Tạo response thành công với message tuỳ chỉnh (vd "Đăng ký thành công"). */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }
}
