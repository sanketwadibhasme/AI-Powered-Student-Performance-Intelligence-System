package com.sanket.studentperformanceintelligencesystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "student_performance_view")
@Immutable
public class StudentPerformanceView {

    @Id
    private Long studentId;

    private String rollNumber;

    private String studentName;

    private String departmentName;

    private Double averageMarks;

    private Double attendancePercentage;

    public StudentPerformanceView() {
    }

    // Getters & Setters

    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartmentName() {
        return departmentName;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }
    public void setAverageMarks(Double averageMarks) {
        this.averageMarks = averageMarks;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }
    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}