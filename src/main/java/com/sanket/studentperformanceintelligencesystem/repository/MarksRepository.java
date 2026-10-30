package com.sanket.studentperformanceintelligencesystem.repository;

import com.sanket.studentperformanceintelligencesystem.entity.Department;
import com.sanket.studentperformanceintelligencesystem.entity.Marks;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarksRepository extends JpaRepository<Marks,Long>, JpaSpecificationExecutor<Marks> {

    /**
     * All marks for one student ordered chronologically (exam date, then
     * record id as a stable tiebreaker). Used by the AI feature extraction
     * instead of scanning the whole marks table.
     */
    @Query("SELECT m FROM Marks m WHERE m.student.studentId = :studentId ORDER BY m.exam.examDate ASC, m.marksId ASC")
    List<Marks> findByStudentStudentIdOrderByExamExamDateAscMarksIdAsc(@Param("studentId") Long studentId);

    @Query("SELECT AVG(m.marksObtained) FROM Marks m WHERE m.student.studentId = :studentId")
    Double calculateAverageMarksByStudentId(@Param("studentId") Long studentId);

    /** Duplicate guard: one marks record per student + subject + exam. */
    boolean existsByStudentStudentIdAndSubjectSubjectIdAndExamExamId(Long studentId, Long subjectId, Long examId);

    /** Same lookup returning the record (used to redirect duplicate edits). */
    Marks findFirstByStudentStudentIdAndSubjectSubjectIdAndExamExamId(Long studentId, Long subjectId, Long examId);

    @Query("SELECT AVG(m.marksObtained) FROM Marks m WHERE m.subject.subjectId = :subjectId")
    Double findAverageMarksBySubjectId(Long subjectId);

    @Query("SELECT AVG(m.marksObtained) FROM Marks m WHERE m.student.department.departmentId = :departmentId")
    Double findAverageMarksByDepartmentId(Long departmentId);

    @Query("""
       SELECT s
       FROM Student s
       WHERE (
           SELECT AVG(m.marksObtained)
           FROM Marks m
           WHERE m.student.studentId = s.studentId
       ) < :threshold
       """)
    List<Student> findStudentsBelowMarksThreshold(Double threshold);

    @Query("SELECT AVG(m.marksObtained) FROM Marks m")
    Double findOverallAverageMarks();

    @Query("""
       SELECT m.student.department
       FROM Marks m
       GROUP BY m.student.department
       ORDER BY AVG(m.marksObtained) DESC
       """)
    List<Department> findDepartmentsByAverageMarksDesc(Pageable pageable);
}
