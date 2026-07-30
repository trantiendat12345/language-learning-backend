package com.languagelearning.language_learning_backend.review.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.review.enums.ReviewRating;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Log/Transaction data theo D9 (liệt kê rõ tên) — append-only, không sửa/xoá qua API thông thường, kế thừa BaseEntity. */
@Entity
@Table(name = "review_log")
@Getter
@Setter
public class ReviewLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewRating rating;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
}
