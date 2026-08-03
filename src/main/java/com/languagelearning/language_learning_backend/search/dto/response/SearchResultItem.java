package com.languagelearning.language_learning_backend.search.dto.response;

import com.languagelearning.language_learning_backend.search.enums.SearchResultType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Đại diện rút gọn cho 1 kết quả tìm kiếm, thống nhất hình dạng bất kể targetType để FE render 1 danh sách chung. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultItem {

    private SearchResultType type;
    private Long id;
    private String title;
    private String subtitle;
    private String imageUrl;
}
