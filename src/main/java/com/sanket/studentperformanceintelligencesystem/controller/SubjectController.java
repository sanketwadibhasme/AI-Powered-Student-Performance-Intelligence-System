package com.sanket.studentperformanceintelligencesystem.controller;
import com.sanket.studentperformanceintelligencesystem.entity.Subject;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.SubjectService;

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
        name = "Subject Management",
        description = "APIs for managing subjects including CRUD operations and student-related functionality."
)
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectService subjectService;
    public SubjectController(SubjectService subjectService){
        this.subjectService = subjectService;
    }


    @Operation(
            summary = "Create a new subject.",
            description = "Creates a new subject record after validating all required fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Subject Created Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409",
                    description = "Duplicate subject data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Subject saveSubject(@Valid @RequestBody Subject subject){
        return subjectService.saveSubject(subject);
    }


    // Get All Subjects
    @Operation(
            summary = "Get all subjects",
            description = "Retrieves a list of all subjects available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Subjects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectService.getAllSubjects();
    }


    // Get Subject By ID
    @Operation(
            summary = "Get subject by ID",
            description = "Retrieves a subject's complete details using the subject ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Subject found Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{subjectId}")
    public Subject getSubjectById(@PathVariable Long subjectId) {
        return subjectService.getSubjectById(subjectId);
    }


    // Update Subject
    @Operation(
            summary = "Update subject",
            description = "Updates an existing subject's information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Subject updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })    @PutMapping("/{subjectId}")
    public Subject updateSubject(@PathVariable Long subjectId,
                                 @Valid @RequestBody Subject subject) {
        return subjectService.updateSubject(subjectId, subject);
    }


    // Delete Subject
    @Operation(
            summary = "Delete subject",
            description = "Deletes a subject using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Subject deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Subject not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{subjectId}")
    public void deleteSubject(@PathVariable Long subjectId) {
        subjectService.deleteSubject(subjectId);
    }


    @Operation(
            summary = "Search subjects",
            description = "Searches subjects using optional filter criteria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Subjects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public List<Subject> searchSubjects(
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Integer credits) {

        return subjectService.searchSubjects(subjectName, subjectCode, departmentId, facultyId, credits);
    }
}
