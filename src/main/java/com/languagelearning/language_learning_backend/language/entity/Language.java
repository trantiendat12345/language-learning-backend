package com.languagelearning.language_learning_backend.language.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import com.languagelearning.language_learning_backend.language.enums.LanguageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Ngôn ngữ hệ thống hỗ trợ (vd "en", "ja") — Course/Vocabulary đều gắn với 1 Language.
 * Content/Master data nên kế thừa AuditableEntity đầy đủ (soft-delete + audit) theo D9.
 */
@Entity
@Table(name = "language")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Language extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "flag_icon_url", length = 500)
    private String flagIconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LanguageStatus status = LanguageStatus.ACTIVE;
}
