package com.languagelearning.language_learning_backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.languagelearning.language_learning_backend.role.entity.Role;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.enums.UserStatus;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CustomUserDetailsTest {

    @Test
    void authorities_prefixedWithRoleFromUserRoles() {
        Role adminRole = new Role();
        adminRole.setCode("ADMIN");
        User user = new User();
        user.setUsername("admin01");
        user.setPasswordHash("hashed");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(adminRole));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertThat(userDetails.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
    }

    @Test
    void isEnabled_trueOnlyWhenStatusActive() {
        assertThat(userDetailsWithStatus(UserStatus.ACTIVE).isEnabled()).isTrue();
        assertThat(userDetailsWithStatus(UserStatus.PENDING_VERIFICATION).isEnabled()).isFalse();
        assertThat(userDetailsWithStatus(UserStatus.DISABLED).isEnabled()).isFalse();
        assertThat(userDetailsWithStatus(UserStatus.LOCKED).isEnabled()).isFalse();
    }

    @Test
    void isAccountNonLocked_falseOnlyWhenStatusLocked() {
        assertThat(userDetailsWithStatus(UserStatus.LOCKED).isAccountNonLocked()).isFalse();
        assertThat(userDetailsWithStatus(UserStatus.ACTIVE).isAccountNonLocked()).isTrue();
        assertThat(userDetailsWithStatus(UserStatus.DISABLED).isAccountNonLocked()).isTrue();
    }

    private CustomUserDetails userDetailsWithStatus(UserStatus status) {
        User user = new User();
        user.setUsername("user01");
        user.setPasswordHash("hashed");
        user.setStatus(status);
        user.setRoles(Set.of());
        return new CustomUserDetails(user);
    }
}
