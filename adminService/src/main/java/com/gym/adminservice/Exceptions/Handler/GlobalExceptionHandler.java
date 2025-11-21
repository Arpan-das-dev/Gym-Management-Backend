package com.gym.adminservice.Exceptions.Handler;

import com.gym.adminservice.Exceptions.Custom.MessageNotFoundException;
import com.gym.adminservice.Exceptions.Custom.PlanNotFounException;
import com.gym.adminservice.Exceptions.Custom.RequestNotFoundException;
import com.gym.adminservice.Exceptions.Model.ErrorResponse;
import com.gym.adminservice.Exceptions.Utils.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ExceptionHandler({Exception.class})
    ResponseEntity<ErrorResponse> handleGlobal(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage(),request);
    }
}
