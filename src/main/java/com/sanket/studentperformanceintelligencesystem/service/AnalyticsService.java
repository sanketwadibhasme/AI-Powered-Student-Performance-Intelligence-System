package com.sanket.studentperformanceintelligencesystem.service;

import com.sanket.studentperformanceintelligencesystem.entity.Department;
import com.sanket.studentperformanceintelligencesystem.entity.Student;

import java.util.List;

public interface AnalyticsService {
    //1. calc avg marks of a student
    Double calculateAverageMarksByStudentId(Long studentId);

    //2.calc attendance% of a student
    Double calculateAttendancePercentageByStudentId(Long studentId);

    //3.calc avg marks in a subject
    Double getAverageMarksBySubject(Long subjectId);

    //4.Avg marks of a department
    Double getAverageMarksByDepartment(Long departmentId);

    //5.Avg attendance% of subject
    Double getAttendancePercentageBySubject(Long subjectId);

    //6.Avg attendance% of department
    Double getAttendancePercentageByDepartment(Long departmentId);

    //7.list of student below attendance threshold
    List<Student> getStudentsBelowAttendanceThreshold(Double threshold);

    //8.list of students below mark threshhold
    List<Student> getStudentsBelowMarksThreshold(Double threshold);

    //9.overall college avg marks
    Double getOverallAverageMarks();

    //10. overall college avg attendance %
    Double getOverallAttendancePercentage();

    //11. Best performing department
    Department getTopPerformingDepartment();
}
