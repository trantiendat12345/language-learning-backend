package com.languagelearning.language_learning_backend.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.OwnershipViolationException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderCreateRequest;
import com.languagelearning.language_learning_backend.notification.dto.request.StudyReminderUpdateRequest;
import com.languagelearning.language_learning_backend.notification.dto.response.StudyReminderResponse;
import com.languagelearning.language_learning_backend.notification.entity.StudyReminder;
import com.languagelearning.language_learning_backend.notification.enums.ReminderChannel;
import com.languagelearning.language_learning_backend.notification.enums.ReminderType;
import com.languagelearning.language_learning_backend.notification.repository.StudyReminderRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyReminderServiceImplTest {

    @Mock
    private StudyReminderRepository studyReminderRepository;

    @Mock
    private UserRepository userRepository;

    private StudyReminderServiceImpl studyReminderService;

    @BeforeEach
    void setUp() {
        studyReminderService = new StudyReminderServiceImpl(studyReminderRepository, userRepository);
    }

    private StudyReminder reminder(Long id, Long userId) {
        StudyReminder reminder = new StudyReminder();
        reminder.setId(id);
        reminder.setUserId(userId);
        reminder.setType(ReminderType.STUDY);
        reminder.setReminderTime(LocalTime.of(20, 0));
        reminder.setDaysOfWeek("MON,TUE,WED,THU,FRI,SAT,SUN");
        reminder.setChannel(ReminderChannel.IN_APP);
        reminder.setActive(true);
        return reminder;
    }

    private StudyReminderCreateRequest createRequest() {
        StudyReminderCreateRequest request = new StudyReminderCreateRequest();
        request.setType(ReminderType.STUDY);
        request.setReminderTime(LocalTime.of(20, 0));
        request.setDaysOfWeek("MON,TUE,WED,THU,FRI,SAT,SUN");
        return request;
    }

    @Test
    void createReminder_channelNotProvided_defaultsToInApp() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(studyReminderRepository.save(any(StudyReminder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyReminderResponse response = studyReminderService.createReminder(100L, createRequest());

        assertThat(response.getChannel()).isEqualTo(ReminderChannel.IN_APP);
        assertThat(response.getType()).isEqualTo(ReminderType.STUDY);
    }

    @Test
    void createReminder_channelProvided_usesGivenChannel() {
        StudyReminderCreateRequest request = createRequest();
        request.setChannel(ReminderChannel.EMAIL);
        when(userRepository.findById(100L)).thenReturn(Optional.of(new User()));
        when(studyReminderRepository.save(any(StudyReminder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyReminderResponse response = studyReminderService.createReminder(100L, request);

        assertThat(response.getChannel()).isEqualTo(ReminderChannel.EMAIL);
    }

    @Test
    void updateReminder_owner_updatesFields() {
        StudyReminder existing = reminder(1L, 100L);
        when(studyReminderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studyReminderRepository.save(any(StudyReminder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyReminderUpdateRequest request = new StudyReminderUpdateRequest();
        request.setType(ReminderType.REVIEW);
        request.setReminderTime(LocalTime.of(21, 0));
        request.setDaysOfWeek("SAT,SUN");
        request.setChannel(ReminderChannel.IN_APP);
        request.setActive(false);

        StudyReminderResponse response = studyReminderService.updateReminder(100L, 1L, request);

        assertThat(response.getType()).isEqualTo(ReminderType.REVIEW);
        assertThat(response.getReminderTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(response.isActive()).isFalse();
    }

    @Test
    void updateReminder_notOwner_throwsOwnershipViolationException() {
        StudyReminder existing = reminder(1L, 999L);
        when(studyReminderRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> studyReminderService.updateReminder(100L, 1L, new StudyReminderUpdateRequest()))
                .isInstanceOf(OwnershipViolationException.class);
        verify(studyReminderRepository, never()).save(any());
    }

    @Test
    void deleteReminder_owner_deletesSuccessfully() {
        StudyReminder existing = reminder(1L, 100L);
        when(studyReminderRepository.findById(1L)).thenReturn(Optional.of(existing));

        studyReminderService.deleteReminder(100L, 1L);

        verify(studyReminderRepository).delete(existing);
    }

    @Test
    void deleteReminder_notOwner_throwsOwnershipViolationException() {
        StudyReminder existing = reminder(1L, 999L);
        when(studyReminderRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> studyReminderService.deleteReminder(100L, 1L)).isInstanceOf(OwnershipViolationException.class);
        verify(studyReminderRepository, never()).delete(any(StudyReminder.class));
    }

    @Test
    void deleteReminder_notFound_throwsResourceNotFoundException() {
        when(studyReminderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyReminderService.deleteReminder(100L, 1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMyReminders_returnsMappedList() {
        when(studyReminderRepository.findAllByUserId(100L)).thenReturn(List.of(reminder(1L, 100L)));

        List<StudyReminderResponse> result = studyReminderService.getMyReminders(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDaysOfWeek()).isEqualTo("MON,TUE,WED,THU,FRI,SAT,SUN");
    }
}
