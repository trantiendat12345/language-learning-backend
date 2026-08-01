package com.languagelearning.language_learning_backend.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.languagelearning.language_learning_backend.exception.OwnershipViolationException;
import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.notification.dto.response.NotificationResponse;
import com.languagelearning.language_learning_backend.notification.entity.Notification;
import com.languagelearning.language_learning_backend.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository);
    }

    private Notification notification(Long id, Long userId, boolean read) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setTitle("Nhắc ôn tập");
        notification.setMessage("Bạn có 5 từ cần ôn hôm nay");
        notification.setRead(read);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    @Test
    void getMyNotifications_returnsMappedList() {
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(100L))
                .thenReturn(List.of(notification(1L, 100L, false)));

        List<NotificationResponse> result = notificationService.getMyNotifications(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Nhắc ôn tập");
        assertThat(result.get(0).isRead()).isFalse();
    }

    @Test
    void markAsRead_owner_setsReadTrue() {
        Notification notification = notification(1L, 100L, false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(100L, 1L);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_notOwner_throwsOwnershipViolationException() {
        Notification notification = notification(1L, 999L, false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(100L, 1L)).isInstanceOf(OwnershipViolationException.class);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_notFound_throwsResourceNotFoundException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(100L, 1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAllAsRead_setsAllUnreadToRead() {
        Notification n1 = notification(1L, 100L, false);
        Notification n2 = notification(2L, 100L, false);
        when(notificationRepository.findAllByUserIdAndReadFalse(100L)).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(100L);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }
}
