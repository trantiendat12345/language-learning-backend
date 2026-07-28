package com.languagelearning.language_learning_backend.common.constant;

/**
 * Message hiển thị cho từng field khi Bean Validation (@NotBlank, @Size...) trên Request DTO
 * bắt lỗi - gán vào thuộc tính message của annotation thay vì viết chuỗi trực tiếp tại DTO,
 * để tránh 1 câu báo lỗi bị viết nhiều kiểu khác nhau ở nhiều DTO.
 */
public final class ValidationMessage {

    private ValidationMessage() {
    }

    // Auth - Register/Login (Giai đoạn 2)
    public static final String USERNAME_REQUIRED = "Username không được để trống";
    public static final String USERNAME_SIZE = "Username phải từ 3 đến 50 ký tự";
    public static final String USERNAME_NO_WHITESPACE = "Username không được chứa khoảng trắng";
    public static final String EMAIL_REQUIRED = "Email không được để trống";
    public static final String EMAIL_INVALID = "Email không đúng định dạng";
    public static final String PASSWORD_REQUIRED = "Password không được để trống";
    public static final String PASSWORD_PATTERN =
            "Password phải từ 8 ký tự trở lên, có ít nhất 1 chữ và 1 số";
    public static final String CONFIRM_PASSWORD_REQUIRED = "Confirm password không được để trống";

    // Auth - Forgot/Reset Password, Verify Email (Giai đoạn 2)
    public static final String TOKEN_REQUIRED = "Token không được để trống";
    public static final String NEW_PASSWORD_REQUIRED = "Password mới không được để trống";
    public static final String CONFIRM_NEW_PASSWORD_REQUIRED = "Confirm password mới không được để trống";

    // User - Change Password (Giai đoạn 2)
    public static final String CURRENT_PASSWORD_REQUIRED = "Password hiện tại không được để trống";

    // User - Edit Profile (Giai đoạn 2)
    public static final String DISPLAY_NAME_SIZE = "Tên hiển thị tối đa 100 ký tự";
    public static final String AVATAR_URL_SIZE = "Avatar URL tối đa 500 ký tự";
    public static final String GENDER_SIZE = "Giới tính tối đa 20 ký tự";
    public static final String COUNTRY_SIZE = "Quốc gia tối đa 100 ký tự";
    public static final String CURRENT_LEVEL_SIZE = "Trình độ tối đa 20 ký tự";
    public static final String BIRTHDAY_MUST_BE_PAST = "Ngày sinh phải là 1 ngày trong quá khứ";
}
