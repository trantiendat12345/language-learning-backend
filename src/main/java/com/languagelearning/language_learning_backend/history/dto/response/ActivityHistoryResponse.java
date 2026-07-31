package com.languagelearning.language_learning_backend.history.dto.response;

import com.languagelearning.language_learning_backend.history.enums.ActivityAction;
import com.languagelearning.language_learning_backend.history.enums.ActivityTargetType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** title resolve từ entity gốc theo targetType tại thời điểm gọi API (không denormalize - D1) - null nếu đối tượng gốc đã bị xoá mềm/không còn tồn tại, KHÔNG ẩn dòng log (khác Favorite). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityHistoryResponse {

    private Long id;
    private ActivityTargetType targetType;
    private Long targetId;
    private String title;
    private ActivityAction action;
    private LocalDateTime occurredAt;
}
