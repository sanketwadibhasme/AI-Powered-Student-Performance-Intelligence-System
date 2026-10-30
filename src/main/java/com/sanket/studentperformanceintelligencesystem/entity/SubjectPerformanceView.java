package com.sanket.studentperformanceintelligencesystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "subject_performance_view")
@Immutable
public class SubjectPerformanceView {

    @Id
    private Long subjectId;

    private String subjectName;

    private String subjectCode;

    private Integer credits;

    private String departmentName;

    private String facultyName;

    private Double averageMarks;

    private Double averageAttendance;

    private Long totalEnrollments;

    public SubjectPerformanceView() {
    }

    // Getters & Setters

    public Long getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getTotalEnrollments() {
        return totalEnrollments;
    }
    public void setTotalEnrollments(Long totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }

    public Double getAverageAttendance() {
        return averageAttendance;
    }
    public void setAverageAttendance(Double averageAttendance) {
        this.averageAttendance = averageAttendance;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }
    public void setAverageMarks(Double averageMarks) {
        this.averageMarks = averageMarks;
    }

    public String getFacultyName() {
        return facultyName;
    }
    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getDepartmentName() {
        return departmentName;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getCredits() {
        return credits;
    }
    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getSubjectCode() {
        return subjectCode;
    }
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}