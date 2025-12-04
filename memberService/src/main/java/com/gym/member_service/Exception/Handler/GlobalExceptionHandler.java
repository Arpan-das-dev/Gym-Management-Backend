package com.gym.member_service.Exception.Handler;

import com.gym.member_service.Exception.Exceptions.*;
import com.gym.member_service.Exception.Model.ErrorResponse;
import com.gym.member_service.Exception.Util.ExceptionUtil;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
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
            DuplicateUserFoundException.class, TrainerAlreadyExistsException.class,
            DuplicateRequestException.class
    })
    ResponseEntity<ErrorResponse> handleDuplicateExceptions(HttpServletRequest request, Exception ex) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidImageUrlException.class, InvalidInputDateException.class, InvalidSessionException.class,
            InvalidTrainerException.class, PlanExpiredException.class, MethodArgumentNotValidException.class,
            InvalidInputDateException.class, InvalidPrUpdateException.class, UnAuthorizedRequestException.class,
    })
    ResponseEntity<ErrorResponse> handleInvalidExceptions(HttpServletRequest request, Exception ex) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({
            S3Exception.class, SdkClientException.class,
    })
    ResponseEntity<ErrorResponse> handleAwsException(HttpServletRequest request, Exception ex) {
        log.info("an exception 💀💀 occurred due to {}", ex.getLocalizedMessage());
        String message = "Failed To Process The Request Due to AWS Client Error";
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST,message,request);
    }

    @ExceptionHandler({
            Exception.class
    })
    ResponseEntity<ErrorResponse> handleDefaultExceptions (HttpServletRequest request, Exception ex) {
        String message = "Failed To Process Current Request Due to Internal Issue";
        log.info("💀💀 Exception occurred due to {}",ex.getLocalizedMessage());
        return ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,message,request);
    }


}
