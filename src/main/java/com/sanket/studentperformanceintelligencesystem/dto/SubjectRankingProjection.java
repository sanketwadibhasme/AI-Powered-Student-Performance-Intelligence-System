package com.sanket.studentperformanceintelligencesystem.dto;

public interface SubjectRankingProjection {

    Long getSubjectId();

    String getSubjectName();

    String getSubjectCode();

    String getDepartmentName();

    Double getAverageMarks();

}