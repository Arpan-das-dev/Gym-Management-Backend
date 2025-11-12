package com.gym.planService.Exception.Handler;

import com.gym.planService.Exception.Custom.*;
import com.gym.planService.Exception.Model.ErrorResponse;
import com.gym.planService.Exception.Utils.ExceptionUtil;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            PlanNotFoundException.class,
            CuponCodeNotFoundException.class,
            PaymentNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({
            DuplicatePlanFoundException.class,
            DuplicateCuponCodeFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(RazorpayException.class)
    public ResponseEntity<ErrorResponse> handleRazorpayExceptions(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,ex.getMessage(),request);
    }

    @ExceptionHandler({Exception.class,
            EmailSendFailedException.class,
            CuponCodeCreationException.class})
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }
}
