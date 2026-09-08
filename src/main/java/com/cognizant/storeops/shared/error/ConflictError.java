package com.cognizant.storeops.shared.error;

/** Thrown when an operation conflicts with the current state of an entity. Maps to HTTP 409. */
public class ConflictError extends AppError {
    public ConflictError(String message) {
        super("CONFLICT", message, 409);
    }
}
