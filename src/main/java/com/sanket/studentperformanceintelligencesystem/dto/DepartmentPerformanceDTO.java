package com.sanket.studentperformanceintelligencesystem.dto;

public class DepartmentPerformanceDTO {
    private Long departmentId;
    private String departmentName;

    private Double averageMarks;
    private Double averageAttendance;

    private Long totalStudents;
    private Long totalFaculty;
    private Long totalSubjects;

    public DepartmentPerformanceDTO() {
    }

    public DepartmentPerformanceDTO(Long departmentId,
                                    String departmentName,
                                    Double averageMarks,
                                    Double averageAttendance,
                                    Long totalStudents,
                                    Long totalFaculty,
                                    Long totalSubjects) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.averageMarks = averageMarks;
        this.averageAttendance = averageAttendance;
        this.totalStudents = totalStudents;
        this.totalFaculty = totalFaculty;
        this.totalSubjects = totalSubjects;
    }

    public Long getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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

    public Double getAverageAttendance() {
        return averageAttendance;
    }
    public void setAverageAttendance(Double averageAttendance) {
        this.averageAttendance = averageAttendance;
    }

    public Long getTotalStudents() {
        return totalStudents;
    }
    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
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
}
