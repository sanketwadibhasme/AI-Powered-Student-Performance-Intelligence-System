package com.sanket.studentperformanceintelligencesystem.repository;

import com.sanket.studentperformanceintelligencesystem.dto.SubjectRankingProjection;
import com.sanket.studentperformanceintelligencesystem.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject,Long>, JpaSpecificationExecutor<Subject> {
    Long countByDepartmentDepartmentId(Long departmentId);

    /** Subjects taught by one faculty member (subject-first faculty workflow). */
    java.util.List<Subject> findByFacultyFacultyId(Long facultyId);

    @Query("""
SELECT
    s.subjectId AS subjectId,
    s.subjectName AS subjectName,
    s.subjectCode AS subjectCode,
    s.department.departmentName AS departmentName,
    COALESCE(AVG(m.marksObtained), 0) AS averageMarks
FROM Subject s

LEFT JOIN Marks m
ON m.subject = s

GROUP BY
    s.subjectId,
    s.subjectName,
    s.subjectCode,
    s.department.departmentName
""")
    List<SubjectRankingProjection> getSubjectRankingData();
}
