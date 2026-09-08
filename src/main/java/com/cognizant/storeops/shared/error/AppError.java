package com.cognizant.storeops.shared.error;

/**
 * Base class for all StoreOps application errors.
 *
 * Architecture rule (non-negotiable): no raw {@code throw new Error(...)} /
 * {@code RuntimeException} is permitted in services or routes. Every thrown
 * error in a service or route MUST be a subclass of AppError so that the
 * global exception handler can map it deterministically to an HTTP status
 * and a machine-readable error code for API consumers.
 */
public abstract class AppError extends RuntimeException {

    private final String code;
    private final int statusCode;

    protected AppError(String code, String message, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
