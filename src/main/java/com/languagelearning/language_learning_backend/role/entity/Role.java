package com.languagelearning.language_learning_backend.role.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Vai trò người dùng (MVP chỉ có ADMIN/USER, tạo sẵn qua RoleSeeder). Quan hệ nhiều-nhiều
 * với Permission qua bảng role_permission — đã sẵn sàng cho RBAC chi tiết ở tương lai (D7)
 * mà không cần đổi schema, chỉ cần thêm dữ liệu và đổi hasRole thành hasAuthority.
 */
@Entity
@Table(name = "role")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class Role extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();
}
