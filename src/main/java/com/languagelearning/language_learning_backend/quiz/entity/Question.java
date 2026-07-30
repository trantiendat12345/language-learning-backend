package com.languagelearning.language_learning_backend.quiz.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionSourceType;
import com.languagelearning.language_learning_backend.quiz.enums.QuestionType;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
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
 * Ngân hàng câu hỏi dùng chung (D4/D5) — không có entity Exercise/Quiz tĩnh riêng. Gắn với 1
 * nguồn qua `sourceType`/`sourceId` (vd sourceType=LESSON, sourceId=id của Lesson) thay vì FK
 * trực tiếp, vì 1 câu hỏi có thể thuộc nhiều loại nguồn khác nhau (Lesson/Course/Deck/VocabList).
 */
@Entity
@Table(name = "question")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Question extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private QuestionSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;

    @Column(name = "prompt_text", columnDefinition = "TEXT")
    private String promptText;

    @Column(name = "prompt_audio_url", length = 500)
    private String promptAudioUrl;

    @Column(name = "prompt_image_url", length = 500)
    private String promptImageUrl;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 20)
    private String difficulty;
}
