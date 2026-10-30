package com.sanket.studentperformanceintelligencesystem.dto;

public class DashboardSummaryDTO {
    private Double overAllAverageMarks;
    private Double overAllAttendancePercentage;

    private Long totalStudents;
    private Long totalDepartments;
    private Long totalFaculty;
    private Long totalSubjects;

    private Long studentsBelowAttendanceThreshold;
    private Long studentsBelowMarksThreshold;

    public DashboardSummaryDTO() {
    }
    public DashboardSummaryDTO(Double overAllAverageMarks,
                               Double overAllAttendancePercentage,
                               Long totalStudents,
                               Long totalDepartments,
                               Long totalFaculty,
                               Long totalSubjects,
                               Long studentsBelowAttendanceThreshold,
                               Long studentsBelowMarksThreshold) {

        this.overAllAverageMarks = overAllAverageMarks;
        this.overAllAttendancePercentage = overAllAttendancePercentage;
        this.totalStudents = totalStudents;
        this.totalDepartments = totalDepartments;
        this.totalFaculty = totalFaculty;
        this.totalSubjects = totalSubjects;
        this.studentsBelowAttendanceThreshold = studentsBelowAttendanceThreshold;
        this.studentsBelowMarksThreshold = studentsBelowMarksThreshold;
    }

    public Double getOverAllAverageMarks() {
        return overAllAverageMarks;
    }
    public void setOverAllAverageMarks(Double overAllAverageMarks) {
        this.overAllAverageMarks = overAllAverageMarks;
    }

    public Double getOverAllAttendancePercentage() {
        return overAllAttendancePercentage;
    }
    public void setOverAllAttendancePercentage(Double overAllAttendancePercentage) {
        this.overAllAttendancePercentage = overAllAttendancePercentage;
    }

    public Long getTotalStudents() {
        return totalStudents;
    }
    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getTotalDepartments() {
        return totalDepartments;
    }
    public void setTotalDepartments(Long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public Long getTotalFaculty() {
        return totalFaculty;
    }
    public void setTotalFaculty(Long totalFaculty) {
        this.totalFaculty = totalFaculty;
    }

    public Long getTotalSubjects() {
        return totalSubjects;
    }
    public void setTotalSubjects(Long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public Long getStudentsBelowAttendanceThreshold() {
        return studentsBelowAttendanceThreshold;
    }
    public void setStudentsBelowAttendanceThreshold(Long studentsBelowAttendanceThreshold) {
        this.studentsBelowAttendanceThreshold = studentsBelowAttendanceThreshold;
    }

    public Long getStudentsBelowMarksThreshold() {
        return studentsBelowMarksThreshold;
    }
    public void setStudentsBelowMarksThreshold(Long studentsBelowMarksThreshold) {
        this.studentsBelowMarksThreshold = studentsBelowMarksThreshold;
    }
}
