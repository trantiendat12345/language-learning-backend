package com.languagelearning.language_learning_backend.quiz.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
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
 * 1 lựa chọn/đáp án thuộc 1 Question — vòng đời quản lý hoàn toàn qua Question (giống
 * GrammarExample->Grammar), update Question = thay toàn bộ danh sách option. Với
 * MULTIPLE_CHOICE/IMAGE_CHOICE/AUDIO_CHOICE phải có đúng 1 option `isCorrect=true`; với
 * FILL_BLANK/TYPING, option `isCorrect=true` duy nhất chính là đáp án đúng dạng text
 * (`optionText`) dùng để so khớp `typedAnswer`, xem QuestionServiceImpl/QuizServiceImpl.
 */
@Entity
@Table(name = "question_option")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class QuestionOption extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** Cột FK đọc trực tiếp (read-only) — tránh JOIN sang Question bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "question_id", insertable = false, updatable = false)
    private Long questionId;

    @Column(name = "option_text", length = 500)
    private String optionText;

    @Column(name = "option_image_url", length = 500)
    private String optionImageUrl;

    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
