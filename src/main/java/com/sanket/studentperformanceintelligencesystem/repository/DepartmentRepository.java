package com.sanket.studentperformanceintelligencesystem.repository;

import com.sanket.studentperformanceintelligencesystem.dto.DepartmentRankingProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sanket.studentperformanceintelligencesystem.entity.Department;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>{
    @Query("""
SELECT
    d.departmentId AS departmentId,
    d.departmentName AS departmentName,
    COALESCE(AVG(m.marksObtained),0) AS averageMarks
FROM Department d

LEFT JOIN Student s
ON s.department = d

LEFT JOIN Marks m
ON m.student = s

GROUP BY
    d.departmentId,
    d.departmentName
""")
    List<DepartmentRankingProjection> getDepartmentRankingData();
}
