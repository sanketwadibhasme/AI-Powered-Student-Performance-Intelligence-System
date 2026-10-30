package com.sanket.studentperformanceintelligencesystem.exception;

/**
 * Thrown when a write operation would create a duplicate record
 * (e.g. attendance already taken for a student/subject/date).
 * Mapped to HTTP 409 CONFLICT by the GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
