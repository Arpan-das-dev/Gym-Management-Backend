package com.gym.trainerService.Exception.Handler;


import com.gym.trainerService.Exception.Custom.*;
import com.gym.trainerService.Exception.Model.ErrorResponse;
import com.gym.trainerService.Exception.Util.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoReviewFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(NoReviewFoundException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoTrainerFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(NoTrainerFoundException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoSessionFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(NoSessionFoundException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoSpecialityFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(NoSpecialityFoundException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(MemberNotFoundException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateSpecialtyFoundException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUser(DuplicateSpecialtyFoundException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateTrainerFoundException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUser(DuplicateTrainerFoundException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(PlanExpirationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(PlanExpirationException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }


    @ExceptionHandler(InvalidImageUrlException.class)
    public ResponseEntity<ErrorResponse> handleValidation(InvalidImageUrlException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidMemberException.class)
    public ResponseEntity<ErrorResponse> handleValidation(InvalidMemberException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidReviewException.class)
    public ResponseEntity<ErrorResponse> handleValidation(InvalidReviewException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<ErrorResponse> handleValidation(InvalidSessionException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }
}
