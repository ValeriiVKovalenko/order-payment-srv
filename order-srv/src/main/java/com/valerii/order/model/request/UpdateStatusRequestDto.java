package com.valerii.order.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class UpdateStatusRequestDto {
    @NotBlank(message = "Status must be not an empty line")
    private final String status;
}
