package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.Subject;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.SubjectRepository;
import com.sanket.studentperformanceintelligencesystem.service.SubjectService;
import com.sanket.studentperformanceintelligencesystem.specification.SubjectSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public Subject saveSubject(Subject subject) {
        //Subject existingSubject = subjectRepository.findSubjectBySubjectCode(subject.getSubjectCode());

//        if (existingSubject != null) {
//            throw new IllegalArgumentException("Subject code already exists.");
//        }

        return subjectRepository.save(subject);
    }

    @Override
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Override
    public Subject getSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));
    }

    @Override
    public Subject updateSubject(Long subjectId, Subject subject) {
        Subject existingSubject = getSubjectById(subjectId);

//        Subject duplicateSubject = subjectRepository.findSubjectBySubjectCode(subject.getSubjectCode());
//        if (duplicateSubject != null && !duplicateSubject.getSubjectId().equals(subjectId)) {
//            throw new IllegalArgumentException("Subject code already exists.");
//        }

        existingSubject.setSubjectName(subject.getSubjectName());
        existingSubject.setSubjectCode(subject.getSubjectCode());
        existingSubject.setCredits(subject.getCredits());
        existingSubject.setDepartment(subject.getDepartment());
        existingSubject.setFaculty(subject.getFaculty());

        return subjectRepository.save(existingSubject);
    }

    @Override
    public void deleteSubject(Long subjectId) {
        Subject subject = getSubjectById(subjectId);

        subjectRepository.delete(subject);
    }

    @Override
    public List<Subject> searchSubjects(
            String subjectName,
            String subjectCode,
            Long departmentId,
            Long facultyId,
            Integer credits
    ){
        Specification<Subject> specification = Specification.unrestricted();

        if(subjectName != null && !subjectName.isBlank()){
            specification = specification.and(
                    SubjectSpecification.hasSubjectName(subjectName));
        }

        if(subjectCode != null && !subjectCode.isBlank()){
            specification = specification.and(
                    SubjectSpecification.hasSubjectCode(subjectCode));
        }

        if(departmentId != null){
            specification = specification.and(
                    SubjectSpecification.hasDepartmentId(departmentId));
        }

        if (facultyId != null) {
            specification = specification.and(
                    SubjectSpecification.hasFacultyId(facultyId));
        }

        if (credits != null) {
            specification = specification.and(
                    SubjectSpecification.hasCredits(credits));
        }

        return subjectRepository.findAll(specification);
    }

}