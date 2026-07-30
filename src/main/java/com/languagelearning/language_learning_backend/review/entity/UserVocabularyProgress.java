package com.languagelearning.language_learning_backend.review.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.review.enums.MasteryLevel;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Mức độ ghi nhớ 1 Vocabulary của 1 User (D2 — khoá theo cặp (user, vocabulary), KHÔNG theo
 * deck/lesson: 1 từ dù học ở đâu cũng chỉ có 1 mức ghi nhớ duy nhất). Kế thừa BaseEntity —
 * giống CourseEnrollment/LessonProgress: là state hiện tại được ghi đè liên tục qua mỗi lần
 * review, không phải log lịch sử (ReviewLog mới là log), không có nhu cầu soft-delete.
 */
@Entity
@Table(
        name = "user_vocabulary_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vocabulary_id"}))
@Getter
@Setter
public class UserVocabularyProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    /** Cột FK đọc trực tiếp (read-only) — tránh JOIN sang User/Vocabulary bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "vocabulary_id", insertable = false, updatable = false)
    private Long vocabularyId;

    @Column(name = "ease_factor", nullable = false)
    private float easeFactor = 2.5f;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays = 0;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount = 0;

    @Column(name = "next_review_date", nullable = false)
    private LocalDate nextReviewDate;

    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;

    @Column(name = "forgot_count", nullable = false)
    private int forgotCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 20)
    private MasteryLevel masteryLevel = MasteryLevel.NEW;
}
