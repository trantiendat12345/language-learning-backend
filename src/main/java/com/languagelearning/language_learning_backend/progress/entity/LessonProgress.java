package com.languagelearning.language_learning_backend.progress.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import com.languagelearning.language_learning_backend.progress.enums.LessonProgressStatus;
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
 * Trạng thái hoàn thành 1 Lesson của 1 User (unique userId+lessonId). Chunk hiện tại chỉ ghi
 * dòng khi hoàn thành (status=COMPLETED qua POST .../complete) — không có API "bắt đầu học"
 * riêng nên NOT_STARTED/IN_PROGRESS không thực sự được ghi (xem LessonProgressStatus). Kế thừa
 * BaseEntity cùng lý do như CourseEnrollment.
 */
@Entity
@Table(
        name = "lesson_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"}))
@Getter
@Setter
public class LessonProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    /** Cột FK đọc trực tiếp (read-only) — cùng lý do CourseEnrollment.userId/courseId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "lesson_id", insertable = false, updatable = false)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonProgressStatus status = LessonProgressStatus.NOT_STARTED;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
