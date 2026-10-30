package com.sanket.studentperformanceintelligencesystem.dto;

public interface StudentAttendanceRankingProjection {

    Long getStudentId();

    String getFirstName();

    String getLastName();

    String getRollNumber();

    String getDepartmentName();

    Long getPresentClasses();

    Long getTotalClasses();

}
