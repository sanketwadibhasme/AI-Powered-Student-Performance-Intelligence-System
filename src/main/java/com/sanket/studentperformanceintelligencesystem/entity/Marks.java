package com.sanket.studentperformanceintelligencesystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "marks")
public class Marks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the marks record",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long marksId;

    @NotNull(message = "Marks obtained are required")
    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    //@Digits(integer = 3, fraction = 2, message = "Marks can have up to 3 integer digits and 2 decimal places")
    @Schema(
            description = "Marks obtained by the student in the examination",
            example = "87.50",
            minimum = "0.0"
    )
    @Column(nullable = false)
    private Double marksObtained;

    @NotNull(message = "Student is required")
    @Schema(
            description = "Student to whom the marks belong"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @NotNull(message = "Subject is needed")
    @Schema(
            description = "Subject for which the marks were awarded"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Subject subject;

    @NotNull(message = "exam is required")
    @Schema(
            description = "Examination in which the student obtained the marks"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exam_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exam exam;

    public Marks() {
    }
    public Marks(Double marksObtained, Student student, Subject subject, Exam exam) {
        this.marksObtained = marksObtained;
        this.student = student;
        this.subject = subject;
        this.exam = exam;
    }

    public Long getMarksId() {
        return marksId;
    }
    public void setMarksId(Long marksId) {
        this.marksId = marksId;
    }

    public Double getMarksObtained() {
        return marksObtained;
    }
    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
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

    public Exam getExam() {
        return exam;
    }
    public void setExam(Exam exam) {
        this.exam = exam;
    }

    @Override
    public String toString() {
        return "Marks{" +
                "marksId=" + marksId +
                ", marksObtained=" + marksObtained +
                ", student=" + student +
                ", subject=" + subject +
                ", exam=" + exam +
                '}';
    }
}
