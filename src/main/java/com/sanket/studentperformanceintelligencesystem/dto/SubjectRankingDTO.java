package com.sanket.studentperformanceintelligencesystem.dto;

public class SubjectRankingDTO {

    private int rank;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String department;
    private double averageMarks;

    public SubjectRankingDTO() {
    }

    public SubjectRankingDTO(int rank, Long subjectId, String subjectName,
                             String subjectCode, String department,
                             double averageMarks) {
        this.rank = rank;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.department = department;
        this.averageMarks = averageMarks;
    }

    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank;
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

    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public double getAverageMarks() {
        return averageMarks;
    }
    public void setAverageMarks(double averageMarks) {
        this.averageMarks = averageMarks;
    }
}
