package com.cognizant.storeops.shared.error;

/** Thrown when request input fails business-rule or field validation. Maps to HTTP 400. */
public class ValidationError extends AppError {
    public ValidationError(String message) {
        super("VALIDATION_ERROR", message, 400);
    }
}
