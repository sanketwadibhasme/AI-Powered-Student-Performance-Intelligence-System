package com.sanket.studentperformanceintelligencesystem.service;

import com.sanket.studentperformanceintelligencesystem.entity.Marks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MarksService {
    Marks saveMarks(Marks marks);

    List<Marks> getAllMarks();

    Page<Marks> getAllMarks(Pageable pageable);
    Marks getMarksById(Long marksId);

    Marks updateMarks(Long marksId,Marks updatedMarks);

    void deleteMarks(Long marksId);

    List<Marks> searchMarks(
            Long studentId,
            Long subjectId,
            Long examId,
            Double minMarks,
            Double maxMarks,
            Long departmentId
    );
}
