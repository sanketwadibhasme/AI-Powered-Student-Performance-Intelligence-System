package com.sanket.studentperformanceintelligencesystem.service;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.enums.Gender;
import com.sanket.studentperformanceintelligencesystem.enums.StudentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {
    Student saveStudent(Student student);

    List<Student> getAllStudents();

    Page<Student> getAllStudents(Pageable pageable);

    Student getStudentById(Long studentId);

    Student updateStudent(Long studentId, Student student);

    void deleteStudent(Long studentId);

    List<Student> searchStudents(
            String rollNumber,
            String firstName,
            String lastName,
            Long departmentId,
            Integer semester,
            Integer admissionYear,
            Gender gender,
            StudentStatus status
    );
}
