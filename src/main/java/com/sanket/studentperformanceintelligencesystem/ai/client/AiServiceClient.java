package com.sanket.studentperformanceintelligencesystem.ai.client;

import com.sanket.studentperformanceintelligencesystem.ai.dto.ServiceHealth;
import com.sanket.studentperformanceintelligencesystem.ai.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Thin client for the Python (Flask) AI/ML inference service.
 *
 * Error mapping rules (never hide the cause):
 *  - connection failure / timeout            -> AiServiceException (-> HTTP 503,
 *    "AI service is unavailable"), because the service itself cannot be reached
 *  - HTTP 4xx from the service (e.g. 400 for
 *    a bad feature payload)                   -> AiServiceException (-> HTTP 502,
 *    with the ML service's own message preserved) — the service answered, so
 *    the problem is the data we sent it, not its availability
 */
@Component
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestClient restClient;

    @Value("${ai.service.enabled:true}")
    private boolean aiServiceEnabled;

    public AiServiceClient(RestClient aiRestClient) {
        this.restClient = aiRestClient;
    }

    public boolean isEnabled() {
        return aiServiceEnabled;
    }

    /** Health probe for the AI status indicator; throws AiServiceException when unreachable. */
    @SuppressWarnings("unchecked")
    public ServiceHealth checkHealth() {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(Map.class);
            String status = body != null && "UP".equals(body.get("status")) ? "UP" : "DOWN";
            Map<String, Object> models = body != null && body.get("models") instanceof Map<?, ?> m
                    ? (Map<String, Object>) m
                    : Map.of();
            return new ServiceHealth(status, models, "DOWN".equals(status) ? "ML service reported status DOWN" : null);
        } catch (RestClientException ex) {
            throw new AiServiceException("ML service is not running. Start the AI service and try again.");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> predictPerformance(Map<String, Object> features) {
        return post("/predict/performance", features);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> predictRisk(Map<String, Object> features) {
        return post("/predict/risk", features);
    }

    private Map<String, Object> post(String uri, Map<String, Object> body) {
        log.info("AI service request: POST {}", uri);
        try {
            Map<String, Object> result = restClient
                    .post()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            log.info("AI service responded: POST {} -> 200 OK", uri);
            return result;
        } catch (RestClientResponseException ex) {
            // The ML service answered with an error status: surface its message.
            log.warn("AI service rejected request: POST {} -> HTTP {}: {}",
                    uri, ex.getStatusCode().value(), ex.getResponseBodyAsString());
            String detail = ex.getResponseBodyAsString();
            throw new AiServiceException(
                    "The ML service rejected the prediction request (HTTP " + ex.getStatusCode().value() + ")"
                            + (detail != null && !detail.isBlank() ? ": " + extractError(detail) : ""),
                    ex);
        } catch (RestClientException ex) {
            log.warn("AI service unreachable: POST {} -> {}", uri, ex.getMessage());
            throw new AiServiceException(
                    "AI service is unavailable. Please make sure the ML service is running (" + ex.getMessage() + ")",
                    ex);
        }
    }

    /** Pull the "error" field out of Flask's JSON error body without a full parser. */
    private String extractError(String responseBody) {
        int idx = responseBody.indexOf("\"error\"");
        if (idx < 0) {
            return responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody;
        }
        int colon = responseBody.indexOf(':', idx);
        int start = responseBody.indexOf('"', colon);
        int end = responseBody.indexOf('"', start + 1);
        if (colon < 0 || start < 0 || end < 0) {
            return responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody;
        }
        return responseBody.substring(start + 1, end);
    }
}
