package com.valerii.order.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PageRequestParamDto {

    private final Integer page;
    private final Integer size;

    public Long getOffset() {
        return (long) page * size;
    }
}
