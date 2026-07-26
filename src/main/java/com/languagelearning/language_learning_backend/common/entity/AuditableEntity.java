package com.languagelearning.language_learning_backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Lớp cha cho các entity thuộc nhóm Content/Master data (User, Course, Lesson, Vocabulary,
 * Deck...) cần đầy đủ audit trail (ai tạo/sửa, lúc nào) và soft-delete. Kế thừa BaseEntity
 * để có sẵn id, cộng thêm 7 field audit tự động cập nhật bởi Spring Data JPA Auditing
 * (xem JpaAuditingConfig) thông qua AuditingEntityListener.
 *
 * Lưu ý quan trọng: Hibernate KHÔNG áp dụng {@code @SQLRestriction} khai báo ở
 * {@code @MappedSuperclass} xuống các entity con, nên mỗi entity kế thừa lớp này PHẢI
 * tự khai báo thêm {@code @SQLRestriction("is_deleted = false")} ngay trên class entity đó,
 * nếu không truy vấn mặc định sẽ trả về cả dữ liệu đã bị xoá mềm.
 */
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;
}
