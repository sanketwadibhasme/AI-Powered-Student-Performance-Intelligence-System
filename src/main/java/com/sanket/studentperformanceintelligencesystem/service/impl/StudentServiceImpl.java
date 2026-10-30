package com.sanket.studentperformanceintelligencesystem.service.impl;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.enums.Gender;
import com.sanket.studentperformanceintelligencesystem.enums.StudentStatus;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.StudentRepository;
import com.sanket.studentperformanceintelligencesystem.service.StudentService;

import com.sanket.studentperformanceintelligencesystem.specification.StudentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID " + studentId));
    }

    @Override
    public Student updateStudent(Long studentId, Student student) {
        Student existingStudent = getStudentById(studentId);    //search by id our-method

        existingStudent.setRollNumber(student.getRollNumber());
        existingStudent.setFirstName(student.getFirstName());
        existingStudent.setLastName(student.getLastName());
        existingStudent.setEmail(student.getEmail());
        existingStudent.setPhone(student.getPhone());
        existingStudent.setGender(student.getGender());
        existingStudent.setDob(student.getDob());
        existingStudent.setAdmissionYear(student.getAdmissionYear());
        existingStudent.setCurrentSemester(student.getCurrentSemester());
        existingStudent.setStatus(student.getStatus());
        existingStudent.setDepartment(student.getDepartment());

        return studentRepository.save(existingStudent);
    }

    @Override
    public void deleteStudent(Long studentId) {
        Student student = getStudentById(studentId);    //in case id does not exist
        studentRepository.delete(student);
    }

    @Override
    public List<Student> searchStudents(
            String rollNumber,
            String firstName,
            String lastName,
            Long departmentId,
            Integer semester,
            Integer admissionYear,
            Gender gender,
            StudentStatus status) {

        Specification<Student> specification = Specification.unrestricted(); //similar to where(null)

        if (rollNumber != null && !rollNumber.isBlank()) {
            specification = specification.and(
                    StudentSpecification.hasRollnumber(rollNumber));
        }

        if (firstName != null && !firstName.isBlank()) {
            specification = specification.and(
                    StudentSpecification.hasFirstName(firstName));
        }

        if (lastName != null && !lastName.isBlank()) {
            specification = specification.and(
                    StudentSpecification.hasLastName(lastName));
        }

        if (departmentId != null) {
            specification = specification.and(
                    StudentSpecification.hasDepartmentId(departmentId));
        }

        if (semester != null) {
            specification = specification.and(
                    StudentSpecification.hasSemester(semester));
        }

        if (admissionYear != null) {
            specification = specification.and(
                    StudentSpecification.hasAdmissionYear(admissionYear));
        }

        if (gender != null) {
            specification = specification.and(
                    StudentSpecification.hasGender(gender));
        }

        if (status != null) {
            specification = specification.and(
                    StudentSpecification.hasStatus(status));
        }

        return studentRepository.findAll(specification);
    }

}
