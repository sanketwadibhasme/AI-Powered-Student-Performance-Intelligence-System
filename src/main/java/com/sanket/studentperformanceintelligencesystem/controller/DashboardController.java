package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.dto.DashboardSummaryDTO;
import com.sanket.studentperformanceintelligencesystem.dto.DepartmentPerformanceDTO;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Dashboard",
        description = "APIs for retrieving dashboard summary and performance metrics."
)
@RestController
public class DashboardController {

    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(
            summary = "Get dashboard summary",
            description = "Retrieves the overall dashboard summary including institution-wide statistics such as total students, departments, faculty, subjects, average marks, average attendance, and students below defined thresholds."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Dashboard summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/api/dashboard/summary")
    public DashboardSummaryDTO getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }


    @Operation(
            summary = "Get department performance summary",
            description = "Retrieves summarized performance information for all departments to support dashboard visualization and reporting."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Department performance summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/api/dashboard/departments-summary")
    public List<DepartmentPerformanceDTO> getDepartmentPerformanceReport() {
        return dashboardService.getDepartmentPerformanceReport();
    }
}
