package com.valerii.order.model.response.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class PageResponseDto<T> {

    private final Integer size;
    private final Integer page;
    private final Integer totalElements;
    private final Integer totalPages;
    private final Boolean hasNext;
    private final Boolean hasPrevious;
    private final List<?> content;

    public static <T> PageResponseDto<T> of(List<T> content, Integer size, Integer page, Integer totalElements) {
        int totalPages = (totalElements + size - 1) / size;

        return PageResponseDto.<T>builder()
                .content(content)
                .size(size)
                .page(page)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page + 1 <  totalPages)
                .hasPrevious(page > 0)
                .build();
    }
}
