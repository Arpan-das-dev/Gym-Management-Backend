package com.gym.trainerService.Exception.Handler;


import com.gym.trainerService.Exception.Custom.*;
import com.gym.trainerService.Exception.Model.ErrorResponse;
import com.gym.trainerService.Exception.Util.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global Exception Handler for Trainer Service.
 *
 * <p>
 * This class provides centralized exception handling across the entire Spring application.
 * It captures and processes all custom and runtime exceptions thrown by controllers or service layers,
 * converting them into standardized HTTP error responses.
 * </p>
 *
 * <p><b>Annotations Used:</b></p>
 * <ul>
 *   <li>{@code @ControllerAdvice} – Marks this class as a global interceptor for handling exceptions across controllers.</li>
 *   <li>{@code @ExceptionHandler} – Maps specific exceptions to handler methods.</li>
 * </ul>
 *
 * <p><b>Design Goals:</b></p>
 * <ul>
 *   <li>Ensure consistent and meaningful error responses across all APIs.</li>
 *   <li>Prevent internal exception details from leaking to clients.</li>
 *   <li>Provide HTTP status codes aligned with REST best practices (e.g., 404, 400, 409, etc.).</li>
 * </ul>
 *
 * <p>
 * The {@link ExceptionUtil} helper class is responsible for constructing {@link ErrorResponse}
 * objects and logging error metadata.
 * </p>
 *
 * @author Arpan Das
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles cases where reviews, trainers, sessions, or specialties are not found.
     */
    @ExceptionHandler({
            NoReviewFoundException.class,
            NoTrainerFoundException.class,
            NoSessionFoundException.class,
            NoSpecialityFoundException.class,
            MemberNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles duplicate data scenarios such as attempting to add an already existing trainer or specialty.
     */
    @ExceptionHandler({
            DuplicateSpecialtyFoundException.class,
            DuplicateTrainerFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles authorization-related exceptions, such as expired plans.
     */
    @ExceptionHandler(PlanExpirationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(PlanExpirationException ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * Handles validation-related exceptions like invalid data or malformed input.
     */
    @ExceptionHandler({
            InvalidImageUrlException.class,
            InvalidMemberException.class,
            InvalidReviewException.class,
            InvalidSessionException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Fallback handler for any unhandled exceptions.
     * Ensures clients receive a generic error response instead of a stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, HttpServletRequest request) {
        return ExceptionUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }
}
