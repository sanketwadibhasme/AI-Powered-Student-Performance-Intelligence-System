package com.sanket.studentperformanceintelligencesystem.dto;

public class SubjectPerformanceDTO {
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private Integer credits;
    private String departmentName;
    private String facultyName;
    private Double averageMarks;
    private Double averageAttendance;
    private Long totalEnrollments;

    public SubjectPerformanceDTO() {
    }

    public SubjectPerformanceDTO(Long subjectId,
                                 String subjectName,
                                 String subjectCode,
                                 Integer credits,
                                 String departmentName,
                                 String facultyName,
                                 Double averageMarks,
                                 Double averageAttendance,
                                 Long totalEnrollments) {

        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.credits = credits;
        this.departmentName = departmentName;
        this.facultyName = facultyName;
        this.averageMarks = averageMarks;
        this.averageAttendance = averageAttendance;
        this.totalEnrollments = totalEnrollments;
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

    public String getDepartmentName() {
        return departmentName;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getFacultyName() {
        return facultyName;
    }
    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public Double getAverageMarks() {
        return averageMarks;
    }
    public void setAverageMarks(Double averageMarks) {
        this.averageMarks = averageMarks;
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
}