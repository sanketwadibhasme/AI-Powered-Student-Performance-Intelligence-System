package com.sanket.studentperformanceintelligencesystem.specification;

import com.sanket.studentperformanceintelligencesystem.entity.Subject;
import org.springframework.data.jpa.domain.Specification;

public class SubjectSpecification {
    //1. by subject name
    public static Specification<Subject> hasSubjectName(String subjectName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("subjectName")), "%" + subjectName.toLowerCase() + "%");
    }

    //2. by subject code
    public static Specification<Subject> hasSubjectCode(String subjectCode) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("subjectCode"), subjectCode);
    }

    //3. by department id
    public static Specification<Subject> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("department").get("departmentId"), departmentId);
    }

    //4. by Faculty id
    public static Specification<Subject> hasFacultyId(Long facultyId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("faculty").get("facultyId"), facultyId);
    }

    //5. by credits
    public static Specification<Subject> hasCredits(Integer credits) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("credits"), credits);
    }
}
