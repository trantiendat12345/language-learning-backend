package com.languagelearning.language_learning_backend.auth.repository;

import com.languagelearning.language_learning_backend.auth.entity.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Dùng khi reset password thành công — revoke toàn bộ Refresh Token cũ của user. */
    List<RefreshToken> findAllByUserIdAndRevokedFalse(Long userId);
}
