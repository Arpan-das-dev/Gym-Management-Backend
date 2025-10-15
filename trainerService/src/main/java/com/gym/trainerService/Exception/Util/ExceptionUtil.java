package com.gym.trainerService.Exception.Util;

import com.gym.trainerService.Exception.Model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
/**
 * Utility class for building standardized {@link ErrorResponse} objects
 * wrapped in {@link ResponseEntity} instances. This class centralizes
 * API error response creation, ensuring all controllers return a
 * consistent error structure across the application.
 *
 * <p>The {@code buildErrorResponse} method can be called by global
 * {@code @ControllerAdvice} exception handlers or service-level
 * components that need to return structured error responses with
 * proper HTTP status codes and contextual request information.</p>
 *
 * <p>This class is designed as a static utility and therefore cannot be
 * instantiated. All functionality is provided through static methods.</p>
 *
 * @author Arpan Das
 * @since 1.0
 */
public class ExceptionUtil {

    /**
     * Private constructor to prevent instantiation.
     * This class is stateless and should not be instantiated.
     */
    private ExceptionUtil() {
    }

    /**
     * Builds a standardized {@link ErrorResponse} wrapped inside a
     * {@link ResponseEntity} using the provided HTTP status, message,
     * and originating request details.
     *
     * <p>Example JSON response:</p>
     * <pre>{@code
     * {
     *   "timestamp": "2025-10-15T08:00:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Member not found",
     *   "path": "/api/members/123"
     * }
     * }</pre>
     *
     * @param status  the {@link HttpStatus} representing the error type
     * @param message a descriptive message explaining the error reason
     * @param request the {@link HttpServletRequest} to extract the request URI
     * @return a {@link ResponseEntity} containing the constructed {@link ErrorResponse}
     */
    public static ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String message, HttpServletRequest request) {

        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }
}
