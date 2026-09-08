package com.cognizant.storeops.shared.error;

/** Thrown when a requested entity does not exist. Maps to HTTP 404. */
public class NotFoundError extends AppError {
    public NotFoundError(String entity, Object id) {
        super("NOT_FOUND", entity + " with id " + id + " was not found", 404);
    }
}
