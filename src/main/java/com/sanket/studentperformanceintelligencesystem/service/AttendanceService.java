package com.sanket.studentperformanceintelligencesystem.service;

import com.sanket.studentperformanceintelligencesystem.entity.Attendance;
import com.sanket.studentperformanceintelligencesystem.enums.AttendanceStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    /**
     * Saves one attendance record. Rejects an exact duplicate
     * (same student + subject + date) with a DuplicateResourceException.
     */
    Attendance saveAttendance(Attendance attendance);

    /**
     * Saves a whole class session in one transaction. Rows for students
     * who already have a record for that subject/date are SKIPPED (not
     * duplicated) and reported in the returned summary.
     */
    BulkSaveResult saveBulkAttendance(List<Attendance> records);

    List<Attendance> getAllAttendance();

    Page<Attendance> getAllAttendance(Pageable pageable);
    Attendance getAttendanceById(Long AttendanceId);

    Attendance updateAttendance(Long attendanceId, Attendance updatedAttendance);

    void deleteAttendance(Long AttendanceId);

    List<Attendance> searchAttendance(
            Long studentId,
            Long subjectId,
            AttendanceStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Long departmentId
    );

    /** Outcome summary of a bulk attendance save. */
    record BulkSaveResult(int saved, int skipped, List<Long> skippedStudentIds) {
        public BulkSaveResult {
            skippedStudentIds = skippedStudentIds == null ? List.of() : List.copyOf(skippedStudentIds);
        }
    }
}
