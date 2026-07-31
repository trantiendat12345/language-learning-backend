package com.languagelearning.language_learning_backend.quiz.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
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
 * 1 lần làm Quiz (D5 — không có entity Quiz tĩnh, kết quả mỗi lần generate+làm bài lưu vào
 * đây). Kế thừa BaseEntity — bản chất là dữ liệu log/transaction (append-only, không sửa/xoá
 * qua API thông thường), giống ReviewLog trong D9, dù D9 không liệt kê tên rõ QuizAttempt.
 * `xpEarned` (Giai đoạn 7) cộng đúng 1 lần mỗi lần nộp bài, kể cả làm lại (khác Lesson complete
 * chỉ cộng lần đầu) — xem QuizServiceImpl/gamification/service/XpService.
 */
@Entity
@Table(name = "quiz_attempt")
@Getter
@Setter
public class QuizAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Cột FK đọc trực tiếp (read-only) — tránh JOIN sang User bị @SQLRestriction lọc mất. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private QuestionSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    @Column(name = "wrong_answers", nullable = false)
    private int wrongAnswers;

    @Column(nullable = false)
    private float score;

    @Column(nullable = false)
    private float accuracy;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "xp_earned", nullable = false)
    private int xpEarned = 0;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
}
