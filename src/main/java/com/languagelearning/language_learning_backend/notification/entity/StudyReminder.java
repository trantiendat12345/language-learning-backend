package com.languagelearning.language_learning_backend.notification.entity;

import com.languagelearning.language_learning_backend.common.entity.BaseEntity;
import com.languagelearning.language_learning_backend.notification.enums.ReminderChannel;
import com.languagelearning.language_learning_backend.notification.enums.ReminderType;
import com.languagelearning.language_learning_backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Cấu hình giờ nhắc học của User. Kế thừa BaseEntity — user tự tạo/sửa/xoá bản ghi của chính
 * mình, không cần audit trail/soft-delete, giống Favorite. `daysOfWeek` lưu dạng chuỗi
 * phân tách bởi dấu phẩy (vd "MON,TUE,WED,THU,FRI,SAT,SUN") — không chuẩn hoá thành bảng
 * riêng vì FRS không yêu cầu truy vấn theo từng ngày, chỉ hiển thị nguyên trạng.
 * `channel` chấp nhận cả 3 giá trị theo đúng ERD nhưng MVP chỉ IN_APP có tác dụng thật —
 * EMAIL/PUSH lưu được nhưng chưa gửi thông báo thật (Phase 2, xem
 * docs/testing/19_FRS_TC_NOTIFICATION_REMINDER.md mục 1.2).
 */
@Entity
@Table(name = "study_reminder")
@Getter
@Setter
public class StudyReminder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Cột FK đọc trực tiếp (read-only) - tránh JOIN sang User bị @SQLRestriction lọc mất, xem GrammarExample.grammarId. */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderType type;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(name = "days_of_week", nullable = false, length = 50)
    private String daysOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderChannel channel = ReminderChannel.IN_APP;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
