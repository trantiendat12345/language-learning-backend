package com.languagelearning.language_learning_backend.notification.service.impl;

import com.languagelearning.language_learning_backend.exception.OwnershipViolationException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderCreateRequest;
import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderUpdateRequest;
import com.languagelearning.language_learning_backend.notification.dto.response.StudyReminderResponse;
import com.languagelearning.language_learning_backend.notification.entity.StudyReminder;
import com.languagelearning.language_learning_backend.notification.enums.ReminderChannel;
import com.languagelearning.language_learning_backend.notification.repository.StudyReminderRepository;
import com.languagelearning.language_learning_backend.notification.service.StudyReminderService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyReminderServiceImpl implements StudyReminderService {

    private final StudyReminderRepository studyReminderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StudyReminderResponse> getMyReminders(Long userId) {
        return studyReminderRepository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public StudyReminderResponse createReminder(Long userId, StudyReminderCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);

        StudyReminder reminder = new StudyReminder();
        reminder.setUser(user);
        reminder.setType(request.getType());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setDaysOfWeek(request.getDaysOfWeek());
        reminder.setChannel(request.getChannel() == null ? ReminderChannel.IN_APP : request.getChannel());
        reminder = studyReminderRepository.save(reminder);

        return toResponse(reminder);
    }

    @Override
    @Transactional
    public StudyReminderResponse updateReminder(Long userId, Long reminderId, StudyReminderUpdateRequest request) {
        StudyReminder reminder = requireOwnedReminder(userId, reminderId);

        reminder.setType(request.getType());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setDaysOfWeek(request.getDaysOfWeek());
        reminder.setChannel(request.getChannel());
        reminder.setActive(request.isActive());
        reminder = studyReminderRepository.save(reminder);

        return toResponse(reminder);
    }

    @Override
    @Transactional
    public void deleteReminder(Long userId, Long reminderId) {
        StudyReminder reminder = requireOwnedReminder(userId, reminderId);
        studyReminderRepository.delete(reminder);
    }

    private StudyReminder requireOwnedReminder(Long userId, Long reminderId) {
        StudyReminder reminder = studyReminderRepository.findById(reminderId).orElseThrow(ResourceNotFoundException::new);
        if (!reminder.getUserId().equals(userId)) {
            throw new OwnershipViolationException();
        }
        return reminder;
    }

    private StudyReminderResponse toResponse(StudyReminder reminder) {
        return StudyReminderResponse.builder()
                .id(reminder.getId())
                .type(reminder.getType())
                .reminderTime(reminder.getReminderTime())
                .daysOfWeek(reminder.getDaysOfWeek())
                .channel(reminder.getChannel())
                .active(reminder.isActive())
                .build();
    }
}
