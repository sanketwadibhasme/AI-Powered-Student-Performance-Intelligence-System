package com.sanket.studentperformanceintelligencesystem.service;

import com.sanket.studentperformanceintelligencesystem.entity.Subject;

import java.util.List;

public interface SubjectService {

    Subject saveSubject(Subject subject);

    List<Subject> getAllSubjects();

    Subject getSubjectById(Long subjectId);

    Subject updateSubject(Long subjectId, Subject subject);

    void deleteSubject(Long subjectId);

    List<Subject> searchSubjects(
            String subjectName,
            String subjectCode,
            Long departmentId,
            Long facultyId,
            Integer credits
    );
}