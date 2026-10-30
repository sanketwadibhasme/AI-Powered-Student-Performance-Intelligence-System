package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.dto.*;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Report & Ranking",
        description = "APIs for generating performance reports and academic ranking"
)
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(
            summary = "Get department performance report",
            description = "Retrieves the performance report for all departments including average marks, average attendance, total students, faculty, and subjects."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Department performance report retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/departments")
    public List<DepartmentPerformanceDTO> getDepartmentPerformanceReport() {
        return reportService.getDepartmentPerformanceReport();
    }

    @Operation(
            summary = "Get student performance report",
            description = "Retrieves the performance report for all students including academic performance and attendance details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Student performance report retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/students")
    public List<StudentPerformanceDTO> getStudentPerformanceReport() {
        return reportService.getStudentPerformanceReport();
    }

    @Operation(
            summary = "Get subject performance report",
            description = "Retrieves the performance report for all subjects including average marks and attendance statistics."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Subject performance report retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/subjects")
    public List<SubjectPerformanceDTO> getSubjectPerformanceReport() {
        return reportService.getSubjectPerformanceReport();
    }

    @Operation(
            summary = "Get department rankings",
            description = "Retrieves departments ranked according to overall academic performance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Department rankings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/department-ranking")
    public List<DepartmentRankingDTO> getDepartmentRanking() {
        return reportService.getDepartmentRanking();
    }

    @Operation(
            summary = "Get student rankings",
            description = "Retrieves students ranked by their overall academic performance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Student rankings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/student-ranking")
    public List<StudentRankingDTO> getStudentRanking() {
        return reportService.getStudentRanking();
    }

    @Operation(
            summary = "Get subject rankings",
            description = "Retrieves subjects ranked according to overall academic performance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Subject rankings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/subject-ranking")
    public List<SubjectRankingDTO> getSubjectRanking() {
        return reportService.getSubjectRanking();
    }

    @Operation(
            summary = "Get highest attendance students",
            description = "Retrieves students ranked in descending order based on attendance percentage."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Highest attendance rankings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/highest-attendance")
    public List<StudentAttendanceRankingDTO> getHighestAttendanceStudents() {
        return reportService.getHighestAttendanceStudents();
    }

    @Operation(
            summary = "Get lowest attendance students",
            description = "Retrieves students ranked in ascending order based on attendance percentage."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Lowest attendance rankings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })    @GetMapping("/lowest-attendance")
    public List<StudentAttendanceRankingDTO> getLowestAttendanceStudents() {
        return reportService.getLowestAttendanceStudents();
    }
}