package com.sanket.studentperformanceintelligencesystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the subject",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "1"
    )
    private Long subjectId;

    @NotBlank(message = "Subject name is required")
    @Size(min = 2, max = 50, message = "Subject name must be between 2 and 50 characters")
    @Schema(
            description = "Name of the subject",
            example = "Database Management Systems"
    )
    @Column(nullable = false)
    private String subjectName;

    @NotBlank(message = "Subject code is required")
    @Size(min = 2, max = 20, message = "Subject code must be between 2 and 20 characters")
    @Schema(
            description = "Unique subject code",
            example = "DBMS301"
    )
    @Column(nullable = false, unique = true)
    private String subjectCode;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    @Schema(
            description = "Number of academic credits assigned to the subject",
            example = "4",
            minimum = "1",
            maximum = "10"
    )
    @Column(nullable = false)
    private Integer credits;

    @NotNull(message = "Department is required")
    @Schema(
            description = "Department offering the subject"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    @NotNull(message = "Faculty is required")
    @Schema(
            description = "Faculty assigned to teach the subject"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Faculty faculty;

    // No-Argument Constructor
    public Subject() {
    }

    // Parameterized Constructor
    public Subject(Long subjectId, String subjectName, String subjectCode, Integer credits,
                   Department department, Faculty faculty) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.credits = credits;
        this.department = department;
        this.faculty = faculty;
    }

    public Long getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public Integer getCredits() {
        return credits;
    }
    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }

    public Faculty getFaculty() {
        return faculty;
    }
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "subjectId=" + subjectId +
                ", subjectName='" + subjectName + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", credits=" + credits +
                ", department=" + department +
                ", faculty=" + faculty +
                '}';
    }
}