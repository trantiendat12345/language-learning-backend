package com.languagelearning.language_learning_backend.gamification.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.gamification.enums.XpReason;
import com.languagelearning.language_learning_backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Log/Transaction data theo D9 — append-only, mỗi dòng tương ứng đúng 1 lần cộng XP, ghi cùng
 * transaction với User.xp += amount (xem D8, gamification/service/XpService). Bất biến bắt
 * buộc: User.xp == SUM(XpLog.amount) theo userId.
 */
@Entity
@Table(name = "xp_log")
@Getter
@Setter
public class XpLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Cột FK đọc trực tiếp (read-only) — tránh JOIN sang User bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private XpReason reason;

    /** id của Lesson/Question/QuizAttempt... gắn với lần cộng XP này, tuỳ theo reason - nullable vì DAILY_GOAL_MET không gắn với 1 nguồn cụ thể. */
    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;
}
