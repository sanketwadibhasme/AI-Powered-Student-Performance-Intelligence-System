package com.sanket.studentperformanceintelligencesystem.service;
import com.sanket.studentperformanceintelligencesystem.dto.*;

import java.util.List;

public interface ReportService {
    List<DepartmentPerformanceDTO> getDepartmentPerformanceReport();

    List<StudentPerformanceDTO> getStudentPerformanceReport();

    List<SubjectPerformanceDTO> getSubjectPerformanceReport();

    List<DepartmentRankingDTO> getDepartmentRanking();

    List<StudentRankingDTO> getStudentRanking();

    List<SubjectRankingDTO> getSubjectRanking();

    List<StudentAttendanceRankingDTO> getHighestAttendanceStudents();
    List<StudentAttendanceRankingDTO> getLowestAttendanceStudents();
}
