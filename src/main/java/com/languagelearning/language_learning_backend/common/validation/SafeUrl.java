package com.languagelearning.language_learning_backend.common.validation;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Chặn URL dùng scheme nguy hiểm (javascript:/data:/vbscript:...) ở các field lưu link ảnh/audio/video
 * do User nhập (imageUrl, avatarUrl, thumbnailUrl...) - chỉ chấp nhận http(s):// tuyệt đối hoặc đường
 * dẫn tương đối. Giá trị null/rỗng luôn hợp lệ, dùng kèm @NotBlank nếu field bắt buộc.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeUrlValidator.class)
public @interface SafeUrl {

    String message() default ValidationMessage.URL_SCHEME_INVALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
