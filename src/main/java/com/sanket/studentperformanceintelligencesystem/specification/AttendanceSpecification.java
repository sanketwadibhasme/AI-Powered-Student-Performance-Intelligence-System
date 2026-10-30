package com.sanket.studentperformanceintelligencesystem.specification;

import com.sanket.studentperformanceintelligencesystem.entity.Attendance;
import com.sanket.studentperformanceintelligencesystem.enums.AttendanceStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AttendanceSpecification {

    public static Specification<Attendance> hasStudentId(Long studentId) {
        return (root, query, cb) ->
                cb.equal(root.get("student").get("studentId"), studentId);
    }

    public static Specification<Attendance> hasSubjectId(Long subjectId) {
        return (root, query, cb) ->
                cb.equal(root.get("subject").get("subjectId"), subjectId);
    }

    public static Specification<Attendance> hasStatus(AttendanceStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Attendance> hasDepartmentId(Long departmentId) {
        return (root, query, cb) ->
                cb.equal(root.get("student").get("department").get("departmentId"), departmentId);
    }

    public static Specification<Attendance> hasStartDate(LocalDate startDate) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("attendanceDate"), startDate);
    }

    public static Specification<Attendance> hasEndDate(LocalDate endDate) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("attendanceDate"), endDate);
    }
}
