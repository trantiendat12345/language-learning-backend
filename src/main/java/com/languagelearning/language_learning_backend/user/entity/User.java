package com.languagelearning.language_learning_backend.user.entity;

import com.languagelearning.language_learning_backend.common.entity.AuditableEntity;
import com.languagelearning.language_learning_backend.role.entity.Role;
import com.languagelearning.language_learning_backend.user.enums.DailyGoalType;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tài khoản người dùng. Field xp/currentStreak/longestStreak/coin là dữ liệu denormalized
 * cho Gamification, đặt sẵn trên bảng User theo đúng ERD đã chốt. `lastActiveDate` bổ sung ở
 * Giai đoạn 7 để tính streak — ERD gốc mô tả `UserStreak` là bảng 1-1 riêng, nhưng vì
 * currentStreak/longestStreak đã denormalized sẵn lên User từ Giai đoạn 2, thêm 1 bảng riêng
 * sẽ tạo 2 nguồn sự thật cho cùng 1 giá trị — quyết định chốt khi code Giai đoạn 7: giữ
 * nguyên đơn giản trên User, không tạo bảng `user_streak` riêng (xem
 * gamification/service/StreakService và docs/dev/SCHEMA_CHANGE_LOG.md).
 * `dailyGoalType`/`dailyGoalValue` (Giai đoạn 7, dùng cho UserDailyActivity.goalMet) — theo
 * đặc tả gốc thuộc Giai đoạn 2 (docs/testing/12_FRS_TC_USER_PROFILE.md mục 1.4) nhưng chưa
 * từng được code, chỉ phát hiện và bổ sung khi làm tới Progress/Gamification cần dùng.
 * nativeLanguageId/learningLanguageId CHƯA thêm — sẽ bổ sung ở Giai đoạn 3 khi entity
 * Language tồn tại, tránh tạo quan hệ tới entity chưa có (ghi vào SCHEMA_CHANGE_LOG.md khi làm).
 */
@Entity
@Table(name = "users")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
public class User extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    private LocalDate birthday;

    @Column(length = 20)
    private String gender;

    @Column(length = 100)
    private String country;

    @Column(name = "current_level", length = 20)
    private String currentLevel;

    @Column(nullable = false)
    private int xp = 0;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 0;

    @Column(nullable = false)
    private int coin = 0;

    @Column(nullable = false, length = 50)
    private String timezone;

    /** Ngày cuối cùng có UserDailyActivity (theo timezone user) - dùng để tính currentStreak/longestStreak, xem gamification/service/StreakService. */
    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "daily_goal_type", nullable = false, length = 20)
    private DailyGoalType dailyGoalType = DailyGoalType.WORDS;

    @Column(name = "daily_goal_value", nullable = false)
    private int dailyGoalValue = 10;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
