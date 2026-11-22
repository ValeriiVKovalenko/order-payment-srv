package com.valerii.payment.exception.exceptionhandler;

import com.valerii.payment.model.response.ApiErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                                                         HttpServletRequest request) {
        String errorMessage = String.format("Invalid value `%s` for parameter `%s`", ex.getValue(), ex.getName());
        return build(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                     HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
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
