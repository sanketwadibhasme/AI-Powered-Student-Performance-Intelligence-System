package com.sanket.studentperformanceintelligencesystem.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request payload for the AI performance/risk prediction endpoints.
 *
 * All feature fields are OPTIONAL. Omitted fields fall back to the
 * student's real academic data (chronological G1/G2, attendance-derived
 * absences) and, for features the database does not track (studyTime,
 * pastFailures), to documented dataset-mean defaults that are surfaced in
 * the response's {@code defaultedFields}.
 *
 * A body containing ONLY what-if overrides (g1/g2/...) is a simulation.
 * For a valid simulation g1 and g2 must be provided together or not at all.
 *
 * The scores are on a 0-100 scale (application scale); the AI service maps
 * them onto its 0-20 UCI dataset scale internally.
 *
 * For a STUDENT caller, studentId is ignored and resolved from the JWT.
 */
@Schema(description = "Feature inputs for AI performance and academic-risk predictions")
public class PredictionRequest {

    @Schema(description = "Student to predict for (ignored for student callers; resolved from JWT)", example = "1")
    private Long studentId;

    @Min(value = 0, message = "G1 must be between 0 and 100")
    @Max(value = 100, message = "G1 must be between 0 and 100")
    @Schema(description = "Optional what-if first-assessment score (0-100). Omit to use the student's real chronological G1.", example = "70")
    private Double g1;

    @Min(value = 0, message = "G2 must be between 0 and 100")
    @Max(value = 100, message = "G2 must be between 0 and 100")
    @Schema(description = "Optional what-if recent-assessment score (0-100). Omit to use the student's real chronological G2.", example = "75")
    private Double g2;

    @Min(value = 0, message = "Study time category must be between 0 and 4")
    @Max(value = 4, message = "Study time category must be between 0 and 4")
    @Schema(description = "Optional what-if weekly study time category (1: <2h, 2: 2-5h, 3: 5-10h, 4: >10h). Omit to use the documented dataset-mean default (2).", example = "3")
    private Integer studyTime;

    @Min(value = 0, message = "Previous failures cannot be negative")
    @Max(value = 10, message = "Previous failures cannot exceed 10")
    @Schema(description = "Optional what-if number of previous subject failures. Omit to use the documented dataset-mean default (0).", example = "0")
    private Integer pastFailures;

    @Min(value = 0, message = "Absences cannot be negative")
    @Schema(description = "Optional what-if number of classes missed. Omit to use the student's real attendance record.", example = "4")
    private Integer absences;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getG1() {
        return g1;
    }

    public void setG1(Double g1) {
        this.g1 = g1;
    }

    public Double getG2() {
        return g2;
    }

    public void setG2(Double g2) {
        this.g2 = g2;
    }

    public Integer getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(Integer studyTime) {
        this.studyTime = studyTime;
    }

    public Integer getPastFailures() {
        return pastFailures;
    }

    public void setPastFailures(Integer pastFailures) {
        this.pastFailures = pastFailures;
    }

    public Integer getAbsences() {
        return absences;
    }

    public void setAbsences(Integer absences) {
        this.absences = absences;
    }
}
