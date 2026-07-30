package com.languagelearning.language_learning_backend.review.repository;

import com.languagelearning.language_learning_backend.review.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
}
