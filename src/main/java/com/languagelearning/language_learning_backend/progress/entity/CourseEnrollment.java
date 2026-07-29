package com.languagelearning.language_learning_backend.progress.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.progress.enums.EnrollmentStatus;
import com.languagelearning.language_learning_backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Trạng thái đăng ký học của 1 User với 1 Course (unique userId+courseId). Kế thừa BaseEntity
 * (không AuditableEntity) — không có nhu cầu soft-delete/unenroll (FRS không có luồng huỷ
 * đăng ký), `createdBy`/`updatedBy`/`deletedBy` cũng vô nghĩa vì user luôn tự thao tác trên
 * chính bản ghi của mình; chỉ cần `enrolledAt`/`updatedAt` tự khai báo, giống cách
 * RefreshToken/VerificationToken tự thêm field thời gian riêng thay vì kế thừa AuditableEntity.
 */
@Entity
@Table(
        name = "course_enrollment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@Getter
@Setter
public class CourseEnrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Cột FK đọc trực tiếp (read-only, ghi vẫn qua field `user`/`course` ở trên) — tránh
     * Spring Data derived query JOIN sang User/Course rồi bị chính @SQLRestriction của 2
     * entity đó lọc mất, xem lý do đầy đủ ở GrammarExample.grammarId/LessonVocabulary.lessonId.
     */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "course_id", insertable = false, updatable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.IN_PROGRESS;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent = 0;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
