package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.Exam;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.ExamRepository;
import com.sanket.studentperformanceintelligencesystem.service.ExamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {
    private final ExamRepository examRepository;
    public ExamServiceImpl(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    public Exam saveExam(Exam exam) {
        return examRepository.save(exam);
    }

    @Override
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }
    @Override
    public Exam getExamById(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));
    }

    @Override
    public Exam updateExam(Long examId, Exam updatedExam) {
        Exam existingExam = getExamById(examId);

        existingExam.setExamName(updatedExam.getExamName());
        existingExam.setExamDate(updatedExam.getExamDate());
        existingExam.setTotalMarks(updatedExam.getTotalMarks());

        return examRepository.save(existingExam);
    }

    @Override
    public void deleteExam(Long examId) {
        Exam exam = getExamById(examId);

        examRepository.delete(exam);
    }
}
