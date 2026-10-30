package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.entity.Faculty;
import com.sanket.studentperformanceintelligencesystem.entity.Subject;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.repository.SubjectRepository;
import com.sanket.studentperformanceintelligencesystem.security.user.CustomUserDetails;
import com.sanket.studentperformanceintelligencesystem.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Faculty Management",
        description = "APIs for managing faculties and related operations."
)
@RestController
@RequestMapping("/api/faculties")
public class FacultyController {

    private final FacultyService facultyService;
    private final SubjectRepository subjectRepository;

    public FacultyController(FacultyService facultyService, SubjectRepository subjectRepository) {
        this.facultyService = facultyService;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Subjects taught by the currently authenticated faculty member.
     * Resolved from the JWT (AppUser → linked Faculty record). This powers
     * the subject-first faculty workflow: pick a subject → take attendance /
     * enter marks for its enrolled class.
     */
    @Operation(
            summary = "My subjects (faculty self-service)",
            description = "Returns the subjects assigned to the currently authenticated faculty member, resolved from the JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subjects retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No faculty record is linked to this account")
    })
    @GetMapping("/me/subjects")
    public List<Subject> getMySubjects(@AuthenticationPrincipal CustomUserDetails principal) {
        Long facultyId = principal.getAppUser().getFacultyId();
        if (facultyId == null) {
            throw new com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException(
                    "No faculty record is linked to this account. Contact the administrator.");
        }
        return subjectRepository.findByFacultyFacultyId(facultyId);
    }


    @Operation(
            summary = "Create a new Faculty.",
            description = "Creates a new faculty record after validating all required fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Faculty Created Successfully.",
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
    public Faculty saveFaculty(@Valid @RequestBody Faculty faculty) {
        return facultyService.saveFaculty(faculty);
    }


    @Operation(
            summary = "Get all faculties",
            description = "Retrieves a list of all faculties available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Faculties retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<Faculty> getAllFaculties() {
        return facultyService.getAllFaculties();
    }


    @Operation(
            summary = "Get faculty by ID",
            description = "Retrieves a faculty's complete details using the subject ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Faculty found Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Faculty not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{facultyId}")
    public Faculty getFacultyById(@PathVariable Long facultyId) {
        return facultyService.getFacultyById(facultyId);
    }


    @Operation(
            summary = "Update faculty",
            description = "Updates an existing faculty's information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Faculty updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Faculty not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{facultyId}")
    public Faculty updateFaculty(@PathVariable Long facultyId,
                                 @Valid @RequestBody Faculty faculty) {
        return facultyService.updateFaculty(facultyId, faculty);
    }


    @Operation(
            summary = "Delete Faculty",
            description = "Deletes a faculty using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Faculty deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Faculty not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{facultyId}")
    public void deleteFaculty(@PathVariable Long facultyId) {
        facultyService.deleteFaculty(facultyId);
    }


    @Operation(
            summary = "Search faculties",
            description = "Searches faculties using optional filter criteria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Faculties retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public List<Faculty> searchFaculties(
            @RequestParam(required = false) String facultyName,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String email) {

        return facultyService.searchFaculties(facultyName, employeeCode, departmentId, designation, email);
    }
}