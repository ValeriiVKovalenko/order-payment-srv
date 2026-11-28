package com.valerii.payment.exception.exceptionhandler;

import com.valerii.payment.exception.DataConsistencyException;
import com.valerii.payment.exception.InvalidPaymentUpdateStatusException;
import com.valerii.payment.exception.PaymentNotFoundException;
import com.valerii.payment.exception.InvalidPaymentStatusException;
import com.valerii.payment.model.response.ApiErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
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

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handlePaymentNotFoundException(PaymentNotFoundException ex,
                                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                                      HttpServletRequest request) {
        String errorMessage = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> {
                            String name;
                            if (error instanceof FieldError fieldError) {
                                name = fieldError.getField();
                            } else {
                                name = result.getMethodParameter().getParameterName();
                            }
                            return name + ": " + error.getDefaultMessage();
                        }))
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex,
                                                                                             HttpServletRequest request) {
        String errorMessage = "Required parameter '%s' is missing".formatted(ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(InvalidPaymentUpdateStatusException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidPaymentUpdateStatusException(InvalidPaymentUpdateStatusException ex,
                                                                                         HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<ApiErrorResponseDto> handlePaymentStatusNotFoundException(InvalidPaymentStatusException ex,
                                                                                    HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DataConsistencyException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDataConsistencyException(DataConsistencyException ex,
                                                                              HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
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
