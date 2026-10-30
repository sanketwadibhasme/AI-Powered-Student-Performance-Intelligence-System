package com.sanket.studentperformanceintelligencesystem.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard API error response")
public class ApiErrorResponse {

    @Schema(
            description = "Time when the error occurred",
            example = "2026-07-18T18:45:12"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "HTTP status code",
            example = "404"
    )
    private int status;

    @Schema(
            description = "HTTP error description",
            example = "Not Found"
    )
    private String error;

    @Schema(
            description = "Detailed error message",
            example = "Student not found with ID 5"
    )
    private String message;

    @Schema(
            description = "Requested API endpoint",
            example = "/api/students/5"
    )
    private String path;

    @Schema(
            description = "Validation errors grouped by field name"
    )
    private Map<String, String> fieldErrors;

    public ApiErrorResponse() {
    }
    public ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
