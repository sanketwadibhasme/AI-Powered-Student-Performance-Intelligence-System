package com.sanket.studentperformanceintelligencesystem.service;

import com.sanket.studentperformanceintelligencesystem.entity.Faculty;

import java.util.List;

public interface FacultyService {

    Faculty saveFaculty(Faculty faculty);

    List<Faculty> getAllFaculties();

    Faculty getFacultyById(Long facultyId);

    Faculty updateFaculty(Long facultyId, Faculty faculty);

    void deleteFaculty(Long facultyId);

    List<Faculty> searchFaculties(
            String facultyName,
            String employeeCode,
            Long departmentId,
            String designation,
            String email
    );
}