package com.valerii.order.exception.exceptionhandler;

import com.valerii.order.exception.*;
import com.valerii.order.model.response.api.ApiErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                     HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(InvalidCreateOrderCommandException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidCreateOrderCommandException(InvalidCreateOrderCommandException ex,
                                                                                        HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DomainInvariantViolationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidDomainInvariantViolationException(DomainInvariantViolationException ex,
                                                                                        HttpServletRequest request) {
        log.error("Domain invariant violated: {}", ex.getMessage(), ex);
        return  build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", request);
    }

    @ExceptionHandler(InvalidOrderAmountException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidOrderAmountException(InvalidOrderAmountException ex,
                                                                                              HttpServletRequest request) {
        log.warn("Invalid order amount: {}", ex.getMessage());
        return  build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                                                                             HttpServletRequest request) {
        String errorMessage = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidOrderStatusException(InvalidOrderStatusException ex,
                                                                                 HttpServletRequest request) {
        log.error("Invalid order status: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
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
