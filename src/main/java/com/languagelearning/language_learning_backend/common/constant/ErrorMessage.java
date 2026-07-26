package com.languagelearning.language_learning_backend.common.constant;

/**
 * Tap hop cac message loi mac dinh hien thi cho nguoi dung (tieng Viet), dung chung
 * cho exception va GlobalExceptionHandler. Gom vao 1 noi de sua noi dung chi can sua
 * 1 cho, tranh moi noi tu viet 1 kieu cau khac nhau cho cung 1 loai loi.
 */
public final class ErrorMessage {

    private ErrorMessage() {
    }

    public static final String RESOURCE_NOT_FOUND = "Không tìm thấy dữ liệu";
    public static final String BAD_REQUEST = "Yêu cầu không hợp lệ";
    public static final String VALIDATION_ERROR = "Dữ liệu không hợp lệ";
    public static final String UNAUTHORIZED = "Vui lòng đăng nhập";
    public static final String FORBIDDEN = "Bạn không có quyền thực hiện thao tác này";
    public static final String DUPLICATE_RESOURCE = "Dữ liệu đã tồn tại";
    public static final String INTERNAL_ERROR = "Đã có lỗi xảy ra, vui lòng thử lại sau";
}
