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

    // Auth-specific (Giai đoạn 2) — message chung chung cho sai username/password, cố tình
    // giống hệt nhau để không tiết lộ username có tồn tại hay không (11_FRS_TC_AUTH.md).
    public static final String AUTH_INVALID_CREDENTIALS = "Sai thông tin đăng nhập";
    public static final String AUTH_ACCOUNT_DISABLED = "Tài khoản đã bị vô hiệu hoá";
    public static final String AUTH_ACCOUNT_LOCKED = "Tài khoản đang bị khoá";
    public static final String AUTH_EMAIL_NOT_VERIFIED = "Vui lòng xác thực email trước khi đăng nhập";
    public static final String AUTH_TOKEN_EXPIRED = "Token đã hết hạn";
    public static final String AUTH_TOKEN_INVALID = "Token không hợp lệ";
    public static final String AUTH_TOKEN_ALREADY_USED = "Token đã được sử dụng hoặc hết hạn";
    public static final String AUTH_USERNAME_TAKEN = "Username đã tồn tại";
    public static final String AUTH_EMAIL_TAKEN = "Email đã tồn tại";
    public static final String AUTH_PASSWORD_MISMATCH = "Confirm password không khớp với password";
    public static final String AUTH_NEW_PASSWORD_SAME_AS_CURRENT = "Password mới không được trùng với password hiện tại";

    // Course/Lesson Progress (Giai đoạn 3)
    public static final String COURSE_NOT_ENROLLED = "Bạn chưa đăng ký khoá học này";

    // Quiz (Giai đoạn 4)
    public static final String QUIZ_ANSWER_OUT_OF_SCOPE = "Có câu trả lời không thuộc bài Quiz này";
}
