package com.languagelearning.language_learning_backend.grammar.entity;

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
 * 1 câu ví dụ thuộc 1 Grammar — vòng đời quản lý hoàn toàn qua Grammar (tạo/sửa Grammar thay
 * toàn bộ danh sách example, không có API sửa/xoá riêng lẻ từng example). Không @OneToMany
 * ngược lại từ Grammar (tránh circular JSON, giống Lesson->Course) — Service tự query theo
 * grammarId khi cần.
 */
@Entity
@Table(name = "grammar_example")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class GrammarExample extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grammar_id", nullable = false)
    private Grammar grammar;

    /**
     * Cột grammar_id đọc trực tiếp (read-only, không insert/update qua field này — ghi vẫn
     * qua field `grammar` ở trên). Bắt buộc phải có field riêng này vì Spring Data derived
     * query `findAllByGrammarId` nếu chỉ dựa vào field `grammar` sẽ sinh JOIN sang bảng
     * `grammar`, và Hibernate áp @SQLRestriction của Grammar vào JOIN đó — khi Grammar cha đã
     * bị soft-delete (vd trong GrammarServiceImpl.deleteGrammar, gọi save() xong mới query
     * example), JOIN sẽ không khớp và query trả về rỗng dù example vẫn còn is_deleted=false,
     * khiến soft-delete không cascade xuống được. Field riêng này query thẳng cột FK, không JOIN.
     */
    @Column(name = "grammar_id", insertable = false, updatable = false)
    private Long grammarId;

    @Column(name = "example_text", nullable = false, columnDefinition = "TEXT")
    private String exampleText;

    @Column(columnDefinition = "TEXT")
    private String translation;

    @Column(columnDefinition = "TEXT")
    private String note;
}
