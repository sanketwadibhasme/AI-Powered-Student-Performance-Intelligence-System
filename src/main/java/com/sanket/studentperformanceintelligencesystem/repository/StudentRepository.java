package com.sanket.studentperformanceintelligencesystem.repository;

import com.sanket.studentperformanceintelligencesystem.dto.StudentRankingDTO;
import com.sanket.studentperformanceintelligencesystem.dto.StudentRankingProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Long countByDepartmentDepartmentId(Long departmentId);

    @Query("""
SELECT
    s.studentId AS studentId,
    s.firstName AS firstName,
    s.lastName AS lastName,
    s.rollNumber AS rollNumber,
    s.department.departmentName AS departmentName,
    COALESCE(AVG(m.marksObtained),0) AS averageMarks
FROM Student s

LEFT JOIN Marks m
ON m.student = s

GROUP BY
    s.studentId,
    s.firstName,
    s.lastName,
    s.rollNumber,
    s.department.departmentName
""")
    List<StudentRankingProjection> getStudentRankingData();

}
