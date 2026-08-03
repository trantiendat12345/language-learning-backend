package com.languagelearning.language_learning_backend.user.repository;

import com.languagelearning.language_learning_backend.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

/** Filter động cho GET /api/admin/users?keyword= — theo đúng mẫu đặt tên ở docs/PROJECT_OVERVIEW.md mục 7.1. */
public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> usernameOrEmailContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("username")), pattern), cb.like(cb.lower(root.get("email")), pattern));
        };
    }
}
