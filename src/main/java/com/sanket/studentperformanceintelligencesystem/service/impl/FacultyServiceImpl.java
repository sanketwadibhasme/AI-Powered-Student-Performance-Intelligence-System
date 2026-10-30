package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.Faculty;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.FacultyRepository;
import com.sanket.studentperformanceintelligencesystem.service.FacultyService;
import com.sanket.studentperformanceintelligencesystem.specification.FacultySpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    public FacultyServiceImpl(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @Override
    public Faculty saveFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    @Override
    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    @Override
    public Faculty getFacultyById(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID " + facultyId));
    }

    @Override
    public Faculty updateFaculty(Long facultyId, Faculty faculty) {
        Faculty existingFaculty = getFacultyById(facultyId);

        existingFaculty.setFacultyName(faculty.getFacultyName());
        existingFaculty.setEmployeeCode(faculty.getEmployeeCode());
        existingFaculty.setEmail(faculty.getEmail());
        existingFaculty.setPhone(faculty.getPhone());
        existingFaculty.setDesignation(faculty.getDesignation());
        existingFaculty.setDepartment(faculty.getDepartment());

        return facultyRepository.save(existingFaculty);
    }

    @Override
    public void deleteFaculty(Long facultyId) {
        Faculty faculty = getFacultyById(facultyId);

        facultyRepository.delete(faculty);
    }

    @Override
    public List<Faculty> searchFaculties(
            String facultyName,
            String employeeCode,
            Long departmentId,
            String designation,
            String email) {

        Specification<Faculty> specification = Specification.unrestricted();

        if (facultyName != null && !facultyName.isBlank()) {
            specification = specification.and(
                    FacultySpecification.hasFacultyName(facultyName));
        }

        if(employeeCode != null && !employeeCode.isBlank()){
            specification = specification.and(
                    FacultySpecification.hasEmployeeCode(employeeCode));
        }

        if (departmentId != null) {
            specification = specification.and(
                    FacultySpecification.hasDepartmentId(departmentId));
        }

        if(designation != null && !designation.isBlank()){
            specification = specification.and(
                    FacultySpecification.hasDesignation(designation));
        }

        if (email != null && !email.isBlank()) {
            specification = specification.and(
                    FacultySpecification.hasEmail(email));
        }

        return facultyRepository.findAll(specification);
    }
}