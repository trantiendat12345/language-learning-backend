package com.languagelearning.language_learning_backend.grammar.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import com.languagelearning.language_learning_backend.lesson.entity.Lesson;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * 1 điểm ngữ pháp thuộc 1 Lesson. Không có field status riêng (theo ERD
 * docs/PROJECT_OVERVIEW.md mục 6.2) — hiển thị/ẩn hoàn toàn phụ thuộc trạng thái Lesson cha,
 * tương tự LessonVocabulary. Quan hệ với Lesson 1 chiều (FK), không @OneToMany ngược lại,
 * cùng lý do tránh circular JSON như Lesson->Course.
 */
@Entity
@Table(name = "grammar")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Grammar extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 255)
    private String pattern;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
