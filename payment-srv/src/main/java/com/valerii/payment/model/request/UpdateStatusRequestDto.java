package com.valerii.payment.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateStatusRequestDto {

    @NotBlank(message = "must be not an empty line")
    private final String status;
}
