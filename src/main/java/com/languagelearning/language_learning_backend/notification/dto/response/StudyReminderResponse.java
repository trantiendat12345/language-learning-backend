package com.languagelearning.language_learning_backend.notification.dto.response;

import com.languagelearning.language_learning_backend.notification.enums.ReminderChannel;
import com.languagelearning.language_learning_backend.notification.enums.ReminderType;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyReminderResponse {

    private Long id;
    private ReminderType type;
    private LocalTime reminderTime;
    private String daysOfWeek;
    private ReminderChannel channel;
    private boolean active;
}
