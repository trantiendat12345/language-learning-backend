package com.languagelearning.language_learning_backend.role.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Quyền hạn chi tiết (vd COURSE_CREATE) — đã có schema sẵn cho RBAC chi tiết (D7),
 * nhưng MVP chưa dùng tới, logic phân quyền hiện tại chỉ check theo Role.
 */
@Entity
@Table(name = "permission")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Permission extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;
}
