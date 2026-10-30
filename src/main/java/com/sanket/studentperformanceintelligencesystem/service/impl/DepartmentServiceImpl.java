package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.Department;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.DepartmentRepository;
import com.sanket.studentperformanceintelligencesystem.service.DepartmentService;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService{
    //constructor with repository
    private final DepartmentRepository departmentRepository;
    public DepartmentServiceImpl(DepartmentRepository departmentRepository){
        this.departmentRepository=departmentRepository;
    }

    //Write services
    @Override
    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID " + departmentId));
    }

    @Override
    public Department updateDepartment(Long departmentId, Department department) {
        Department existingDepartment = getDepartmentById(departmentId);

        existingDepartment.setDepartmentName(department.getDepartmentName());
        existingDepartment.setDepartmentCode(department.getDepartmentCode());
        existingDepartment.setHodName(department.getHodName());

        return departmentRepository.save(existingDepartment);

    }

    @Override
    public void deleteDepartment(Long departmentId) {
        Department department = getDepartmentById(departmentId);

        departmentRepository.delete(department);
    }
}
