package com.sanket.studentperformanceintelligencesystem.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal accumulator for the real-data feature extraction performed by
 * {@link com.sanket.studentperformanceintelligencesystem.ai.service.AiPredictionService}.
 * Not exposed over the API directly — its values feed PredictionResponse's
 * data-quality fields.
 */
public class StudentAiSummary {

    @Schema(description = "Student the extraction was performed for", example = "1")
    private Long studentId;

    @Schema(description = "Chronologically second-most-recent assessment score (0-100), or null when none exists")
    private Double g1;

    @Schema(description = "Most recent assessment score (0-100), or null when none exists")
    private Double g2;

    @Schema(description = "Number of classes missed (total attendance rows minus present rows)", example = "3")
    private Integer absences;

    @Schema(description = "Total attendance records for the student", example = "42")
    private long totalClasses;

    @Schema(description = "Number of recorded assessments (marks rows)", example = "8")
    private int assessmentCount;

    @Schema(description = "SUFFICIENT_DATA, LIMITED_DATA or INSUFFICIENT_DATA", example = "SUFFICIENT_DATA")
    private String dataSufficiency = "INSUFFICIENT_DATA";

    @Schema(description = "Simple data-quality label: Good, Fair or Poor", example = "Good")
    private String dataQuality = "Poor";

    @Schema(description = "Which assessment supplied G1 (exam name / subject / date)", example = "Mid Semester (CS301) on 2026-09-15")
    private String g1Source;

    @Schema(description = "Which assessment supplied G2", example = "Internal Assessment 2 (CS301) on 2026-08-25")
    private String g2Source;

    @Schema(description = "Human-readable list of the data the prediction was based on")
    private List<String> dataBasis = new ArrayList<>();

    @Schema(description = "Features that fell back to documented dataset-mean defaults", example = "[\"studyTime\"]")
    private List<String> defaultedFields = new ArrayList<>();

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

    public Integer getAbsences() {
        return absences;
    }

    public void setAbsences(Integer absences) {
        this.absences = absences;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(long totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getAssessmentCount() {
        return assessmentCount;
    }

    public void setAssessmentCount(int assessmentCount) {
        this.assessmentCount = assessmentCount;
    }

    public String getDataSufficiency() {
        return dataSufficiency;
    }

    public void setDataSufficiency(String dataSufficiency) {
        this.dataSufficiency = dataSufficiency;
    }

    public String getDataQuality() {
        return dataQuality;
    }

    public void setDataQuality(String dataQuality) {
        this.dataQuality = dataQuality;
    }

    public String getG1Source() {
        return g1Source;
    }

    public void setG1Source(String g1Source) {
        this.g1Source = g1Source;
    }

    public String getG2Source() {
        return g2Source;
    }

    public void setG2Source(String g2Source) {
        this.g2Source = g2Source;
    }

    public List<String> getDataBasis() {
        return dataBasis;
    }

    public void setDataBasis(List<String> dataBasis) {
        this.dataBasis = dataBasis;
    }

    public List<String> getDefaultedFields() {
        return defaultedFields;
    }

    public void setDefaultedFields(List<String> defaultedFields) {
        this.defaultedFields = defaultedFields;
    }
}
