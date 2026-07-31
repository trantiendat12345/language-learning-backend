package com.languagelearning.language_learning_backend.progress.repository;

import com.languagelearning.language_learning_backend.progress.entity.UserDailyActivity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivity, Long> {

    Optional<UserDailyActivity> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);
}
