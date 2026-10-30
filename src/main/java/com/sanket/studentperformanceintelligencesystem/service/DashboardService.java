package com.sanket.studentperformanceintelligencesystem.service;
import com.sanket.studentperformanceintelligencesystem.dto.DashboardSummaryDTO;
import com.sanket.studentperformanceintelligencesystem.dto.DepartmentPerformanceDTO;

import java.util.List;

public interface DashboardService {
    DashboardSummaryDTO getDashboardSummary();

    List<DepartmentPerformanceDTO> getDepartmentPerformanceReport();
}
