package com.sanket.studentperformanceintelligencesystem.specification;

import com.sanket.studentperformanceintelligencesystem.entity.Enrollment;
import org.springframework.data.jpa.domain.Specification;

public class EnrollmentSpecification {
    //1.by academic year
    public static Specification<Enrollment> hasAcademicYear(String academicYear) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("academicYear"), academicYear);
    }

    //2.by semester
    public static Specification<Enrollment> hasSemester(Integer semester) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("semester"), semester);
    }

    //3. by student id
    public static Specification<Enrollment> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("student").get("studentId"), studentId);
    }

    //4. by department id
    public static Specification<Enrollment> hasDepartmentId(Long departmentId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("student").get("department").get("departmentId"), departmentId);
    }

    //5.by subject id
    public static Specification<Enrollment> hasSubjectId(Long subjectId){
        return (root, query, criteriaBuilder) ->
               criteriaBuilder.equal(root.get("subject").get("subjectId"), subjectId);
    }
}
