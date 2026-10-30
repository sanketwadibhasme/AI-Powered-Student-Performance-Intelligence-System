package com.sanket.studentperformanceintelligencesystem.repository;

import com.sanket.studentperformanceintelligencesystem.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment,Long>, JpaSpecificationExecutor<Enrollment> {
    Long countBySubjectSubjectId(Long subjectId);
}
