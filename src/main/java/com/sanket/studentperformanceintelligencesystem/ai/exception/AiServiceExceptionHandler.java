package com.sanket.studentperformanceintelligencesystem.ai.exception;

import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Maps AI-service failures to distinct statuses so the frontend can show
 * the real reason instead of a generic failure:
 *
 *  - 503 SERVICE_UNAVAILABLE: the ML service could not be reached at all
 *    (not running, connection refused, timeout)
 *  - 502 BAD_GATEWAY: the ML service answered with an error (e.g. it
 *    rejected the feature payload) — its own message is preserved
 */
@RestControllerAdvice
public class AiServiceExceptionHandler {

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAiServiceException(
            AiServiceException ex,
            HttpServletRequest request) {

        boolean serviceRejectedRequest = ex.getCause()
                instanceof org.springframework.web.client.RestClientResponseException;

        HttpStatus status = serviceRejectedRequest
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.SERVICE_UNAVAILABLE;

        ApiErrorResponse errorResponse = new ApiErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorResponse, status);
    }
}
