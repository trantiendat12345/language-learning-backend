package com.languagelearning.language_learning_backend.lesson.entity;

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
 * Bảng join thuần giữa Lesson và Vocabulary (D1 — Lesson chỉ liên kết, không copy dữ liệu từ
 * vựng). Kế thừa BaseEntity (không AuditableEntity) vì đây là quan hệ cấu trúc thuần không có
 * nội dung riêng cần audit/soft-delete — gỡ 1 từ khỏi Lesson là xoá cứng dòng join, không có
 * nhu cầu khôi phục lại join đã gỡ (khác Course/Lesson/Vocabulary là Content data thật sự có
 * ý nghĩa "archive rồi phục hồi"), tương tự cách RefreshToken/VerificationToken dùng BaseEntity.
 */
@Entity
@Table(
        name = "lesson_vocabulary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lesson_id", "vocabulary_id"}))
@Getter
@Setter
public class LessonVocabulary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    /**
     * 2 cột FK đọc trực tiếp (read-only, không insert/update qua 2 field này — ghi vẫn qua
     * `lesson`/`vocabulary` ở trên). Bắt buộc có riêng vì Spring Data derived query dựa vào
     * field `lesson`/`vocabulary` sẽ sinh JOIN sang Lesson/Vocabulary, và Hibernate áp
     * @SQLRestriction của 2 entity đó vào JOIN — nếu Lesson/Vocabulary đã bị soft-delete thì
     * JOIN không khớp, query trả rỗng dù dòng join vẫn còn (xem lỗi tương tự đã gặp và sửa ở
     * GrammarExample.grammarId). 2 field riêng này query thẳng cột FK, không JOIN.
     */
    @Column(name = "lesson_id", insertable = false, updatable = false)
    private Long lessonId;

    @Column(name = "vocabulary_id", insertable = false, updatable = false)
    private Long vocabularyId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
