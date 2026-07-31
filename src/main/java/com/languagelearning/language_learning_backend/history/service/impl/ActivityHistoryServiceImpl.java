package com.languagelearning.language_learning_backend.history.service.impl;

import com.languagelearning.language_learning_backend.course.repository.CourseRepository;
import com.languagelearning.language_learning_backend.deck.repository.DeckRepository;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.history.dto.response.ActivityHistoryResponse;
import com.languagelearning.language_learning_backend.history.entity.ActivityHistory;
import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import com.languagelearning.language_learning_backend.history.enums.ActivityTargetType;
import com.languagelearning.language_learning_backend.history.repository.ActivityHistoryRepository;
import com.languagelearning.language_learning_backend.history.service.ActivityHistoryService;
import com.languagelearning.language_learning_backend.lesson.repository.LessonRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import com.languagelearning.language_learning_backend.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityHistoryServiceImpl implements ActivityHistoryService {

    private final ActivityHistoryRepository activityHistoryRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final DeckRepository deckRepository;
    private final VocabularyRepository vocabularyRepository;

    @Override
    @Transactional
    public void recordActivity(Long userId, ActivityTargetType targetType, Long targetId, ActivityAction action) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);

        ActivityHistory history = new ActivityHistory();
        history.setUser(user);
        history.setTargetType(targetType);
        history.setTargetId(targetId);
        history.setAction(action);
        history.setOccurredAt(LocalDateTime.now());
        activityHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityHistoryResponse> getMyHistory(Long userId, ActivityAction action, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<ActivityHistory> histories = action == null
                ? activityHistoryRepository.findAllByUserIdOrderByOccurredAtDesc(userId, pageRequest)
                : activityHistoryRepository.findAllByUserIdAndActionOrderByOccurredAtDesc(userId, action, pageRequest);

        return histories.stream().map(this::toResponse).toList();
    }

    private ActivityHistoryResponse toResponse(ActivityHistory history) {
        String title = resolveTitle(history.getTargetType(), history.getTargetId());
        return ActivityHistoryResponse.builder()
                .id(history.getId())
                .targetType(history.getTargetType())
                .targetId(history.getTargetId())
                .title(title)
                .action(history.getAction())
                .occurredAt(history.getOccurredAt())
                .build();
    }

    private String resolveTitle(ActivityTargetType targetType, Long targetId) {
        return switch (targetType) {
            case COURSE -> courseRepository.findById(targetId).map(c -> c.getTitle()).orElse(null);
            case LESSON -> lessonRepository.findById(targetId).map(l -> l.getTitle()).orElse(null);
            case DECK -> deckRepository.findById(targetId).map(d -> d.getTitle()).orElse(null);
            case VOCABULARY -> vocabularyRepository.findById(targetId).map(v -> v.getWord()).orElse(null);
        };
    }
}
