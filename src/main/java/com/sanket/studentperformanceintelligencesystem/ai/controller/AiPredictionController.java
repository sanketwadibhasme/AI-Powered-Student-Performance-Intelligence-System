package com.sanket.studentperformanceintelligencesystem.ai.controller;

import com.sanket.studentperformanceintelligencesystem.ai.client.AiServiceClient;
import com.sanket.studentperformanceintelligencesystem.ai.dto.PredictionRequest;
import com.sanket.studentperformanceintelligencesystem.ai.dto.PredictionResponse;
import com.sanket.studentperformanceintelligencesystem.ai.dto.ServiceHealth;
import com.sanket.studentperformanceintelligencesystem.ai.exception.AiServiceException;
import com.sanket.studentperformanceintelligencesystem.ai.service.AiPredictionService;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.security.user.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI prediction endpoints.
 *
 * - ADMIN / FACULTY can predict for any student via /api/ai/students/{id}
 * - STUDENT can only predict for themselves via /api/ai/me (studentId is
 *   always resolved from the JWT, never from a request parameter)
 *
 * POST bodies are optional. A body with feature values is treated as a
 * what-if simulation; an empty body predicts from the student's real data.
 * Sending g1 without g2 (or vice versa) is rejected with 400.
 */
@Tag(
        name = "AI Predictions",
        description = "AI-powered student performance prediction and academic-risk prediction using trained ML models."
)
@RestController
@RequestMapping("/api/ai")
public class AiPredictionController {

    private final AiPredictionService aiPredictionService;
    private final AiServiceClient aiServiceClient;

    public AiPredictionController(AiPredictionService aiPredictionService, AiServiceClient aiServiceClient) {
        this.aiPredictionService = aiPredictionService;
        this.aiServiceClient = aiServiceClient;
    }

    private Long resolveStudentId(CustomUserDetails principal, Long requestedStudentId) {
        if ("STUDENT".equals(principal.getAppUser().getRole().name())) {
            var student = principal.getAppUser().getStudent();
            if (student == null) {
                throw new ResourceNotFoundException(
                        "No student record is linked to this account. Contact the administrator.");
            }
            return student.getStudentId();
        }
        if (requestedStudentId == null) {
            throw new ResourceNotFoundException("studentId is required");
        }
        return requestedStudentId;
    }

    /**
     * A request body is a what-if simulation only when it carries at least
     * one feature override. An empty body means "use the student's real data".
     */
    private boolean isSimulation(PredictionRequest request) {
        return request != null && (
                request.getG1() != null || request.getG2() != null
                        || request.getStudyTime() != null || request.getPastFailures() != null
                        || request.getAbsences() != null);
    }

    private void validatePartialWhatIf(PredictionRequest request) {
        if (request != null
                && (request.getG1() == null) != (request.getG2() == null)) {
            throw new IllegalArgumentException(
                    "What-if simulation requires both G1 and G2 (or leave both empty to use the student's real assessments).");
        }
    }

    @Operation(
            summary = "AI service health",
            description = "Checks whether the Python ML inference service is up and both models are loaded. ADMIN/FACULTY/STUDENT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ML service is up"),
            @ApiResponse(responseCode = "502", description = "ML service is down or unhealthy")
    })
    @GetMapping("/health")
    public ResponseEntity<ServiceHealth> health() {
        try {
            ServiceHealth h = aiServiceClient.checkHealth();
            return ResponseEntity.ok(h);
        } catch (AiServiceException ex) {
            ServiceHealth h = new ServiceHealth("DOWN", Map.of("performance", false, "risk", false), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(h);
        }
    }

    @Operation(
            summary = "Combined AI insights for a student (real data)",
            description = "Runs both models on the student's real academic history. An empty POST body is the normal usage; a body with feature values is treated as a what-if simulation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prediction generated successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "502", description = "ML service returned an invalid response"),
            @ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    @PostMapping("/students/{studentId}/insights")
    public ResponseEntity<PredictionResponse> getStudentInsights(
            @PathVariable Long studentId,
            @RequestBody(required = false) PredictionRequest request) {

        validatePartialWhatIf(request);
        PredictionResponse response = isSimulation(request)
                ? aiPredictionService.simulateForStudent(studentId, request)
                : aiPredictionService.predictForStudent(studentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "What-if simulation for a student",
            description = "Explicit simulation endpoint: feature overrides are applied on top of the student's real data. Does not modify any academic records."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulation generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or partial what-if inputs"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "502", description = "ML service returned an invalid response"),
            @ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    @PostMapping("/students/{studentId}/simulate")
    public ResponseEntity<PredictionResponse> simulateForStudent(
            @PathVariable Long studentId,
            @RequestBody PredictionRequest request) {

        if (request == null
                || (request.getG1() == null && request.getG2() == null
                        && request.getStudyTime() == null && request.getPastFailures() == null
                        && request.getAbsences() == null)) {
            throw new IllegalArgumentException(
                    "Provide at least one what-if value (g1, g2, studyTime, pastFailures or absences).");
        }
        validatePartialWhatIf(request);
        return ResponseEntity.ok(aiPredictionService.simulateForStudent(studentId, request));
    }

    @Operation(
            summary = "My AI insights (student self-service)",
            description = "Returns performance and risk predictions for the currently authenticated student. The student ID is resolved from the JWT; a body with feature values is an optional what-if simulation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prediction generated successfully"),
            @ApiResponse(responseCode = "404", description = "No student record linked to this account"),
            @ApiResponse(responseCode = "502", description = "ML service returned an invalid response"),
            @ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    @PostMapping("/me/insights")
    public ResponseEntity<PredictionResponse> getMyInsights(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody(required = false) PredictionRequest request) {

        Long studentId = resolveStudentId(principal, null);
        validatePartialWhatIf(request);
        PredictionResponse response = isSimulation(request)
                ? aiPredictionService.simulateForStudent(studentId, request)
                : aiPredictionService.predictForStudent(studentId, request);
        return ResponseEntity.ok(response);
    }
}
