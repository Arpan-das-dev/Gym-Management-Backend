package com.gym.authservice.Exceptions.Handler;

import com.gym.authservice.Exceptions.Custom.*;
import com.gym.authservice.Exceptions.Model.ErrorResponse;
import com.gym.authservice.Exceptions.Util.ExceptionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>>  handleUserNotFound(UserNotFoundException ex, ServerWebExchange exchange) {
        return Mono.just(ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), exchange));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public Mono<ResponseEntity<ErrorResponse>>  handleInvalidOtp(InvalidOtpException ex, ServerWebExchange exchange) {
        return Mono.just(ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateUser(DuplicateUserException ex, ServerWebExchange exchange) {
        return Mono.just(ExceptionUtil.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), exchange));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnauthorized(UnauthorizedAccessException ex, ServerWebExchange exchange) {
        return Mono.just(ExceptionUtil.buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), exchange));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTokenExpired(TokenExpiredException ex, ServerWebExchange exchange) {
        return Mono.just(ExceptionUtil.buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), exchange));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(MethodArgumentNotValidException ex, ServerWebExchange exchange) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Mono.just( ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, message, exchange));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGlobal(Exception ex, ServerWebExchange exchange) {
        return Mono.just(ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), exchange));
    }
}
