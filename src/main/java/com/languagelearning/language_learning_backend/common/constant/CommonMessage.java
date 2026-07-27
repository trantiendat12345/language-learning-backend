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
}
