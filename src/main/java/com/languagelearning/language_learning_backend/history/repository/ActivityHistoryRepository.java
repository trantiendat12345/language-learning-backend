package com.languagelearning.language_learning_backend.history.repository;

import com.languagelearning.language_learning_backend.history.entity.ActivityHistory;
import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityHistoryRepository extends JpaRepository<ActivityHistory, Long> {

    List<ActivityHistory> findAllByUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable);

    List<ActivityHistory> findAllByUserIdAndActionOrderByOccurredAtDesc(Long userId, ActivityAction action, Pageable pageable);
}
