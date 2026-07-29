package com.languagelearning.language_learning_backend.progress.service;

import com.languagelearning.language_learning_backend.progress.dto.response.LessonCompleteResponse;

public interface LessonProgressService {

    /**
     * 404 nếu Lesson/Course cha không tồn tại/không PUBLISHED. 400 (COURSE_NOT_ENROLLED) nếu
     * chưa enroll Course chứa Lesson. Idempotent — hoàn thành lại Lesson đã COMPLETED không
     * cộng thêm gì, chỉ trả về trạng thái hiện tại.
     */
    LessonCompleteResponse completeLesson(Long lessonId, Long userId);
}
