package com.gym.authservice.Exceptions.Util;

import com.gym.authservice.Exceptions.Model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

public class ExceptionUtil {
    private ExceptionUtil() {
    }
    public static ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String message, ServerWebExchange exchange) {

        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        exchange.getRequest().getPath().toString()
                ));
    }
}
