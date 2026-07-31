package com.languagelearning.language_learning_backend.favorite.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.favorite.enums.FavoriteTargetType;
import com.languagelearning.language_learning_backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * User đánh dấu yêu thích 1 Course/Deck/Vocabulary (unique userId+targetType+targetId). Kế
 * thừa BaseEntity - bỏ Favorite là xoá cứng (giống DeckCard/LessonVocabulary, không phải
 * Content/Master data cần soft-delete theo D9), tự thêm `favoritedAt` thay vì audit đầy đủ vì
 * user luôn tự thao tác trên bản ghi của chính mình, không có luồng "sửa" (chỉ tạo/xoá).
 * `targetType`+`targetId` không FK trực tiếp (giống Question.sourceType/sourceId) vì trỏ tới
 * nhiều loại entity khác nhau - resolve/hiển thị chi tiết ở tầng Service.
 */
@Entity
@Table(
        name = "favorite",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_type", "target_id"}))
@Getter
@Setter
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Cột FK đọc trực tiếp (read-only) - tránh JOIN sang User bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private FavoriteTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "favorited_at", nullable = false)
    private LocalDateTime favoritedAt;
}
