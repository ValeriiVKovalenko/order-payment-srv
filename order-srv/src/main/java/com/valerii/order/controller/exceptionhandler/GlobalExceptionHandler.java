package com.valerii.order.controller.exceptionhandler;

import com.valerii.order.exception.OrderNotFoundException;
import com.valerii.order.model.response.api.ApiErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                                                         HttpServletRequest request) {
        String errorMessage = String.format("Invalid value `%s` for parameter `%s`", ex.getValue(), ex.getName());
        return build(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    private ResponseEntity<ApiErrorResponseDto> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponseDto.of(
                        status,
                        message,
                        request
                ));
    }
}
