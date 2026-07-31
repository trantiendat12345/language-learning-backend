package com.languagelearning.language_learning_backend.gamification.repository;

import com.languagelearning.language_learning_backend.gamification.entity.XpLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XpLogRepository extends JpaRepository<XpLog, Long> {
}
