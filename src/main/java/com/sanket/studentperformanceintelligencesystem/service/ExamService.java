package com.sanket.studentperformanceintelligencesystem.service;
import com.sanket.studentperformanceintelligencesystem.entity.Exam;

import java.util.List;

public interface ExamService {
    Exam saveExam(Exam exam);

    List<Exam> getAllExams();
    Exam getExamById(Long examId);

    Exam updateExam(Long examId, Exam updatedExam);

    void deleteExam(Long examId);
}
