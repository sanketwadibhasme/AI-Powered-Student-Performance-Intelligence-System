package com.sanket.studentperformanceintelligencesystem.specification;

import com.sanket.studentperformanceintelligencesystem.entity.Marks;
import org.springframework.data.jpa.domain.Specification;

public class MarksSpecification {

    public static Specification<Marks> hasStudentId(Long studentId) {
        return (root, query, cb) ->
                cb.equal(root.get("student").get("studentId"), studentId);
    }

    public static Specification<Marks> hasSubjectId(Long subjectId) {
        return (root, query, cb) ->
                cb.equal(root.get("subject").get("subjectId"), subjectId);
    }

    public static Specification<Marks> hasExamId(Long examId) {
        return (root, query, cb) ->
                cb.equal(root.get("exam").get("examId"), examId);
    }

    public static Specification<Marks> hasDepartmentId(Long departmentId) {
        return (root, query, cb) ->
                cb.equal(root.get("student").get("department").get("departmentId"), departmentId);
    }

    public static Specification<Marks> hasMinimumMarks(Double minMarks) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("marksObtained"), minMarks);
    }

    public static Specification<Marks> hasMaximumMarks(Double maxMarks) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("marksObtained"), maxMarks);
    }
}
