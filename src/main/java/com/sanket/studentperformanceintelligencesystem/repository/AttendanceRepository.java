package com.sanket.studentperformanceintelligencesystem.repository;

import com.sanket.studentperformanceintelligencesystem.dto.StudentAttendanceRankingProjection;
import com.sanket.studentperformanceintelligencesystem.entity.Attendance;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance,Long>, JpaSpecificationExecutor<Attendance> {

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.studentId = :studentId")
    Long countTotalClassesByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.studentId = :studentId AND a.status = 'PRESENT'")
    Long countPresentClassesByStudentId(@Param("studentId") Long studentId);

    /** Duplicate guard for the take-attendance workflow: same student + subject + date. */
    boolean existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDate(Long studentId, Long subjectId, java.time.LocalDate date);

    /** Records already taken for a subject on a date (used to prefill/correct a session). */
    List<Attendance> findBySubjectSubjectIdAndAttendanceDate(Long subjectId, java.time.LocalDate date);

    /** Attendance percentage for one student in one subject (faculty class view). */
    @Query("""
       SELECT (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0) / COUNT(a)
       FROM Attendance a
       WHERE a.student.studentId = :studentId AND a.subject.subjectId = :subjectId
       """)
    Double findAttendancePercentageByStudentAndSubject(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    @Query("""
       SELECT
       (COUNT(CASE WHEN a.status = 'Present' THEN 1 END) * 100.0)/ COUNT(a)
       FROM Attendance a
       WHERE a.subject.subjectId = :subjectId
       """)
    Double findAttendancePercentageBySubjectId(Long subjectId);

    @Query("""
       SELECT
       (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0)
       / COUNT(a)
       FROM Attendance a
       WHERE a.student.department.departmentId = :departmentId
       """)
    Double findAttendancePercentageByDepartmentId(Long departmentId);

    @Query("""
       SELECT s
       FROM Student s
       WHERE (
           SELECT
           (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0)
           / COUNT(a)
           FROM Attendance a
           WHERE a.student.studentId = s.studentId
       ) < :threshold
       """)
    List<Student> findStudentsBelowAttendanceThreshold(Double threshold);

    @Query("""
       SELECT
       (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0)
       / COUNT(a)
       FROM Attendance a
       """)
    Double findOverallAttendancePercentage();

    @Query("""
SELECT
    s.studentId AS studentId,
    s.firstName AS firstName,
    s.lastName AS lastName,
    s.rollNumber AS rollNumber,
    s.department.departmentName AS departmentName,
    SUM(
            CASE
                WHEN a.status = 'PRESENT' THEN 1
                ELSE 0
            END
        ) AS presentClasses,
        COUNT(a) AS totalClasses

FROM Student s

LEFT JOIN Attendance a
ON a.student = s

GROUP BY
    s.studentId,
    s.firstName,
    s.lastName,
    s.rollNumber,
    s.department.departmentName
""")
    List<StudentAttendanceRankingProjection> getStudentAttendanceRanking();
}

