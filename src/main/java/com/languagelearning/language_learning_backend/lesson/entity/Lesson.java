package com.languagelearning.language_learning_backend.lesson.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import com.languagelearning.language_learning_backend.course.entity.Course;
import com.languagelearning.language_learning_backend.lesson.enums.LessonStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Bài học thuộc 1 Course. Quan hệ với Course là 1 chiều (Lesson -> Course qua FK), Course
 * KHÔNG có @OneToMany ngược lại — tránh circular JSON risk (docs/PROJECT_OVERVIEW.md mục
 * 10.2), Service tự query danh sách Lesson theo courseId khi cần.
 */
@Entity
@Table(name = "lesson")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Lesson extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonStatus status = LessonStatus.DRAFT;
}
