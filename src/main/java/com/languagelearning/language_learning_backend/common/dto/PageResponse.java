package com.languagelearning.language_learning_backend.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * Cấu trúc dữ liệu phân trang dùng chung cho mọi API danh sách - bọc bên trong
 * {@code ApiResponse<PageResponse<T>>} để Frontend luôn nhận cùng 1 hình dạng dữ liệu
 * phân trang, không phụ thuộc trực tiếp vào kiểu {@code Page<T>} nội bộ của Spring Data.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /** Chuyển đổi từ {@code Page<T>} của Spring Data sang PageResponse để trả ra API. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
