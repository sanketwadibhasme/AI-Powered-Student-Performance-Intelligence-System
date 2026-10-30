package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.Department;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.repository.AttendanceRepository;
import com.sanket.studentperformanceintelligencesystem.repository.MarksRepository;
import com.sanket.studentperformanceintelligencesystem.service.AnalyticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private final MarksRepository marksRepository;
    private final AttendanceRepository attendanceRepository;

    public AnalyticsServiceImpl(MarksRepository marksRepository, AttendanceRepository attendanceRepository) {
        this.marksRepository = marksRepository;
        this.attendanceRepository = attendanceRepository;
    }

    //1.
    @Override
    public Double calculateAverageMarksByStudentId(Long studentId){
        return marksRepository.calculateAverageMarksByStudentId(studentId);
    }

    //2.
    @Override
    public Double calculateAttendancePercentageByStudentId(Long studentId){
        Long totalClasses = attendanceRepository.countTotalClassesByStudentId(studentId);
        Long presentClasses = attendanceRepository.countPresentClassesByStudentId(studentId);
        if(totalClasses == 0) return 0.0;

        return (presentClasses * 100.0)/totalClasses;
    }

    //3.
    @Override
    public Double getAverageMarksBySubject(Long subjectId) {
        Double average = marksRepository.findAverageMarksBySubjectId(subjectId);
        if (average == null) return 0.0;

        return average;
    }

    // 4.
    @Override
    public Double getAverageMarksByDepartment(Long departmentId) {
        Double average = marksRepository.findAverageMarksByDepartmentId(departmentId);
        if (average == null) return 0.0;

        return average;
    }

    //5.
    @Override
    public Double getAttendancePercentageBySubject(Long subjectId) {
        Double percentage = attendanceRepository.findAttendancePercentageBySubjectId(subjectId);
        if (percentage == null) return 0.0;

        return percentage;
    }

    //6.
    @Override
    public Double getAttendancePercentageByDepartment(Long departmentId) {
        Double percentage = attendanceRepository.findAttendancePercentageByDepartmentId(departmentId);
        if (percentage == null) return 0.0;

        return percentage;
    }

    //7.
    @Override
    public List<Student> getStudentsBelowAttendanceThreshold(Double threshold) {
        return attendanceRepository.findStudentsBelowAttendanceThreshold(threshold);
    }

    //8.
    @Override
    public List<Student> getStudentsBelowMarksThreshold(Double threshold) {
        return marksRepository.findStudentsBelowMarksThreshold(threshold);
    }

    // 9.
    @Override
    public Double getOverallAverageMarks() {
        Double average = marksRepository.findOverallAverageMarks();
        if (average == null) return 0.0;

        return average;
    }

    //10.
    @Override
    public Double getOverallAttendancePercentage() {
        Double percentage = attendanceRepository.findOverallAttendancePercentage();
        if (percentage == null) return 0.0;
        
        return percentage;
    }

    //11.
    @Override
    public Department getTopPerformingDepartment() {
        List<Department> departments = marksRepository.findDepartmentsByAverageMarksDesc(PageRequest.of(0, 1));
        if (departments.isEmpty()) return null;

        return departments.get(0);
    }
}
