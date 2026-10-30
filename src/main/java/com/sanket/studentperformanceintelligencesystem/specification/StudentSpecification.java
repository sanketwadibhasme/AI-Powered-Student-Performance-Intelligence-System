package com.sanket.studentperformanceintelligencesystem.specification;

import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.enums.Gender;
import com.sanket.studentperformanceintelligencesystem.enums.StudentStatus;
import org.springframework.data.jpa.domain.Specification;

public class StudentSpecification {
    //1. By Roll no.
    public static Specification<Student> hasRollnumber(String rollNumber){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("rollNumber"), rollNumber);
    }

    //2. by first name (case insensitive)
    public static Specification<Student> hasFirstName(String firstName){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),"%" + firstName.toLowerCase() + "%");
    }

    //3. by last name
    public static Specification<Student> hasLastName(String lastName){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%"+lastName.toLowerCase()+"%");
    }

    //4. by departmentId
    public static Specification<Student> hasDepartmentId(Long departmentId){
        return (root, query, criteriaBuilder)->
                criteriaBuilder.equal(root.get("department").get("departmentId"), departmentId);
    }

    //5. by Semester
    public static Specification<Student> hasSemester(Integer sem){
        return (root, query, criteriaBuilder)->
                criteriaBuilder.equal(root.get("currentSemester"), sem);
    }

    //6. by admission year
    public static Specification<Student> hasAdmissionYear(Integer admissionYear) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("admissionYear"), admissionYear);
    }

    //7. by gender
    public static Specification<Student> hasGender(Gender gender){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("gender"), gender);
    }

    //8. by status
    public static Specification<Student> hasStatus(StudentStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }
}
