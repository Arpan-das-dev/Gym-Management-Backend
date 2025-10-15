package com.gym.trainerService.Exception.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * Represents a standardized error response returned by the system
 * whenever an API request fails due to validation errors, exceptions,
 * or other client/server side issues.
 *
 * <p>This class encapsulates all relevant details about an error
 * including the timestamp, HTTP status code, error type, a descriptive
 * message, and the request path that triggered the error. It is useful
 * for ensuring consistent JSON response formatting across the
 * application’s error handling layer.</p>
 *
 * <p>Typical usage involves using this as a response body within a
 * {@code @ControllerAdvice}-based global exception handler or
 * service‑level error response generator.</p>
 *
 * @author Arpan Das
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    /**
     * The exact time when the error occurred, represented in ISO‑8601 format.
     */
    private LocalDateTime timestamp;

    /**
     * The HTTP status code representing the type of error (e.g., 404, 500).
     */
    private int status;

    /**
     * The short error reason phrase or category (e.g., "Bad Request", "Internal Server Error").
     */
    private String error;

    /**
     * A more detailed explanation or custom message describing the cause of the error.
     */
    private String message;

    /**
     * The exact path or endpoint where the error originated, useful for debugging.
     */
    private String path;
}
