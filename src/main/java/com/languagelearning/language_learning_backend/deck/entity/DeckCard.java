package com.languagelearning.language_learning_backend.deck.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.vocabulary.entity.Vocabulary;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Bảng join thuần giữa Deck và Vocabulary (D1 — không copy dữ liệu từ vựng). Kế thừa
 * BaseEntity (không AuditableEntity) — cùng lý do LessonVocabulary: gỡ từ khỏi Deck là xoá
 * cứng dòng join, không cần soft-delete/audit; Vocabulary gốc không bị ảnh hưởng.
 */
@Entity
@Table(
        name = "deck_card",
        uniqueConstraints = @UniqueConstraint(columnNames = {"deck_id", "vocabulary_id"}))
@Getter
@Setter
public class DeckCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    /** 2 cột FK đọc trực tiếp (read-only) — tránh JOIN bị @SQLRestriction của Deck/Vocabulary lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "deck_id", insertable = false, updatable = false)
    private Long deckId;

    @Column(name = "vocabulary_id", insertable = false, updatable = false)
    private Long vocabularyId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
