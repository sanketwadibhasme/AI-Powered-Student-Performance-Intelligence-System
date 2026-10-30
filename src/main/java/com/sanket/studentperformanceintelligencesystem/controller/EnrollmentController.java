package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.entity.Enrollment;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Enrollment Management",
        description = "APIs for managing enrollments and related functionalities."
)
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Create Enrollment
    @Operation(
            summary = "Create a new enrollment entry.",
            description = "Creates a new enrollment record after validating all required fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Entry Created Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409",
                    description = "Duplicate data found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Enrollment saveEnrollment(@Valid @RequestBody Enrollment enrollment) {
        return enrollmentService.saveEnrollment(enrollment);
    }

    // Get All Enrollments
    @Operation(
            summary = "Get all enrollments",
            description = "Retrieves a list of all enrollments available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Student's enrollments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<Enrollment> getAllEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    // Get Enrollment By ID
    @Operation(
            summary = "Get Enrollment by ID",
            description = "Retrieves a students enrollment's complete details using the subject ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students enrollment's entry found Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Students enrollment's entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{enrollmentId}")
    public Enrollment getEnrollmentById(@PathVariable Long enrollmentId) {
        return enrollmentService.getEnrollmentById(enrollmentId);
    }

    // Update Enrollment
    @Operation(
            summary = "Update enrollment",
            description = "Updates an existing student's enrollment information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Student's enrollment updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Student's enrollment not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{enrollmentId}")
    public Enrollment updateEnrollment(@PathVariable Long enrollmentId,
                                       @Valid @RequestBody Enrollment enrollment) {
        return enrollmentService.updateEnrollment(enrollmentId, enrollment);
    }

    // Delete Enrollment
    @Operation(
            summary = "Delete Enrollment",
            description = "Deletes a student' enrollment entry using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Enrollment entry deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Enrollment entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{enrollmentId}")
    public String deleteEnrollment(@PathVariable Long enrollmentId) {
        enrollmentService.deleteEnrollment(enrollmentId);
        return "Enrollment deleted successfully!";
    }

    @Operation(
            summary = "Search enrollment details",
            description = "Searches enrollment using optional filter criteria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Entries retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public List<Enrollment> searchEnrollments(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long subjectId) {

        return enrollmentService.searchEnrollments(academicYear, semester, studentId, departmentId, subjectId);
    }
}