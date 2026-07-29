package com.languagelearning.language_learning_backend.progress.enums;

/**
 * NOT_STARTED không bao giờ được ghi thành 1 dòng LessonProgress thật — "chưa bắt đầu" nghĩa
 * là không tồn tại dòng nào cho (user, lesson) đó. Giá trị này giữ lại để khớp
 * docs/testing/07_DATA_DICTIONARY.md, dùng khi FE cần hiển thị trạng thái mặc định.
 */
public enum LessonProgressStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
