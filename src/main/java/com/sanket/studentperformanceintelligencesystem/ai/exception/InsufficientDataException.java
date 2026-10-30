package com.sanket.studentperformanceintelligencesystem.ai.exception;

/**
 * Thrown when a prediction is requested but the student's academic history
 * cannot support one (no assessments recorded and no what-if scores given).
 * Mapped to HTTP 422 so the UI can say "not enough academic data" instead of
 * showing a fabricated prediction.
 */
public class InsufficientDataException extends RuntimeException {
    public InsufficientDataException(String message) {
        super(message);
    }
}
