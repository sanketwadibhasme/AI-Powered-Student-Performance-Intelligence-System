package com.sanket.studentperformanceintelligencesystem.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Structured AI prediction result returned by the /api/ai endpoints.
 * Contains both performance prediction and academic-risk prediction,
 * the feature vector actually sent to the model, and an explicit data
 * sufficiency assessment so the UI never presents a weak prediction as
 * if it were authoritative.
 */
@Schema(description = "AI prediction result for a student")
public class PredictionResponse {

    @Schema(description = "Student the prediction belongs to", example = "1")
    private Long studentId;

    @Schema(description = "Predicted final score on a 0-100 scale", example = "78.4")
    private Double predictedScore;

    @Schema(description = "Performance category: EXCELLENT, GOOD, AVERAGE or POOR", example = "GOOD")
    private String performanceCategory;

    @Schema(description = "Academic risk level: LOW, MEDIUM or HIGH", example = "LOW")
    private String riskLevel;

    @Schema(description = "Model probability of academic risk, between 0 and 1. This is a model output, not a calibrated confidence.", example = "0.14")
    private Double riskProbability;

    @Schema(description = "Factors considered by the model, phrased as comparisons against the training-dataset reference levels")
    private List<String> majorFactors;

    @Schema(description = "The exact feature vector sent to the ML models (G1/G2 on the 0-20 dataset scale)")
    private Map<String, Object> featuresUsed;

    @Schema(description = "SUFFICIENT_DATA, LIMITED_DATA or INSUFFICIENT_DATA — how much real academic history backed this prediction", example = "SUFFICIENT_DATA")
    private String dataSufficiency;

    @Schema(description = "Simple data-quality label derived from the available history: Good, Fair or Poor", example = "Good")
    private String dataQuality;

    @Schema(description = "Human-readable description of the academic data the prediction was based on")
    private List<String> dataBasis;

    @Schema(description = "Features that used documented dataset-mean defaults because the system does not track them", example = "[\"studyTime\"]")
    private List<String> defaultedFields;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getPredictedScore() {
        return predictedScore;
    }

    public void setPredictedScore(Double predictedScore) {
        this.predictedScore = predictedScore;
    }

    public String getPerformanceCategory() {
        return performanceCategory;
    }

    public void setPerformanceCategory(String performanceCategory) {
        this.performanceCategory = performanceCategory;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Double getRiskProbability() {
        return riskProbability;
    }

    public void setRiskProbability(Double riskProbability) {
        this.riskProbability = riskProbability;
    }

    public List<String> getMajorFactors() {
        return majorFactors;
    }

    public void setMajorFactors(List<String> majorFactors) {
        this.majorFactors = majorFactors;
    }

    public Map<String, Object> getFeaturesUsed() {
        return featuresUsed;
    }

    public void setFeaturesUsed(Map<String, Object> featuresUsed) {
        this.featuresUsed = featuresUsed;
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
