package com.languagelearning.language_learning_backend.gamification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.gamification.entity.XpLog;
import com.languagelearning.language_learning_backend.gamification.enums.XpReason;
import com.languagelearning.language_learning_backend.gamification.repository.XpLogRepository;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class XpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private XpLogRepository xpLogRepository;

    private XpServiceImpl xpService;

    @BeforeEach
    void setUp() {
        xpService = new XpServiceImpl(userRepository, xpLogRepository);
    }

    private User user() {
        User user = new User();
        user.setId(100L);
        user.setXp(50);
        return user;
    }

    @Test
    void awardXp_addsToUserXpAndWritesXpLog_sameCall() {
        User user = user();
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        xpService.awardXp(100L, XpReason.LESSON_COMPLETED, 10, 5L);

        assertThat(user.getXp()).isEqualTo(60);
        verify(userRepository).save(user);

        ArgumentCaptor<XpLog> logCaptor = ArgumentCaptor.forClass(XpLog.class);
        verify(xpLogRepository).save(logCaptor.capture());
        XpLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getUser()).isEqualTo(user);
        assertThat(savedLog.getAmount()).isEqualTo(10);
        assertThat(savedLog.getReason()).isEqualTo(XpReason.LESSON_COMPLETED);
        assertThat(savedLog.getSourceId()).isEqualTo(5L);
        assertThat(savedLog.getEarnedAt()).isNotNull();
    }

    @Test
    void awardXp_whenUserNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> xpService.awardXp(100L, XpReason.REVIEW_DONE, 2, null))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(xpLogRepository, never()).save(any());
    }
}
