package com.languagelearning.language_learning_backend.search.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Khi `type` được truyền: chỉ list tương ứng có dữ liệu (phân trang theo `page`/`size`/
 * `totalElements`/`totalPages`), 4 list còn lại rỗng (không truy vấn). Khi `type` bỏ trống:
 * cả 5 list đều có dữ liệu, mỗi list giới hạn tối đa `SearchServiceImpl.GROUPED_RESULT_LIMIT`
 * kết quả (không phân trang sâu - quyết định chốt khi code, FRS chỉ yêu cầu "trả kết quả
 * gộp/nhóm theo từng loại nội dung" không cho cơ chế phân trang cụ thể cho chế độ gộp),
 * `page`/`size`/`totalElements`/`totalPages` đều `null`.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private List<SearchResultItem> courses;
    private List<SearchResultItem> lessons;
    private List<SearchResultItem> vocabularies;
    private List<SearchResultItem> grammars;
    private List<SearchResultItem> decks;

    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
