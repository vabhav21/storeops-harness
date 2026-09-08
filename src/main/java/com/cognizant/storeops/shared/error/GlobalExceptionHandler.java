package com.cognizant.storeops.shared.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Single point of translation from AppError -> HTTP response.
 * Routes and services never construct ResponseEntity error bodies themselves;
 * they throw a typed AppError and let this handler do it consistently.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppError.class)
    public ResponseEntity<Map<String, Object>> handleAppError(AppError ex) {
        Map<String, Object> body = Map.of(
                "code", ex.getCode(),
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}
