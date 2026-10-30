package com.sanket.studentperformanceintelligencesystem.ai.service;

import com.sanket.studentperformanceintelligencesystem.ai.client.AiServiceClient;
import com.sanket.studentperformanceintelligencesystem.ai.dto.PredictionRequest;
import com.sanket.studentperformanceintelligencesystem.ai.dto.PredictionResponse;
import com.sanket.studentperformanceintelligencesystem.entity.Exam;
import com.sanket.studentperformanceintelligencesystem.entity.Marks;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.AttendanceRepository;
import com.sanket.studentperformanceintelligencesystem.repository.MarksRepository;
import com.sanket.studentperformanceintelligencesystem.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates AI predictions for a student.
 *
 * <h3>Feature mapping (college data -> UCI model features)</h3>
 * The models were trained on the UCI Student Performance dataset
 * (ml/data/student-mat.csv, 0-20 grade scale). The college database stores
 * marks as percentages per exam. The mapping below is the single source of
 * truth for converting academic records into model inputs; see
 * docs/ML_FEATURE_MAPPING.md for the full explanation.
 *
 * <pre>
 *   College academic data                Model feature
 *   -------------------------------------------------------------
 *   second-most-recent assessment (%) ->  G1   (scaled to 0-20)
 *   most-recent assessment (%)        ->  G2   (scaled to 0-20)
 *   attendance: classes missed        ->  absences (raw count)
 *   (not tracked in the DB)           ->  studytime (dataset mean 2, labelled)
 *   (not tracked in the DB)           ->  failures  (dataset mean 0, labelled)
 * </pre>
 *
 * "Most recent" is resolved by exam date (then marks record id as a stable
 * tiebreaker) — never by sorting scores numerically. When the college data
 * cannot support a meaningful prediction the service reports
 * INSUFFICIENT_DATA instead of silently substituting plausible defaults.
 *
 * G1/G2 here are chronological assessments of the same student across
 * subjects — an approximation of the UCI same-subject G1->G2->G3 sequence,
 * which is documented as a model limitation.
 */
@Service
public class AiPredictionService {

    private static final Logger log = LoggerFactory.getLogger(AiPredictionService.class);

    private final AiServiceClient aiServiceClient;
    private final MarksRepository marksRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AiPredictionService(
            AiServiceClient aiServiceClient,
            StudentRepository studentRepository,
            MarksRepository marksRepository,
            AttendanceRepository attendanceRepository) {
        this.aiServiceClient = aiServiceClient;
        this.studentRepository = studentRepository;
        this.marksRepository = marksRepository;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * Combined prediction (performance + risk) for one student.
     *
     * @param request optional what-if overrides; explicit values take
     *                precedence over the student's real data, and any field
     *                the caller leaves null keeps the real/derived value.
     * @param applyWhatIf when true, overrides from {@code request} are applied
     *                    on top of the real data (simulation mode).
     */
    public PredictionResponse predictForStudent(Long studentId, PredictionRequest request, boolean applyWhatIf) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID " + studentId));

        // ---- gather real academic data with targeted queries (no full scans) ----
        List<Marks> marksRows = marksRepository.findByStudentStudentIdOrderByExamExamDateAscMarksIdAsc(studentId);
        long totalClasses = attendanceRepository.countTotalClassesByStudentId(studentId);
        long presentClasses = attendanceRepository.countPresentClassesByStudentId(studentId);

        StudentAiSummary summary = buildSummary(studentId, marksRows, totalClasses, presentClasses);
        PredictionRequest effective = buildEffectiveRequest(summary, request, applyWhatIf);

        // No real assessments and no what-if scores: refuse to fabricate a
        // prediction. The UI shows "insufficient data" instead of a number.
        // (Checked before feature building: G1/G2 may legitimately be null here.)
        if (effective.getG1() == null || effective.getG2() == null) {
            throw new com.sanket.studentperformanceintelligencesystem.ai.exception.InsufficientDataException(
                    "Not enough academic data available for a reliable prediction. Record at least one assessment (marks entry) for this student first.");
        }

        Map<String, Object> features = toFeatureMap(effective);

        // Minimal diagnostic logging for the AI flow (never logs credentials).
        log.info("AI prediction started for student {} (sufficiency={}, basis={})",
                studentId, summary.dataSufficiency, summary.dataBasis);
        log.info("AI features for student {}: {}", studentId, features);
        try {
            Map<String, Object> perf = aiServiceClient.predictPerformance(features);
            Map<String, Object> risk = aiServiceClient.predictRisk(features);

            PredictionResponse response = new PredictionResponse();
            response.setStudentId(studentId);
            response.setPredictedScore(asDouble(perf.get("predictedScore")));
            response.setPerformanceCategory((String) perf.get("performanceCategory"));
            response.setRiskLevel((String) risk.get("riskLevel"));
            response.setRiskProbability(asDouble(risk.get("riskProbability")));
            response.setMajorFactors(firstNonEmpty(asStringList(risk.get("majorFactors")), asStringList(perf.get("majorFactors"))));
            response.setFeaturesUsed(features);
            response.setDataSufficiency(summary.dataSufficiency);
            response.setDataQuality(summary.dataQuality);
            response.setDataBasis(summary.dataBasis);
            response.setDefaultedFields(summary.defaultedFields);
            log.info("AI prediction completed for student {}: score={}, risk={}/{}",
                    studentId, response.getPredictedScore(), response.getRiskLevel(), response.getRiskProbability());
            return response;
        } catch (RuntimeException ex) {
            log.warn("AI prediction failed for student {}: {}", studentId, ex.getMessage());
            throw ex;
        }
    }

    /** Real-data prediction: what-if overrides are NOT applied. */
    public PredictionResponse predictForStudent(Long studentId, PredictionRequest request) {
        return predictForStudent(studentId, request, false);
    }

    /** What-if simulation: overrides applied on top of the student's real data. */
    public PredictionResponse simulateForStudent(Long studentId, PredictionRequest request) {
        return predictForStudent(studentId, request, true);
    }

    /**
     * Builds the effective feature request.
     *
     * Priority order:
     *  1. real academic data from the database (chronological G1/G2, absences),
     *  2. explicit what-if overrides from the caller (skipped when forceRealData),
     *  3. dataset-mean defaults ONLY for features the database does not track
     *     (studytime, failures) — these are always reported in defaultedFields.
     */
    private PredictionRequest buildEffectiveRequest(StudentAiSummary summary, PredictionRequest request, boolean applyWhatIf) {
        PredictionRequest effective = new PredictionRequest();
        effective.setStudentId(summary.studentId);
        effective.setG1(summary.g1);
        effective.setG2(summary.g2);
        effective.setAbsences(summary.absences);

        boolean whatIf = request != null && applyWhatIf;
        if (whatIf) {
            if (request.getG1() != null) effective.setG1(request.getG1());
            if (request.getG2() != null) effective.setG2(request.getG2());
            if (request.getAbsences() != null) effective.setAbsences(request.getAbsences());
            if (request.getStudyTime() != null) effective.setStudyTime(request.getStudyTime());
            if (request.getPastFailures() != null) effective.setPastFailures(request.getPastFailures());
        }

        // Neutral dataset-average defaults for features not tracked in the DB.
        // Always surfaced to the user via defaultedFields (see ML_FEATURE_MAPPING.md).
        if (effective.getStudyTime() == null) {
            effective.setStudyTime(2); // dataset mean studytime
            summary.defaultedFields.add("studyTime");
        }
        if (effective.getPastFailures() == null) {
            effective.setPastFailures(0); // dataset mean failures rounded down
            summary.defaultedFields.add("pastFailures");
        }

        // Aggregated basis overrides (what-if sets exact values instead)
        if (!whatIf || request.getAbsences() == null) {
            summary.dataBasis.add(summary.totalClasses + " attendance record(s)");
        }
        if (!whatIf) {
            summary.dataBasis.add(marksRowsNote(summary));
        }
        return effective;
    }

    private String marksRowsNote(StudentAiSummary summary) {
        return switch (summary.assessmentCount) {
            case 0 -> "no recorded assessments";
            case 1 -> "1 recorded assessment";
            default -> summary.assessmentCount + " recorded assessments";
        };
    }

    /**
     * Chronological feature extraction from real academic data.
     * Chooses G1/G2 by exam DATE (then marks id), never by numeric score.
     */
    private StudentAiSummary buildSummary(Long studentId, List<Marks> marksRows, long totalClasses, long presentClasses) {
        StudentAiSummary s = new StudentAiSummary();
        s.studentId = studentId;
        s.totalClasses = totalClasses;
        s.absences = (int) Math.min(Math.max(0, totalClasses - presentClasses), Integer.MAX_VALUE);

        // chronologically ordered: exam date asc, then marks id asc
        List<Marks> ordered = marksRows.stream()
                .filter(m -> m.getMarksObtained() != null)
                .sorted(Comparator
                        .comparing((Marks m) -> m.getExam() != null && m.getExam().getExamDate() != null
                                ? m.getExam().getExamDate()
                                : java.time.LocalDate.MIN)
                        .thenComparing(m -> m.getMarksId() != null ? m.getMarksId() : Long.MAX_VALUE))
                .toList();

        s.assessmentCount = ordered.size();
        if (ordered.size() >= 2) {
            Marks g1Row = ordered.get(ordered.size() - 2);
            Marks g2Row = ordered.get(ordered.size() - 1);
            s.g1 = g1Row.getMarksObtained();
            s.g2 = g2Row.getMarksObtained();
            s.g1Source = describeAssessment(g1Row);
            s.g2Source = describeAssessment(g2Row);
            s.dataSufficiency = "SUFFICIENT_DATA";
            s.dataQuality = "Good";
            s.dataBasis.add("Two most recent assessments: " + s.g1Source + " -> " + s.g2Source);
        } else if (ordered.size() == 1) {
            Marks only = ordered.get(0);
            s.g1 = only.getMarksObtained();
            s.g2 = only.getMarksObtained();
            s.g1Source = s.g2Source = describeAssessment(only);
            s.dataSufficiency = "LIMITED_DATA";
            s.dataQuality = "Fair";
            s.dataBasis.add("Only one recorded assessment: " + s.g2Source);
        } else {
            // No assessments at all: do NOT invent a plausible score.
            s.dataSufficiency = "INSUFFICIENT_DATA";
            s.dataQuality = "Poor";
            s.dataBasis.add("No recorded assessments");
        }

        if (totalClasses == 0) {
            s.dataBasis.add("No attendance records");
        }

        // Widen quality when the attendance record is thin as well
        if (totalClasses > 0 && totalClasses < 10 && "Good".equals(s.dataQuality)) {
            s.dataQuality = "Fair";
        }
        return s;
    }

    private String describeAssessment(Marks m) {
        String examName = m.getExam() != null ? m.getExam().getExamName() : "assessment";
        String date = m.getExam() != null && m.getExam().getExamDate() != null
                ? m.getExam().getExamDate().toString()
                : "no date";
        String subject = m.getSubject() != null ? m.getSubject().getSubjectCode() : "";
        return examName + (subject.isEmpty() ? "" : " (" + subject + ")") + " on " + date;
    }

    /**
     * College percent scale -> UCI 0-20 scale, plus raw studytime/failures.
     * The returned map is also surfaced to the UI in PredictionResponse
     * .featuresUsed so users can see exactly what the model received.
     */
    private Map<String, Object> toFeatureMap(PredictionRequest request) {
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("G1", round2(request.getG1() / 100.0 * 20.0));
        features.put("G2", round2(request.getG2() / 100.0 * 20.0));
        features.put("studytime", request.getStudyTime());
        features.put("failures", request.getPastFailures());
        features.put("absences", request.getAbsences());
        return features;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private Double asDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.doubleValue();
        return Double.parseDouble(value.toString());
    }

    @SuppressWarnings("unchecked")
    private List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o != null) out.add(o.toString());
            }
            return out;
        }
        return List.of();
    }

    private List<String> firstNonEmpty(List<String> a, List<String> b) {
        return (a != null && !a.isEmpty()) ? a : (b != null ? b : List.of());
    }

    /** Mutable accumulator for the real-data feature extraction result. */
    private static class StudentAiSummary {
        Long studentId;
        Double g1;
        Double g2;
        Integer absences;
        long totalClasses;
        int assessmentCount;
        String dataSufficiency = "INSUFFICIENT_DATA";
        String dataQuality = "Poor";
        String g1Source;
        String g2Source;
        List<String> dataBasis = new ArrayList<>();
        List<String> defaultedFields = new ArrayList<>();
    }
}
