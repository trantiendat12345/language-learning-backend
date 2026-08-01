package com.languagelearning.language_learning_backend.notification.service;

import com.languagelearning.language_learning_backend.notification.dto.response.NotificationResponse;
import java.util.List;

public interface NotificationService {

    /** Danh sách Notification cá nhân của currentUser, mới nhất trước. Chưa hỗ trợ broadcast (userId=null) - xem Javadoc Notification entity. */
    List<NotificationResponse> getMyNotifications(Long userId);

    /** Ownership check - chỉ chủ sở hữu Notification mới đánh dấu đọc được. */
    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);
}
