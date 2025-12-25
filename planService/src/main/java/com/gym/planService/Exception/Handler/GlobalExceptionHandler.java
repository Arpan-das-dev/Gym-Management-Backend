package com.gym.planService.Exception.Handler;

import com.gym.planService.Exception.Custom.*;
import com.gym.planService.Exception.Model.ErrorResponse;
import com.gym.planService.Exception.Utils.ExceptionUtil;
import com.razorpay.RazorpayException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@Slf4j
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

    @ExceptionHandler({
            CannotAcquireLockException.class,
            PessimisticLockException.class,
            LockTimeoutException.class
    })
    public ResponseEntity<ErrorResponse> handleDuplicatePayments(Exception ex, HttpServletRequest request) {
        log.warn("DB lock contention detected: {}", ex.getMessage());
        return ExceptionUtil.buildErrorResponse(
                HttpStatus.CONFLICT,
                "Payment is already being processed. Please retry shortly.",
                request
        );
    }

    @ExceptionHandler({RazorpayException.class, RevenueLimitExceededException.class})
    public ResponseEntity<ErrorResponse> handleRazorpayExceptions(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler({
            EmailSendFailedException.class,
            CuponCodeCreationException.class,
            InterServiceCommunicationException.class,
            PaymentFailedException.class,
            RefundFailedException.class
    })
    public ResponseEntity<ErrorResponse> handleBusinessFailures(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return ExceptionUtil.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error occurred",
                request
        );
    }
}
