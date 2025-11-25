package com.gym.member_service.Exception.Handler;

import com.gym.member_service.Exception.Exceptions.*;
import com.gym.member_service.Exception.Model.ErrorResponse;
import com.gym.member_service.Exception.Util.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            TrainerNotFoundException.class, UserNotFoundException.class, PlanNotFounException.class,
            NoSessionFoundException.class
    })
    ResponseEntity<ErrorResponse> handleNotFoundException(HttpServletRequest request, Exception ex) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({
            DuplicateUserFoundException.class, TrainerAlreadyExistsException.class
    })
    ResponseEntity<ErrorResponse> handleDuplicateExceptions(HttpServletRequest request, Exception ex) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidImageUrlException.class, InvalidInputDateException.class, InvalidSessionException.class,
            InvalidTrainerException.class, PlanExpiredException.class, MethodArgumentNotValidException.class
    })
    ResponseEntity<ErrorResponse> handleInvalidExceptions(HttpServletRequest request, Exception ex) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({
            Exception.class
    })
    ResponseEntity<ErrorResponse> handleDefaultExceptions (HttpServletRequest request, Exception ex) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage(),request);
    }
}
