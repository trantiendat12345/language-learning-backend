package com.languagelearning.language_learning_backend.notification.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Thông báo trong app. Kế thừa BaseEntity — không phải Content/Master data (không có luồng
 * Admin sửa/xoá 1 Notification cụ thể) và cũng không phải log thuần append-only (field
 * `isRead` bị User cập nhật qua API) — giống Favorite/CourseEnrollment: state đơn giản do
 * chính User thao tác, không cần audit trail. Tự thêm `createdAt` (không kế thừa
 * AuditableEntity) để sắp xếp "mới nhất trước".
 *
 * `user` nullable theo đúng ERD (`docs/PROJECT_OVERVIEW.md` mục 6.6) để dành cho Notification
 * broadcast (`userId=null` = mọi user đều thấy) — NHƯNG chunk này CHƯA có đường tạo broadcast
 * nào (TC-NOTI-004 trong FRS tự ghi rõ "(Phase 2)": cần Admin Notification management, thuộc
 * `02_FEATURE_LIST.md` mục 9.10 P2). Vì vậy Service tầng này chỉ tạo/đọc Notification cá nhân
 * (`user` luôn có giá trị); bảng phụ theo dõi "đã đọc" riêng theo user cho broadcast
 * (`NotificationRead`, FRS gợi ý ở mục 1.1) hoãn tới khi Admin broadcast thật sự được xây.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Cột FK đọc trực tiếp (read-only) - tránh JOIN sang User bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(length = 50)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
