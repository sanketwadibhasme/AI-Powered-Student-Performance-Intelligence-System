package com.sanket.studentperformanceintelligencesystem.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Health status of the Python ML inference service, as returned by its
 * /health endpoint. Exposed to the frontend through GET /api/ai/health so
 * the UI can show an AI service status indicator before running predictions.
 */
@Schema(description = "Health status of the Python ML inference service")
public class ServiceHealth {

    @Schema(description = "UP when the service responds and both models are loaded, DOWN otherwise", example = "UP")
    private String status;

    @Schema(description = "Per-model load status", example = "{\"performance\": true, \"risk\": true}")
    private Map<String, Object> models;

    @Schema(description = "Error detail when the service is DOWN", example = "Connection refused")
    private String error;

    public ServiceHealth() {
    }

    public ServiceHealth(String status, Map<String, Object> models, String error) {
        this.status = status;
        this.models = models;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getModels() {
        return models;
    }

    public void setModels(Map<String, Object> models) {
        this.models = models;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
