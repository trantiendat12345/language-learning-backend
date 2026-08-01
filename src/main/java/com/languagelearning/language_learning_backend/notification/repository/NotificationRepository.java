package com.languagelearning.language_learning_backend.notification.repository;

import com.languagelearning.language_learning_backend.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findAllByUserIdAndReadFalse(Long userId);
}
