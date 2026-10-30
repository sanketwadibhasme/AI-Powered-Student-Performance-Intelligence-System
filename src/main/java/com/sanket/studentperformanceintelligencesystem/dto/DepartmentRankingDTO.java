package com.sanket.studentperformanceintelligencesystem.dto;

public class DepartmentRankingDTO {

    private int rank;
    private Long departmentId;
    private String departmentName;
    private double averageMarks;

    public DepartmentRankingDTO() {
    }

    public DepartmentRankingDTO(int rank, Long departmentId, String departmentName, double averageMarks) {
        this.rank = rank;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.averageMarks = averageMarks;
    }

    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank;
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

    public double getAverageMarks() {
        return averageMarks;
    }
    public void setAverageMarks(double averageMarks) {
        this.averageMarks = averageMarks;
    }
}