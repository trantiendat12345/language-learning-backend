package com.languagelearning.language_learning_backend.progress.repository;

import com.languagelearning.language_learning_backend.progress.entity.CourseEnrollment;
import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    Optional<CourseEnrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    /** Dùng cho "Continue Learning" ở Progress Dashboard - khoá học IN_PROGRESS được cập nhật gần nhất. */
    Optional<CourseEnrollment> findFirstByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, EnrollmentStatus status);
}
