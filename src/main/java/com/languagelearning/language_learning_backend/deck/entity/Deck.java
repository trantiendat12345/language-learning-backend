package com.languagelearning.language_learning_backend.deck.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import com.languagelearning.language_learning_backend.deck.enums.DeckStatus;
import com.languagelearning.language_learning_backend.deck.enums.DeckVisibility;
import com.languagelearning.language_learning_backend.language.entity.Language;
import com.languagelearning.language_learning_backend.user.entity.User;
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
 * Bộ thẻ từ vựng do User tự tạo (D1 — chỉ liên kết tới Vocabulary có sẵn qua DeckCard, không
 * copy dữ liệu). Xoá Deck = soft-delete (D9), không ảnh hưởng `UserVocabularyProgress` của các
 * từ trong deck vì progress khoá theo `(user, vocabulary)`, độc lập với Deck (D2).
 */
@Entity
@Table(name = "deck")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Deck extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Cột FK đọc trực tiếp (read-only) — tránh JOIN sang User bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "owner_id", insertable = false, updatable = false)
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeckVisibility visibility = DeckVisibility.PRIVATE;

    /** Self-reference — null nếu deck tự tạo, có giá trị nếu clone từ Deck khác (D3). Không @OneToMany ngược (tránh circular JSON). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloned_from_deck_id")
    private Deck clonedFromDeck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeckStatus status = DeckStatus.ACTIVE;
}
