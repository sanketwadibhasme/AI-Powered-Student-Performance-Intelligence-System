package com.sanket.studentperformanceintelligencesystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the enrollment record",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long enrollmentId;

    @Column(nullable = false)
    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year must be in the format YYYY-YYYY (e.g., 2025-2026)")
    @Schema(
            description = "Academic year for the enrollment",
            example = "2025-2026",
            pattern = "^\\d{4}-\\d{4}$"
    )
    private String academicYear;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot exceed 8")
    @Schema(
            description = "Semester in which the student is enrolled",
            example = "5",
            minimum = "1",
            maximum = "8"
    )
    @Column(nullable = false)
    private Integer semester;

    @NotNull(message = "Student is required")
    @Schema(
            description = "Student enrolled in the subject"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"department"})
    private Student student;

    @NotNull(message = "Subject is required")
    @Schema(
            description = "Subject in which the student is enrolled"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({"department", "faculty"})
    private Subject subject;

    // Default Constructor
    public Enrollment() {
    }

    // Parameterized Constructor
    public Enrollment(Long enrollmentId, String academicYear, Integer semester, Student student, Subject subject) {
        this.enrollmentId = enrollmentId;
        this.academicYear = academicYear;
        this.semester = semester;
        this.student = student;
        this.subject = subject;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }
    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getAcademicYear() {
        return academicYear;
    }
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getSemester() {
        return semester;
    }
    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", academicYear='" + academicYear + '\'' +
                ", semester=" + semester +
                ", student=" + student +
                ", subject=" + subject +
                '}';
    }
}