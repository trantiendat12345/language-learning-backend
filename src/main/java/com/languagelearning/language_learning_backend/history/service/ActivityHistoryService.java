package com.languagelearning.language_learning_backend.history.service;

import com.languagelearning.language_learning_backend.history.dto.response.ActivityHistoryResponse;
import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import com.languagelearning.language_learning_backend.history.enums.ActivityTargetType;
import java.util.List;

public interface ActivityHistoryService {

    /**
     * Ghi 1 dòng ActivityHistory (append-only) - gọi từ Course view/Lesson complete/Review
     * submit. KHÔNG idempotent - mỗi lần gọi tạo 1 dòng mới (xem nhiều lần = nhiều dòng), khác
     * hẳn Favorite (idempotent).
     */
    void recordActivity(Long userId, ActivityTargetType targetType, Long targetId, ActivityAction action);

    /**
     * Danh sách hoạt động gần đây của currentUser, mới nhất trước, giới hạn `limit` bản ghi,
     * lọc theo `action` nếu có. Dòng mà đối tượng gốc đã bị xoá mềm/không còn tồn tại vẫn hiển
     * thị (chỉ `title=null`) - khác Favorite (ẩn hẳn), vì đây là log lịch sử "đã xảy ra".
     */
    List<ActivityHistoryResponse> getMyHistory(Long userId, ActivityAction action, int limit);
}
