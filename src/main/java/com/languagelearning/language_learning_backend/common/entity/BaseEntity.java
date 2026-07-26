package com.languagelearning.language_learning_backend.common.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Lớp cha tối thiểu cho mọi entity, chỉ cung cấp khoá chính tự tăng (id). Dùng trực tiếp
 * (không qua AuditableEntity) cho các bảng log/transaction tần suất cao như XpLog, ReviewLog,
 * ActivityHistory... vì các bảng này không cần audit fields hay soft-delete (xem D9 ở
 * docs/PROJECT_OVERVIEW.md).
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
