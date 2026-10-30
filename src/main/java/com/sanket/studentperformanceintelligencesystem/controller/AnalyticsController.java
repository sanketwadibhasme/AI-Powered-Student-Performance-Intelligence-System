package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.entity.Department;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Analytics",
        description = "APIs for retrieving academic performance analytics, attendance statistics, and institutional insights."
)
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }


    @Operation(
            summary = "Get student's average marks",
            description = "Calculates the average marks obtained by a student across all examinations."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Average marks calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/student/{studentId}/student-average-marks")
    public Double calculateAverageMarksByStudentId(@PathVariable Long studentId) {
        return analyticsService.calculateAverageMarksByStudentId(studentId);

    }


    @Operation(
            summary = "Get student's attendance percentage",
            description = "Calculates the attendance percentage of a student."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Attendance percentage calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/student/{studentId}/student-attendance-percentage")
    public Double calculateAttendancePercentageByStudentId(@PathVariable Long studentId){
        return analyticsService.calculateAttendancePercentageByStudentId(studentId);
    }


    @Operation(
            summary = "Get subject average marks",
            description = "Calculates the average marks of all students for the specified subject."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Average marks calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/subject/{subjectId}/subject-average-marks")
    public Double getAverageMarksBySubject(@PathVariable Long subjectId) {
        return analyticsService.getAverageMarksBySubject(subjectId);
    }


    @Operation(
            summary = "Get department average marks",
            description = "Calculates the average marks of students belonging to the specified department."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Average marks calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/department/{departmentId}/department-average-marks")
    public Double getAverageMarksByDepartment(@PathVariable Long departmentId) {
        return analyticsService.getAverageMarksByDepartment(departmentId);
    }


    @Operation(
            summary = "Get subject attendance percentage",
            description = "Calculates the attendance percentage for the specified subject."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Attendance percentage calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/subject/{subjectId}/subject-attendance-percentage")
    public Double getAttendancePercentageBySubject(@PathVariable Long subjectId) {
        return analyticsService.getAttendancePercentageBySubject(subjectId);
    }


    @Operation(
            summary = "Get department attendance percentage",
            description = "Calculates the attendance percentage of students in the specified department."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Attendance percentage calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error")
    })
    @GetMapping("/department/{departmentId}/department-attendance-percentage")
    public Double getAttendancePercentageByDepartment(@PathVariable Long departmentId) {
        return analyticsService.getAttendancePercentageByDepartment(departmentId);
    }


    @Operation(
            summary = "Get students below attendance threshold",
            description = "Retrieves students whose attendance percentage is below the specified threshold."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid threshold value",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/student/below-attendance/{threshold}")
    public List<Student> getStudentsBelowAttendanceThreshold(@PathVariable Double threshold) {
        return analyticsService.getStudentsBelowAttendanceThreshold(threshold);
    }


    @Operation(
            summary = "Get students below marks threshold",
            description = "Retrieves students whose average marks are below the specified threshold."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid threshold value",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/student/below-marks/{threshold}")
    public List<Student> getStudentsBelowMarksThreshold(@PathVariable Double threshold) {
        return analyticsService.getStudentsBelowMarksThreshold(threshold);
    }


    @Operation(
            summary = "Get college average marks",
            description = "Calculates the overall average marks across the institution."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Overall average marks calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/college/college-average-marks")
    public Double getOverallAverageMarks() {
        return analyticsService.getOverallAverageMarks();
    }


    @Operation(
            summary = "Get college attendance percentage",
            description = "Calculates the overall attendance percentage across the institution."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Overall attendance percentage calculated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/college/college-attendance-percentage")
    public Double getOverallAttendancePercentage() {
        return analyticsService.getOverallAttendancePercentage();
    }


    @Operation(
            summary = "Get top performing department",
            description = "Retrieves the department with the highest overall academic performance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Top performing department retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "No department data available",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/department/top-performing")
    public Department getTopPerformingDepartment() {
        return analyticsService.getTopPerformingDepartment();
    }
}
