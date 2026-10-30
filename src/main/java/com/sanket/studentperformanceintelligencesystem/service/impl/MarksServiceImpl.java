package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.Exam;
import com.sanket.studentperformanceintelligencesystem.entity.Marks;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.ExamRepository;
import com.sanket.studentperformanceintelligencesystem.repository.MarksRepository;
import com.sanket.studentperformanceintelligencesystem.service.MarksService;
import com.sanket.studentperformanceintelligencesystem.specification.MarksSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarksServiceImpl implements MarksService {
    private final MarksRepository marksRepository;
    private final ExamRepository examRepository;
    public MarksServiceImpl(MarksRepository marksRepository, ExamRepository examRepository) {
        this.marksRepository = marksRepository;
        this.examRepository = examRepository;
    }

    @Override
    public Marks saveMarks(Marks marks){
        Double marksObtained = marks.getMarksObtained();

        Exam exam = examRepository.findById(marks.getExam().getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID " + marks.getExam().getExamId()));
        Integer totalMarks = exam.getTotalMarks();

        if(marksObtained > totalMarks.doubleValue()){
            throw new IllegalArgumentException("Marks obtained cannot exceed total marks.");
        }

        // duplicate guard: one marks record per student + subject + exam
        if (marks.getStudent() != null && marks.getSubject() != null
                && marksRepository.existsByStudentStudentIdAndSubjectSubjectIdAndExamExamId(
                        marks.getStudent().getStudentId(),
                        marks.getSubject().getSubjectId(),
                        exam.getExamId())) {
            throw new com.sanket.studentperformanceintelligencesystem.exception.DuplicateResourceException(
                    "Marks already exist for this student, subject and exam. Edit the existing record instead.");
        }

        return marksRepository.save(marks);
    }

    @Override
    public List<Marks> getAllMarks(){
        return marksRepository.findAll();
    }

    @Override
    public Page<Marks> getAllMarks(Pageable pageable){
        return marksRepository.findAll(pageable);
    }
    @Override
    public Marks getMarksById(Long marksId){
        return marksRepository.findById(marksId)
                .orElseThrow(() -> new ResourceNotFoundException("Marks record not found with ID " + marksId));
    }

    @Override
    public Marks updateMarks(Long marksId, Marks marks) {
        Marks existingMarks = getMarksById(marksId);

        Double marksObtained = marks.getMarksObtained();

        Exam exam = examRepository.findById(marks.getExam().getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID " + marks.getExam().getExamId()));
        Integer totalMarks = exam.getTotalMarks();
        if (marksObtained > totalMarks.doubleValue()) {
            throw new IllegalArgumentException(
                    "Marks obtained cannot exceed total marks.");
        }

        // duplicate guard on update: target student+subject+exam must not already
        // belong to a different marks record
        Long targetStudent = marks.getStudent() != null ? marks.getStudent().getStudentId() : null;
        Long targetSubject = marks.getSubject() != null ? marks.getSubject().getSubjectId() : null;
        if (targetStudent != null && targetSubject != null) {
            Marks duplicate = marksRepository
                    .findFirstByStudentStudentIdAndSubjectSubjectIdAndExamExamId(
                            targetStudent, targetSubject, exam.getExamId());
            if (duplicate != null && !duplicate.getMarksId().equals(marksId)) {
                throw new com.sanket.studentperformanceintelligencesystem.exception.DuplicateResourceException(
                        "Another marks record already exists for this student, subject and exam.");
            }
        }

        existingMarks.setMarksObtained(marks.getMarksObtained());
        existingMarks.setStudent(marks.getStudent());
        existingMarks.setSubject(marks.getSubject());
        existingMarks.setExam(marks.getExam());

        return marksRepository.save(existingMarks);
    }

    @Override
    public void deleteMarks(Long marksId) {
        Marks marks = getMarksById(marksId);

        marksRepository.delete(marks);
    }

    @Override
    public List<Marks> searchMarks(Long studentId,
                                   Long subjectId,
                                   Long examId,
                                   Double minMarks,
                                   Double maxMarks,
                                   Long departmentId){

        Specification<Marks> spec = Specification.unrestricted();

        if(studentId != null)
            spec = spec.and(
                    MarksSpecification.hasStudentId(studentId));

        if(subjectId != null)
            spec = spec.and(
                    MarksSpecification.hasSubjectId(subjectId));

        if(examId != null)
            spec = spec.and(
                    MarksSpecification.hasExamId(examId));

        if(minMarks != null)
            spec = spec.and(
                    MarksSpecification.hasMinimumMarks(minMarks));

        if(maxMarks != null)
            spec = spec.and(
                    MarksSpecification.hasMaximumMarks(maxMarks));

        if(departmentId != null)
            spec = spec.and(
                    MarksSpecification.hasDepartmentId(departmentId));

        return marksRepository.findAll(spec);
    }
}
