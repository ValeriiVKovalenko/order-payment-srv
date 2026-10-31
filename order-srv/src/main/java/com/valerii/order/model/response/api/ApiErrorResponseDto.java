package com.valerii.order.model.response.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@RequiredArgsConstructor
@Builder
public class ApiErrorResponseDto {
    private final Instant timestamp;
    private final Integer status;
    private final String error;
    private final String message;
    private final String path;

    public static ApiErrorResponseDto of(HttpStatus status, String message, HttpServletRequest request) {
        return new ApiErrorResponseDto(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
    }
}
