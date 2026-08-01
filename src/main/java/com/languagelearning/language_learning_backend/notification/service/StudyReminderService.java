package com.languagelearning.language_learning_backend.notification.service;

import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderCreateRequest;
import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderUpdateRequest;
import com.languagelearning.language_learning_backend.notification.dto.response.StudyReminderResponse;
import java.util.List;

public interface StudyReminderService {

    List<StudyReminderResponse> getMyReminders(Long userId);

    StudyReminderResponse createReminder(Long userId, StudyReminderCreateRequest request);

    /** Ownership check - chỉ chủ sở hữu Reminder mới sửa được. */
    StudyReminderResponse updateReminder(Long userId, Long reminderId, StudyReminderUpdateRequest request);

    /** Ownership check - chỉ chủ sở hữu Reminder mới xoá được. */
    void deleteReminder(Long userId, Long reminderId);
}
