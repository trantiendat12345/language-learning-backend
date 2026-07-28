package com.languagelearning.language_learning_backend.common.constant;

/**
 * Message dung chung cho response thanh cong (ApiResponse), tach rieng khoi
 * ErrorMessage de khong lan giua message loi va message thanh cong.
 */
public final class CommonMessage {

    private CommonMessage() {
    }

    public static final String SUCCESS = "Success";

    // Auth-specific (Giai đoạn 2)
    public static final String AUTH_REGISTER_SUCCESS =
            "Đăng ký thành công, vui lòng kiểm tra email để xác thực tài khoản";
    public static final String AUTH_LOGIN_SUCCESS = "Đăng nhập thành công";
    public static final String AUTH_LOGOUT_SUCCESS = "Đăng xuất thành công";
    public static final String AUTH_REFRESH_TOKEN_SUCCESS = "Cấp lại Access Token thành công";

    // Message cố tình giống hệt nhau dù email tồn tại hay không - tránh dò tài khoản qua
    // forgot-password (xem docs/testing/11_FRS_TC_AUTH.md mục 1.5).
    public static final String AUTH_FORGOT_PASSWORD_SUCCESS =
            "Nếu email tồn tại trong hệ thống, link đặt lại mật khẩu đã được gửi";
    public static final String AUTH_RESET_PASSWORD_SUCCESS = "Đặt lại mật khẩu thành công";
    public static final String AUTH_VERIFY_EMAIL_SUCCESS = "Xác thực email thành công";
}
