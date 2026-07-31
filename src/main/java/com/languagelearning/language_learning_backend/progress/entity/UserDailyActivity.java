package com.languagelearning.language_learning_backend.progress.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Hoạt động học tập của 1 User trong 1 ngày (unique userId+activityDate, activityDate tính
 * theo timezone của User - D10/CLAUDE.md #11). Kế thừa BaseEntity - state hiện tại được cộng
 * dồn liên tục trong ngày (giống CourseEnrollment/LessonProgress), không phải log lịch sử.
 */
@Entity
@Table(
        name = "user_daily_activity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_date"}))
@Getter
@Setter
public class UserDailyActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Cột FK đọc trực tiếp (read-only) - tránh JOIN sang User bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "study_minutes", nullable = false)
    private int studyMinutes = 0;

    @Column(name = "words_learned", nullable = false)
    private int wordsLearned = 0;

    @Column(name = "xp_earned", nullable = false)
    private int xpEarned = 0;

    @Column(name = "goal_met", nullable = false)
    private boolean goalMet = false;
}
