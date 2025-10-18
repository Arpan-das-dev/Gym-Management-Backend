package com.gym.planService.Exception.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

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
