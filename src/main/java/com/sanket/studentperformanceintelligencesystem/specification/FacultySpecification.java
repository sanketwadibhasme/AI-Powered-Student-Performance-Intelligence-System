package com.sanket.studentperformanceintelligencesystem.specification;

import com.sanket.studentperformanceintelligencesystem.entity.Faculty;
import org.springframework.data.jpa.domain.Specification;

public class FacultySpecification {
    //1. by faculty name
    public static Specification<Faculty> hasFacultyName(String facultyName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("facultyName")), "%" + facultyName.toLowerCase() + "%");
    }

    //2. by employee code
    public static Specification<Faculty> hasEmployeeCode(String employeeCode) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeCode")), "%" + employeeCode.toLowerCase() + "%");
    }

    //3. by department  id
    public static Specification<Faculty> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("department").get("departmentId"), departmentId);
    }

    //4. by designation
    public static Specification<Faculty> hasDesignation(String designation) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("designation")), "%" + designation.toLowerCase() + "%");
    }

    //5. by email
    public static Specification<Faculty> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("email")), email.toLowerCase());
    }


}
