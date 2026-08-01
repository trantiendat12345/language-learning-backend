package com.languagelearning.language_learning_backend.notification.dto.request;

import com.languagelearning.language_learning_backend.common.constant.ValidationMessage;
import com.languagelearning.language_learning_backend.notification.enums.ReminderChannel;
import com.languagelearning.language_learning_backend.notification.enums.ReminderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyReminderUpdateRequest {

    @NotNull(message = ValidationMessage.STUDY_REMINDER_TYPE_REQUIRED)
    private ReminderType type;

    @NotNull(message = ValidationMessage.STUDY_REMINDER_TIME_REQUIRED)
    private LocalTime reminderTime;

    @NotBlank(message = ValidationMessage.STUDY_REMINDER_DAYS_OF_WEEK_REQUIRED)
    private String daysOfWeek;

    @NotNull(message = ValidationMessage.STUDY_REMINDER_CHANNEL_REQUIRED)
    private ReminderChannel channel;

    private boolean active;
}
