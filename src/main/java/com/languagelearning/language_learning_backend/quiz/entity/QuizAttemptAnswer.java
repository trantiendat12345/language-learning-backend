package com.languagelearning.language_learning_backend.quiz.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 1 câu trả lời trong 1 QuizAttempt — Log/Transaction data theo D9, kế thừa BaseEntity. */
@Entity
@Table(name = "quiz_attempt_answer")
@Getter
@Setter
public class QuizAttemptAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    /** Cột FK đọc trực tiếp (read-only) — tránh JOIN bị @SQLRestriction của Question lọc mất. */
    @Column(name = "quiz_attempt_id", insertable = false, updatable = false)
    private Long quizAttemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(name = "typed_answer", length = 500)
    private String typedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    /** Giữ đúng thứ tự câu hỏi lúc generate (field bổ sung ngoài data dictionary, phục vụ hiển thị lại đúng thứ tự). */
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
