package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.Enrollment;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.EnrollmentRepository;
import com.sanket.studentperformanceintelligencesystem.service.EnrollmentService;
import com.sanket.studentperformanceintelligencesystem.specification.EnrollmentSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Enrollment saveEnrollment(Enrollment enrollment) {
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    @Override
    public Enrollment getEnrollmentById(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
    }

    @Override
    public Enrollment updateEnrollment(Long enrollmentId, Enrollment enrollment) {
        Enrollment existingEnrollment = getEnrollmentById(enrollmentId);

        existingEnrollment.setAcademicYear(enrollment.getAcademicYear());
        existingEnrollment.setSemester(enrollment.getSemester());
        existingEnrollment.setStudent(enrollment.getStudent());
        existingEnrollment.setSubject(enrollment.getSubject());

        return enrollmentRepository.save(existingEnrollment);
    }

    @Override
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment existingEnrollment = getEnrollmentById(enrollmentId);

        enrollmentRepository.delete(existingEnrollment);
    }

    @Override
    public List<Enrollment> searchEnrollments(
            String academicYear,
            Integer semester,
            Long studentId,
            Long departmentId,
            Long subjectId) {

        Specification<Enrollment> specification = Specification.unrestricted();

        if (academicYear != null && !academicYear.isBlank()) {
            specification = specification.and(
                    EnrollmentSpecification.hasAcademicYear(academicYear));
        }

        if (semester != null) {
            specification = specification.and(
                    EnrollmentSpecification.hasSemester(semester));
        }

        if (studentId != null) {
            specification = specification.and(
                    EnrollmentSpecification.hasStudentId(studentId));
        }

        if (departmentId != null) {
            specification = specification.and(
                    EnrollmentSpecification.hasDepartmentId(departmentId));
        }

        if(subjectId != null){
            specification = specification.and(
                    EnrollmentSpecification.hasSubjectId(subjectId));
        }

        return enrollmentRepository.findAll(specification);
    }
}