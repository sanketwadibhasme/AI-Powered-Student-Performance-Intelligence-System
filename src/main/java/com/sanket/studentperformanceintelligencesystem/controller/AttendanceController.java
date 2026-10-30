package com.sanket.studentperformanceintelligencesystem.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.sanket.studentperformanceintelligencesystem.enums.AttendanceStatus;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.specification.AttendanceSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.sanket.studentperformanceintelligencesystem.entity.Attendance;
import com.sanket.studentperformanceintelligencesystem.service.AttendanceService;

@Tag(
        name = "Attendance Management",
        description = "APIs for managing attendance including CRUD operations and related functionality."
)
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }


    @Operation(
            summary = "Create a new Attendance entry.",
            description = "Creates a new attendance record after validating all required fields."
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
    public Attendance saveAttendance(@Valid @RequestBody Attendance attendance) {
        return attendanceService.saveAttendance(attendance);
    }

    /**
     * Saves a whole attendance session (one subject, one date, many students)
     * in a single transaction. Students who already have a record for that
     * subject/date are skipped rather than duplicated; the response reports
     * how many were saved and skipped so the UI can inform the faculty.
     */
    @Operation(
            summary = "Save a full attendance session",
            description = "Creates attendance records for many students at once (one subject + date). Duplicate rows for the same student/subject/date are skipped, not re-created."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session saved (see saved/skipped counts)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/bulk")
    public Map<String, Object> saveBulkAttendance(@Valid @RequestBody List<Attendance> records) {
        AttendanceService.BulkSaveResult result = attendanceService.saveBulkAttendance(records);
        return Map.of(
                "saved", result.saved(),
                "skipped", result.skipped(),
                "skippedStudentIds", result.skippedStudentIds());
    }


    @Operation(
            summary = "Get all Attendance entries",
            description = "Retrieves a list of all student' attendance entries available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Attendance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }


    @Operation(
            summary = "Get all attendance entries (paginated)",
            description = "Retrieves attendance entries with pagination and sorting. Example: /api/attendance/page?page=0&size=20&sort=attendanceDate,desc"
    )
    @GetMapping("/page")
    public Page<Attendance> getAllAttendancePage(Pageable pageable) {
        return attendanceService.getAllAttendance(pageable);
    }


    @Operation(
            summary = "Get attendance by ID",
            description = "Retrieves a students attendance's complete details using the subject ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Entry found Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{attendanceId}")
    public Attendance getAttendanceById(@PathVariable Long attendanceId) {
        return attendanceService.getAttendanceById(attendanceId);
    }


    @Operation(
            summary = "Update attendance",
            description = "Updates an existing student's attendance information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Attendance updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Attendance not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{attendanceId}")
    public Attendance updateAttendance(@PathVariable Long attendanceId,
                                       @Valid @RequestBody Attendance attendance) {
        return attendanceService.updateAttendance(attendanceId, attendance);
    }


    @Operation(
            summary = "Delete Attendance",
            description = "Deletes a student's attendance entry using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Entry deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{attendanceId}")
    public String deleteAttendance(@PathVariable Long attendanceId) {
        attendanceService.deleteAttendance(attendanceId);
        return "Attendance deleted successfully.";
    }


    @Operation(
            summary = "Search by attendance",
            description = "Searches details using optional filter criteria."
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
    public List<Attendance> searchAttendance(@RequestParam(required = false) Long studentId,
                                             @RequestParam(required = false) Long subjectId,
                                             @RequestParam(required = false) AttendanceStatus status,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             @RequestParam(required = false) Long departmentId){

        return attendanceService.searchAttendance(studentId, subjectId, status, startDate, endDate, departmentId);
    }
}