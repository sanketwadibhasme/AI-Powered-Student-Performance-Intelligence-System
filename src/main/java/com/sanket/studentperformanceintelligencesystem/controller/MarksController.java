package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.entity.Marks;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.MarksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Marks management",
        description = "APIs for  managing marks including CRUD operations and marks related functionalities."
)
@RestController
@RequestMapping("/api/marks")
public class MarksController {

    private final MarksService marksService;
    public MarksController(MarksService marksService) {
        this.marksService = marksService;
    }

    @Operation(
            summary = "Create a new Marks entry.",
            description = "Creates a new marks record after validating all required fields."
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
    public Marks saveMarks(@Valid @RequestBody Marks marks) {
        return marksService.saveMarks(marks);
    }


    @Operation(
            summary = "Get all marks",
            description = "Retrieves a list of all students's marks available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students's marks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<Marks> getAllMarks() {
        return marksService.getAllMarks();
    }


    @Operation(
            summary = "Get all marks (paginated)",
            description = "Retrieves marks records with pagination and sorting. Example: /api/marks/page?page=0&size=20&sort=marksObtained,desc"
    )
    @GetMapping("/page")
    public Page<Marks> getAllMarksPage(Pageable pageable) {
        return marksService.getAllMarks(pageable);
    }


    @Operation(
            summary = "Get marks by ID",
            description = "Retrieves a students mark's complete details using the subject ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students mark's entry found Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Students mark's entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{marksId}")
    public Marks getMarksById(@PathVariable Long marksId) {
        return marksService.getMarksById(marksId);
    }


    @Operation(
            summary = "Update marks",
            description = "Updates an existing student's marks information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Marks entry updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Marks entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{marksId}")
    public Marks updateMarks(@PathVariable Long marksId,
                             @Valid @RequestBody Marks marks) {
        return marksService.updateMarks(marksId, marks);
    }


    @Operation(
            summary = "Delete marks entry",
            description = "Deletes a student's marks entry using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Entry deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{marksId}")
    public void deleteMarks(@PathVariable Long marksId) {
        marksService.deleteMarks(marksId);
    }


    @Operation(
            summary = "Search by marks",
            description = "Searches marks using optional filter criteria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students with marks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))

    })
    @GetMapping("/search")
    public List<Marks> searchMarks(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Double minMarks,
            @RequestParam(required = false) Double maxMarks,
            @RequestParam(required = false) Long departmentId) {

        return marksService.searchMarks(studentId, subjectId, examId, minMarks, maxMarks, departmentId);
    }
}