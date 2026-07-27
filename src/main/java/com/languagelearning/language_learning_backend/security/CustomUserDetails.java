package com.languagelearning.language_learning_backend.security;

import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Bọc User entity theo interface UserDetails của Spring Security. Service lấy user hiện tại
 * qua getUserId() của class này (từ SecurityContext), không bao giờ nhận userId từ request
 * — xem CLAUDE.md mục "Quy tắc bắt buộc" #6.
 *
 * Quyết định: chỉ status=ACTIVE mới đăng nhập được (isEnabled=true) — PENDING_VERIFICATION
 * bị chặn hoàn toàn cho tới khi xác thực email, không cho đăng nhập giới hạn tính năng.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final UserStatus status;
    private final Collection<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.status = user.getStatus();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
