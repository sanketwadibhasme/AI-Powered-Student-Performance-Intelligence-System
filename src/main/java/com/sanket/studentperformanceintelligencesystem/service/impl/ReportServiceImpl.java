package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.dto.*;
import com.sanket.studentperformanceintelligencesystem.entity.*;
import com.sanket.studentperformanceintelligencesystem.repository.*;
import com.sanket.studentperformanceintelligencesystem.service.AnalyticsService;
import com.sanket.studentperformanceintelligencesystem.service.ReportService;

import com.sanket.studentperformanceintelligencesystem.repository.DepartmentPerformanceViewRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final AnalyticsService analyticsService;

    private final DepartmentPerformanceViewRepository departmentPerformanceViewRepository;
    private final StudentPerformanceViewRepository studentPerformanceViewRepository;
    private final SubjectPerformanceViewRepository subjectPerformanceViewRepository;

    public ReportServiceImpl(DepartmentRepository departmentRepository,
                             StudentRepository studentRepository,
                             AttendanceRepository attendanceRepository,
                             SubjectRepository subjectRepository,
                             AnalyticsService analyticsService,
                             DepartmentPerformanceViewRepository departmentPerformanceViewRepository,
                             StudentPerformanceViewRepository studentPerformanceViewRepository,
                             SubjectPerformanceViewRepository subjectPerformanceViewRepository) {

        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.subjectRepository = subjectRepository;
        this.analyticsService = analyticsService;

        this.departmentPerformanceViewRepository = departmentPerformanceViewRepository;
        this.studentPerformanceViewRepository = studentPerformanceViewRepository;
        this.subjectPerformanceViewRepository = subjectPerformanceViewRepository;
    }

    @Override
    public List<DepartmentPerformanceDTO> getDepartmentPerformanceReport() {

        List<DepartmentPerformanceView> views = departmentPerformanceViewRepository.findAll();
        List<DepartmentPerformanceDTO> report = new ArrayList<>();

        for (DepartmentPerformanceView view : views) {
            DepartmentPerformanceDTO dto = new DepartmentPerformanceDTO();

            dto.setDepartmentId(view.getDepartmentId());
            dto.setDepartmentName(view.getDepartmentName());
            dto.setAverageMarks(view.getAverageMarks());
            dto.setAverageAttendance(view.getAverageAttendance());
            dto.setTotalStudents(view.getTotalStudents());
            dto.setTotalFaculty(view.getTotalFaculty());
            dto.setTotalSubjects(view.getTotalSubjects());

//            dto.setDepartmentId(department.getDepartmentId());
//            dto.setDepartmentName(department.getDepartmentName());
//
//            dto.setAverageMarks(
//                    analyticsService.getAverageMarksByDepartment(department.getDepartmentId()));
//
//            dto.setAverageAttendance(
//                    analyticsService.getAttendancePercentageByDepartment(department.getDepartmentId()));
//
//            dto.setTotalStudents(
//                    studentRepository.countByDepartmentDepartmentId(department.getDepartmentId()));
//
//            dto.setTotalFaculty(
//                    facultyRepository.countByDepartmentDepartmentId(department.getDepartmentId()));
//
//            dto.setTotalSubjects(
//                    subjectRepository.countByDepartmentDepartmentId(department.getDepartmentId()));

            report.add(dto);
        }
        return report;
    }

    @Override
    public List<StudentPerformanceDTO> getStudentPerformanceReport() {
        List<StudentPerformanceView> views = studentPerformanceViewRepository.findAll();
        List<StudentPerformanceDTO> report = new ArrayList<>();

        for (StudentPerformanceView view : views) {
            StudentPerformanceDTO dto = new StudentPerformanceDTO();
            dto.setStudentId(view.getStudentId());
            dto.setRollNumber(view.getRollNumber());
            dto.setStudentName(view.getStudentName());
            dto.setDepartmentName(view.getDepartmentName());
            dto.setAverageMarks(view.getAverageMarks());
            dto.setAttendancePercentage(view.getAttendancePercentage());

//            dto.setStudentId(student.getStudentId());
//            dto.setRollNumber(student.getRollNumber());
//
//            dto.setStudentName(
//                    student.getFirstName() + " " + student.getLastName());
//
//            dto.setDepartmentName(
//                    student.getDepartment().getDepartmentName());
//
//            dto.setAverageMarks(
//                    analyticsService.calculateAverageMarksByStudentId(student.getStudentId()));
//
//            dto.setAttendancePercentage(
//                    analyticsService.calculateAttendancePercentageByStudentId(student.getStudentId()));

            report.add(dto);
        }
        return report;
    }

    @Override
    public List<SubjectPerformanceDTO> getSubjectPerformanceReport() {

        List<SubjectPerformanceView> views = subjectPerformanceViewRepository.findAll();
        List<SubjectPerformanceDTO> report = new ArrayList<>();

        for (SubjectPerformanceView view : views) {
            SubjectPerformanceDTO dto = new SubjectPerformanceDTO();

            dto.setSubjectId(view.getSubjectId());
            dto.setSubjectName(view.getSubjectName());
            dto.setSubjectCode(view.getSubjectCode());
            dto.setCredits(view.getCredits());

            dto.setDepartmentName(view.getDepartmentName());
            dto.setFacultyName(view.getFacultyName());

            dto.setAverageMarks(view.getAverageMarks());
            dto.setAverageAttendance(view.getAverageAttendance());

            dto.setTotalEnrollments(view.getTotalEnrollments());

//            dto.setSubjectId(subject.getSubjectId());
//            dto.setSubjectName(subject.getSubjectName());
//            dto.setSubjectCode(subject.getSubjectCode());
//            dto.setCredits(subject.getCredits());
//
//            dto.setDepartmentName(
//                    subject.getDepartment().getDepartmentName());
//
//            dto.setFacultyName(
//                    subject.getFaculty().getFacultyName());
//
//            dto.setAverageMarks(
//                    analyticsService.getAverageMarksBySubject(subject.getSubjectId()));
//
//            dto.setAverageAttendance(
//                    analyticsService.getAttendancePercentageBySubject(subject.getSubjectId()));
//
//            dto.setTotalEnrollments(
//                    enrollmentRepository.countBySubjectSubjectId(subject.getSubjectId()));

            report.add(dto);
        }

        return report;
    }

    @Override
    public List<DepartmentRankingDTO> getDepartmentRanking() {

        List<DepartmentRankingDTO> rankings = new ArrayList<>();
        List<DepartmentRankingProjection> departments = departmentRepository.getDepartmentRankingData();

        for (DepartmentRankingProjection department : departments) {
            DepartmentRankingDTO dto = new DepartmentRankingDTO();

            dto.setDepartmentId(department.getDepartmentId());
            dto.setDepartmentName(department.getDepartmentName());
            dto.setAverageMarks(department.getAverageMarks());

            rankings.add(dto);
        }

        rankings.sort((d1, d2) ->
                Double.compare(d2.getAverageMarks(), d1.getAverageMarks()));

        int rank = 1;
        for (DepartmentRankingDTO dto : rankings) {
            dto.setRank(rank++);
        }

        return rankings;
    }

    @Override
    public List<StudentRankingDTO> getStudentRanking() {

        List<StudentRankingDTO> rankings = new ArrayList<>();
        List<StudentRankingProjection> students = studentRepository.getStudentRankingData();

        for (StudentRankingProjection student : students) {
//            double averageMarks =
//                    analyticsService.calculateAverageMarksByStudentId(student.getStudentId());

            StudentRankingDTO dto = new StudentRankingDTO();

            dto.setStudentId(student.getStudentId());
            dto.setStudentName(student.getFirstName() + " " + student.getLastName());
            dto.setRollNumber(student.getRollNumber());
            dto.setDepartment(student.getDepartmentName());
            dto.setAverageMarks(student.getAverageMarks());

            rankings.add(dto);
        }

        rankings.sort((s1, s2) ->
                Double.compare(s2.getAverageMarks(), s1.getAverageMarks()));

        int rank = 1;
        for (StudentRankingDTO dto : rankings) {
            dto.setRank(rank++);
        }

        return rankings;
    }

    @Override
    public List<SubjectRankingDTO> getSubjectRanking() {

        List<SubjectRankingDTO> rankings = new ArrayList<>();
        List<SubjectRankingProjection> subjects = subjectRepository.getSubjectRankingData();

        for (SubjectRankingProjection subject : subjects) {
            SubjectRankingDTO dto = new SubjectRankingDTO();

            dto.setSubjectId(subject.getSubjectId());
            dto.setSubjectName(subject.getSubjectName());
            dto.setSubjectCode(subject.getSubjectCode());
            dto.setDepartment(subject.getDepartmentName());
            dto.setAverageMarks(subject.getAverageMarks());

            rankings.add(dto);
        }

        rankings.sort((s1, s2) ->
                Double.compare(s2.getAverageMarks(), s1.getAverageMarks()));

        int rank = 1;
        for (SubjectRankingDTO dto : rankings) {
            dto.setRank(rank++);
        }

        return rankings;
    }

    @Override
    public List<StudentAttendanceRankingDTO> getHighestAttendanceStudents() {

        List<StudentAttendanceRankingDTO> rankings = new ArrayList<>();
        List<StudentAttendanceRankingProjection> students = attendanceRepository.getStudentAttendanceRanking();

        for (StudentAttendanceRankingProjection student : students) {
//            double attendance =
//                    analyticsService.calculateAttendancePercentageByStudentId(student.getStudentId());

            StudentAttendanceRankingDTO dto = new StudentAttendanceRankingDTO();
            dto.setStudentId(student.getStudentId());
            dto.setStudentName(student.getFirstName() + " " + student.getLastName());
            dto.setRollNumber(student.getRollNumber());
            dto.setDepartment(student.getDepartmentName());

            double attendancePercentage = 0.0;
            if (student.getTotalClasses() != 0) {
                attendancePercentage =
                        (student.getPresentClasses() * 100.0)
                                / student.getTotalClasses();
            }
            dto.setAttendancePercentage(attendancePercentage);

            rankings.add(dto);
        }

        rankings.sort((s1, s2) ->
                Double.compare(s2.getAttendancePercentage(),
                        s1.getAttendancePercentage()));

        int rank = 1;
        for (StudentAttendanceRankingDTO dto : rankings) {
            dto.setRank(rank++);
        }

        return rankings;
    }

    @Override
    public List<StudentAttendanceRankingDTO> getLowestAttendanceStudents() {

        List<StudentAttendanceRankingDTO> rankings = new ArrayList<>();
        List<StudentAttendanceRankingProjection> students =
                attendanceRepository.getStudentAttendanceRanking();

        for (StudentAttendanceRankingProjection student : students) {
//            double attendance =
//                    analyticsService.calculateAttendancePercentageByStudentId(student.getStudentId());

            StudentAttendanceRankingDTO dto = new StudentAttendanceRankingDTO();
            dto.setStudentId(student.getStudentId());
            dto.setStudentName(student.getFirstName() + " " + student.getLastName());
            dto.setRollNumber(student.getRollNumber());
            dto.setDepartment(student.getDepartmentName());

            double attendancePercentage = 0.0;
            if (student.getTotalClasses() != 0) {
                attendancePercentage =
                        (student.getPresentClasses() * 100.0)
                                / student.getTotalClasses();
            }
            dto.setAttendancePercentage(attendancePercentage);

            rankings.add(dto);
        }

        rankings.sort((s1, s2) ->
                Double.compare(s1.getAttendancePercentage(),
                        s2.getAttendancePercentage()));

        int rank = 1;
        for (StudentAttendanceRankingDTO dto : rankings) {
            dto.setRank(rank++);
        }

        return rankings;
    }
}
