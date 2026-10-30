package com.sanket.studentperformanceintelligencesystem.entity;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the examination",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long examId;

    @NotBlank(message = "Exam name is required")
    @Size(min = 2, max = 50, message = "Exam name must be between 2 and 50 characters")
    @Schema(
            description = "Name of the examination",
            example = "End Semester Examination"
    )
    @Column(nullable = false)
    private String examName;

    @Column(nullable = false)
    @NotNull(message = "Exam date is required")
    @Schema(
            description = "Date on which the examination is conducted",
            example = "2026-05-15"
    )
    private LocalDate examDate;

    @NotNull(message = "Total marks are required")
    @Min(value = 0, message = "Total marks must be greater than 0")
    @Max(value = 200, message = "Total marks cannot exceed 200")
    @Schema(
            description = "Maximum marks for the examination",
            example = "100",
            minimum = "1",
            maximum = "200"
    )
    @Column(nullable = false)
    private Integer totalMarks;

    public Exam() {
    }
    public Exam(String examName, LocalDate examDate,Integer totalMarks){
        this.examName = examName;
        this.examDate = examDate;
        this.totalMarks = totalMarks;
    }

    public Long getExamId() {
        return examId;
    }
    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }
    public void setExamName(String examName) {
        this.examName = examName;
    }

    public LocalDate getExamDate() {
        return examDate;
    }
    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }
    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "examId=" + examId +
                ", examName='" + examName + '\'' +
                ", examDate=" + examDate +
                ", totalMarks=" + totalMarks +
                '}';
    }
}