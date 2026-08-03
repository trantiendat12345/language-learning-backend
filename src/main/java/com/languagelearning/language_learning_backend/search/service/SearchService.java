package com.languagelearning.language_learning_backend.search.service;

import com.languagelearning.language_learning_backend.search.dto.response.SearchResponse;
import com.languagelearning.language_learning_backend.search.enums.SearchResultType;
import org.springframework.data.domain.Pageable;

public interface SearchService {

    /**
     * `type=null` → kết quả gộp/nhóm theo cả 5 loại nội dung (mỗi loại giới hạn số lượng, xem
     * SearchServiceImpl.GROUPED_RESULT_LIMIT), `pageable` bị bỏ qua. `type` cụ thể → chỉ tìm
     * loại đó, có phân trang đầy đủ theo `pageable`. `keyword` rỗng/blank → trả kết quả rỗng
     * có kiểm soát, không truy vấn DB (không lộ toàn bộ dữ liệu hệ thống).
     */
    SearchResponse search(String keyword, SearchResultType type, Pageable pageable);
}
