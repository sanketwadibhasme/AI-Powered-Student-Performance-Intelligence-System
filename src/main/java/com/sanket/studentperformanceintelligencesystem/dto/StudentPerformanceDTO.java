package com.sanket.studentperformanceintelligencesystem.dto;

public class StudentPerformanceDTO {

    private Long studentId;

    private String rollNumber;

    private String studentName;

    private String departmentName;

    private Double averageMarks;

    private Double attendancePercentage;

    public StudentPerformanceDTO() {
    }

    public StudentPerformanceDTO(Long studentId,
                                 String rollNumber,
                                 String studentName,
                                 String departmentName,
                                 Double averageMarks,
                                 Double attendancePercentage) {

        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        this.departmentName = departmentName;
        this.averageMarks = averageMarks;
        this.attendancePercentage = attendancePercentage;
    }

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
