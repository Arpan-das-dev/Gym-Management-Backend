package com.gym.adminservice.Exceptions.Handler;

import com.gym.adminservice.Exceptions.Custom.*;
import com.gym.adminservice.Exceptions.Model.ErrorResponse;
import com.gym.adminservice.Exceptions.Utils.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.ExecutionException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RequestNotFoundException.class,
    PlanNotFounException.class, MessageNotFoundException.class})
    ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({ExecutionException.class, InterruptedException.class})
    ResponseEntity<ErrorResponse> handleWebClient(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }
    @ExceptionHandler({InvalidUserException.class, MethodArgumentNotValidException.class,
            MessageLimitReachedException.class,UnauthorizedRequestException.class})
    ResponseEntity<ErrorResponse> handleInvalidRequests(Exception ex, HttpServletRequest request){
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }
    @ExceptionHandler({Exception.class, interServiceCommunicationException.class})
    ResponseEntity<ErrorResponse> handleGlobal(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage(),request);
    }


}
