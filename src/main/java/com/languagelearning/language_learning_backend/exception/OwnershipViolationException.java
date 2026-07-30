package com.languagelearning.language_learning_backend.exception;

import com.languagelearning.language_learning_backend.common.constant.ErrorCode;
import com.languagelearning.language_learning_backend.common.constant.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Ném ra khi user đã đăng nhập hợp lệ nhưng thao tác sửa/xoá lên tài nguyên cá nhân không
 * thuộc sở hữu của mình (Deck, Favorite, StudyReminder, Vocabulary custom...) —
 * `resource.ownerId != currentUserId`. Khác `ForbiddenException` (errorCode FORBIDDEN, dùng
 * cho sai role ở `/api/admin/**`) — dùng errorCode riêng OWNERSHIP_VIOLATION để FE phân biệt
 * 2 nguyên nhân 403 khác nhau. Theo quy tắc mục 6 `docs/testing/04_BUSINESS_RULES_GLOBAL.md`:
 * mặc định trả 403 (không phải 404) trừ trường hợp cố ý ẩn sự tồn tại tài nguyên private.
 */
public class OwnershipViolationException extends BusinessException {

    public OwnershipViolationException() {
        super(HttpStatus.FORBIDDEN, ErrorCode.OWNERSHIP_VIOLATION, ErrorMessage.OWNERSHIP_VIOLATION);
    }
}
