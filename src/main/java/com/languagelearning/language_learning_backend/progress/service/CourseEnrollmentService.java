package com.languagelearning.language_learning_backend.progress.service;

import com.languagelearning.language_learning_backend.progress.dto.response.CourseEnrollmentResponse;

public interface CourseEnrollmentService {

    /** 404 nếu Course không tồn tại/không PUBLISHED. Idempotent — enroll lại trả về bản ghi cũ, không tạo trùng. */
    CourseEnrollmentResponse enrollInCourse(Long courseId, Long userId);
}
