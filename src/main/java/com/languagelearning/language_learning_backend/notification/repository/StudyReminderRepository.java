package com.languagelearning.language_learning_backend.notification.repository;

import com.languagelearning.language_learning_backend.notification.entity.StudyReminder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyReminderRepository extends JpaRepository<StudyReminder, Long> {

    List<StudyReminder> findAllByUserId(Long userId);
}
