package com.sanket.studentperformanceintelligencesystem.service;
import com.sanket.studentperformanceintelligencesystem.entity.Enrollment;

import java.util.List;

public interface EnrollmentService {
    Enrollment saveEnrollment(Enrollment enrollment);

    List<Enrollment> getAllEnrollments();
    Enrollment getEnrollmentById(Long enrollmentId);

    Enrollment updateEnrollment(Long enrollmentId, Enrollment enrollment);

    void deleteEnrollment(Long enrollment);

    List<Enrollment> searchEnrollments(
            String academicYear,
            Integer semester,
            Long studentId,
            Long departmentId,
            Long subjectId
    );
}
