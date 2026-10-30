package com.sanket.studentperformanceintelligencesystem.dto;

public class StudentAttendanceRankingDTO {

    private int rank;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String department;
    private double attendancePercentage;

    public StudentAttendanceRankingDTO() {
    }

    public StudentAttendanceRankingDTO(int rank,
                                       Long studentId,
                                       String studentName,
                                       String rollNumber,
                                       String department,
                                       double attendancePercentage) {
        this.rank = rank;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.department = department;
        this.attendancePercentage = attendancePercentage;
    }

    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }

    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}