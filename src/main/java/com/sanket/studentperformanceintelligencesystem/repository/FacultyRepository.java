package com.sanket.studentperformanceintelligencesystem.repository;

import com.sanket.studentperformanceintelligencesystem.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty,Long>, JpaSpecificationExecutor<Faculty> {
    Long countByDepartmentDepartmentId(Long departmentId);

    /** Faculty accounts have no FK column in app_users; matched by unique email. */
    Optional<Faculty> findByEmail(String email);
}
