package com.valerii.payment.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequestDto {

    @NotBlank(message = "must be not an empty line")
    private String status;
}
