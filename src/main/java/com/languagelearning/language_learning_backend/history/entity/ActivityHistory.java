package com.languagelearning.language_learning_backend.history.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import com.languagelearning.language_learning_backend.history.enums.ActivityTargetType;
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
 * Log/Transaction data theo D9 — append-only, ghi tự động bởi hệ thống khi User xem/học/ôn tập
 * 1 đối tượng, không cho phép sửa/xoá qua API thông thường (xem docs/testing/18_FRS_TC_FAVORITE_HISTORY.md
 * mục 1.2). `targetType`+`targetId` không FK trực tiếp (giống Question.sourceType/sourceId,
 * Favorite.targetType/targetId) vì trỏ tới nhiều loại entity khác nhau.
 */
@Entity
@Table(name = "activity_history")
@Getter
@Setter
public class ActivityHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Cột FK đọc trực tiếp (read-only) - tránh JOIN sang User bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ActivityTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityAction action;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
