package com.languagelearning.language_learning_backend.auth.repository;

import com.languagelearning.language_learning_backend.auth.entity.VerificationToken;
import com.languagelearning.language_learning_backend.auth.enums.TokenType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, TokenType type);
}
