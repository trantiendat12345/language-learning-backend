package com.languagelearning.language_learning_backend.user.repository;

import com.languagelearning.language_learning_backend.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Đăng nhập chấp nhận cả username lẫn email trong cùng 1 ô nhập — xem 11_FRS_TC_AUTH.md.
     * JOIN FETCH roles vì kết quả dùng để dựng CustomUserDetails.getAuthorities() ngay trong
     * JwtAuthenticationFilter (chạy ngoài transaction) — không JOIN FETCH sẽ ném
     * LazyInitializationException khi truy cập roles sau khi session đã đóng.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
}
