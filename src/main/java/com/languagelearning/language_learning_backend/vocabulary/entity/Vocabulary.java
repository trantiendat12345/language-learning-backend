package com.languagelearning.language_learning_backend.vocabulary.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.vocabulary.enums.VocabularyStatus;
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
 * Từ vựng dùng chung theo Language (D1) — Lesson/Deck chỉ liên kết tới Vocabulary qua bảng
 * join, không copy dữ liệu. `owner` null = từ hệ thống (Admin quản lý, chunk hiện tại);
 * có giá trị = từ custom của User (tạo qua luồng thêm-từ-vào-Deck ở Giai đoạn 5, CHƯA có
 * trong chunk này — entity đã có sẵn field để không phải đổi schema sau).
 */
@Entity
@Table(name = "vocabulary")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Vocabulary extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 200)
    private String word;

    @Column(nullable = false, length = 500)
    private String meaning;

    @Column(length = 100)
    private String ipa;

    @Column(name = "pronunciation_audio_url", length = 500)
    private String pronunciationAudioUrl;

    @Column(name = "word_type", length = 30)
    private String wordType;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(name = "example_translation", columnDefinition = "TEXT")
    private String exampleTranslation;

    @Column(name = "frequency_rank")
    private Integer frequencyRank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VocabularyStatus status = VocabularyStatus.ACTIVE;
}
