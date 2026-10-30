package com.sanket.studentperformanceintelligencesystem.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sanket.studentperformanceintelligencesystem.enums.AttendanceStatus;
import com.sanket.studentperformanceintelligencesystem.exception.DuplicateResourceException;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.specification.AttendanceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.sanket.studentperformanceintelligencesystem.entity.Attendance;
import com.sanket.studentperformanceintelligencesystem.repository.AttendanceRepository;
import com.sanket.studentperformanceintelligencesystem.service.AttendanceService;@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public Attendance saveAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance payload is required.");
        }
        Long studentId = attendance.getStudent() != null ? attendance.getStudent().getStudentId() : null;
        Long subjectId = attendance.getSubject() != null ? attendance.getSubject().getSubjectId() : null;
        if (studentId != null && subjectId != null && attendance.getAttendanceDate() != null
                && attendanceRepository.existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDate(
                        studentId, subjectId, attendance.getAttendanceDate())) {
            throw new DuplicateResourceException(
                    "Attendance already exists for this student, subject and date. Edit the existing record instead.");
        }
        return attendanceRepository.save(attendance);
    }

    @Override
    public BulkSaveResult saveBulkAttendance(List<Attendance> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("No attendance records were provided.");
        }
        int saved = 0;
        int skipped = 0;
        List<Long> skippedStudentIds = new ArrayList<>();
        Set<Long> seenInBatch = new HashSet<>();

        for (Attendance record : records) {
            Long studentId = record.getStudent() != null ? record.getStudent().getStudentId() : null;
            if (studentId == null || record.getSubject() == null || record.getAttendanceDate() == null) {
                throw new IllegalArgumentException(
                        "Every attendance row needs a student, a subject and a date.");
            }
            // one row per student within a single session
            if (!seenInBatch.add(studentId)) {
                skipped++;
                skippedStudentIds.add(studentId);
                continue;
            }
            // already recorded for this subject/date (correction use case: edit instead)
            boolean exists = attendanceRepository
                    .existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDate(
                            studentId, record.getSubject().getSubjectId(), record.getAttendanceDate());
            if (exists) {
                skipped++;
                skippedStudentIds.add(studentId);
                continue;
            }
            attendanceRepository.save(record);
            saved++;
        }
        return new BulkSaveResult(saved, skipped, skippedStudentIds);
    }

    @Override
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @Override
    public Page<Attendance> getAllAttendance(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Attendance getAttendanceById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with ID: " + attendanceId));
    }

    @Override
    public Attendance updateAttendance(Long attendanceId, Attendance attendance) {
        Attendance existingAttendance = getAttendanceById(attendanceId);

        existingAttendance.setAttendanceDate(attendance.getAttendanceDate());
        existingAttendance.setStatus(attendance.getStatus());
        existingAttendance.setStudent(attendance.getStudent());
        existingAttendance.setSubject(attendance.getSubject());

        return attendanceRepository.save(existingAttendance);
    }

    @Override
    public void deleteAttendance(Long attendanceId) {
        Attendance attendance = getAttendanceById(attendanceId);

        attendanceRepository.delete(attendance);
    }

    @Override
    public List<Attendance> searchAttendance(Long studentId,
                                             Long subjectId,
                                             AttendanceStatus status,
                                             LocalDate startDate,
                                             LocalDate endDate,
                                             Long departmentId){

        Specification<Attendance> spec = Specification.unrestricted();

        if(studentId != null)
            spec = spec.and(AttendanceSpecification.hasStudentId(studentId));

        if(subjectId != null)
            spec =spec.and(AttendanceSpecification.hasSubjectId(subjectId));

        if(status != null)
            spec = spec.and(AttendanceSpecification.hasStatus(status));

        if(startDate != null)
            spec = spec.and(AttendanceSpecification.hasStartDate(startDate));

        if(endDate != null)
            spec = spec.and(AttendanceSpecification.hasEndDate(endDate));

        if(departmentId != null)
            spec = spec.and(AttendanceSpecification.hasDepartmentId(departmentId));

        return attendanceRepository.findAll(spec);
    }
}