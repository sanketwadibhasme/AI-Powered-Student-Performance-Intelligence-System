package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.dto.DashboardSummaryDTO;
import com.sanket.studentperformanceintelligencesystem.dto.DepartmentPerformanceDTO;
import com.sanket.studentperformanceintelligencesystem.entity.Department;
import com.sanket.studentperformanceintelligencesystem.repository.DepartmentRepository;
import com.sanket.studentperformanceintelligencesystem.repository.FacultyRepository;
import com.sanket.studentperformanceintelligencesystem.repository.StudentRepository;
import com.sanket.studentperformanceintelligencesystem.repository.SubjectRepository;
import com.sanket.studentperformanceintelligencesystem.service.AnalyticsService;
import com.sanket.studentperformanceintelligencesystem.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final AnalyticsService analyticsService;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;

    public DashboardServiceImpl(
            AnalyticsService analyticsService,
            StudentRepository studentRepository,
            DepartmentRepository departmentRepository,
            FacultyRepository facultyRepository,
            SubjectRepository subjectRepository) {

        this.analyticsService = analyticsService;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public DashboardSummaryDTO getDashboardSummary(){
        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        dto.setOverAllAverageMarks(analyticsService.getOverallAverageMarks());
        dto.setOverAllAttendancePercentage(analyticsService.getOverallAttendancePercentage());

        dto.setTotalStudents(studentRepository.count());
        dto.setTotalDepartments(departmentRepository.count());
        dto.setTotalFaculty(facultyRepository.count());
        dto.setTotalSubjects(subjectRepository.count());

        dto.setStudentsBelowAttendanceThreshold((long) analyticsService.getStudentsBelowAttendanceThreshold(75.0).size());
        dto.setStudentsBelowMarksThreshold((long) analyticsService.getStudentsBelowMarksThreshold(35.0).size());

        return dto;
    }

    @Override
    public List<DepartmentPerformanceDTO> getDepartmentPerformanceReport() {

        List<Department> departments = departmentRepository.findAll();
        List<DepartmentPerformanceDTO> report = new ArrayList<>();

        for (Department department : departments) {
            DepartmentPerformanceDTO dto = new DepartmentPerformanceDTO();

            dto.setDepartmentId(department.getDepartmentId());
            dto.setDepartmentName(department.getDepartmentName());

            dto.setAverageMarks(analyticsService.getAverageMarksByDepartment(department.getDepartmentId()));

            dto.setAverageAttendance(analyticsService.getAttendancePercentageByDepartment(department.getDepartmentId()));

            dto.setTotalStudents(studentRepository.countByDepartmentDepartmentId(department.getDepartmentId()));

            dto.setTotalFaculty(facultyRepository.countByDepartmentDepartmentId(department.getDepartmentId()));

            dto.setTotalSubjects(subjectRepository.countByDepartmentDepartmentId(department.getDepartmentId()));

            report.add(dto);
        }

        return report;
    }
}
