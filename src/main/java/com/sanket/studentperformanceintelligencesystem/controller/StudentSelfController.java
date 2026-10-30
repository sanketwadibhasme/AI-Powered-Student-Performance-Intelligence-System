package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.entity.Attendance;
import com.sanket.studentperformanceintelligencesystem.entity.Enrollment;
import com.sanket.studentperformanceintelligencesystem.entity.Marks;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.security.user.CustomUserDetails;
import com.sanket.studentperformanceintelligencesystem.service.AnalyticsService;
import com.sanket.studentperformanceintelligencesystem.service.AttendanceService;
import com.sanket.studentperformanceintelligencesystem.service.EnrollmentService;
import com.sanket.studentperformanceintelligencesystem.service.MarksService;
import com.sanket.studentperformanceintelligencesystem.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Student self-service endpoints. The studentId is ALWAYS resolved from the
 * authenticated principal (JWT), never from a request parameter, so a student
 * cannot read another student's data by changing an ID.
 */
@Tag(
        name = "My Academic Data (Student Self-Service)",
        description = "Endpoints for a logged-in student to view their own academic information, marks, attendance and performance."
)
@RestController
@RequestMapping("/api/students/me")
public class StudentSelfController {

    private final MarksService marksService;
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final AnalyticsService analyticsService;
    private final EnrollmentService enrollmentService;

    public StudentSelfController(
            MarksService marksService,
            AttendanceService attendanceService,
            StudentService studentService,
            AnalyticsService analyticsService,
            EnrollmentService enrollmentService) {
        this.marksService = marksService;
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.analyticsService = analyticsService;
        this.enrollmentService = enrollmentService;
    }

    private Student getCurrentStudent(CustomUserDetails principal) {
        if (principal.getAppUser().getStudent() == null) {
            throw new ResourceNotFoundException(
                    "No student record is linked to this account. Contact the administrator.");
        }
        Long studentId = principal.getAppUser().getStudent().getStudentId();
        return studentService.getStudentById(studentId);
    }

    @Operation(
            summary = "Get my profile",
            description = "Returns the profile of the currently authenticated student."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "No student record linked to this account",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/profile")
    public Student getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return getCurrentStudent(principal);
    }

    @Operation(
            summary = "Get my marks",
            description = "Returns all marks records belonging to the currently authenticated student."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Marks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/marks")
    public List<Marks> getMyMarks(@AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = getCurrentStudent(principal).getStudentId();
        return marksService.searchMarks(studentId, null, null, null, null, null);
    }

    @Operation(
            summary = "Get my attendance",
            description = "Returns all attendance records belonging to the currently authenticated student."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Attendance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/attendance")
    public List<Attendance> getMyAttendance(@AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = getCurrentStudent(principal).getStudentId();
        return attendanceService.searchAttendance(studentId, null, null, null, null, null);
    }

    @Operation(
            summary = "Get my average marks",
            description = "Returns the average marks of the currently authenticated student."
    )
    @GetMapping("/average-marks")
    public Double getMyAverageMarks(@AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = getCurrentStudent(principal).getStudentId();
        return analyticsService.calculateAverageMarksByStudentId(studentId);
    }

    @Operation(
            summary = "Get my attendance percentage",
            description = "Returns the attendance percentage of the currently authenticated student."
    )
    @GetMapping("/attendance-percentage")
    public Double getMyAttendancePercentage(@AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = getCurrentStudent(principal).getStudentId();
        return analyticsService.calculateAttendancePercentageByStudentId(studentId);
    }

    @Operation(
            summary = "Get my enrollments",
            description = "Returns the subject enrollments of the currently authenticated student."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Enrollments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/enrollments")
    public List<Enrollment> getMyEnrollments(@AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = getCurrentStudent(principal).getStudentId();
        return enrollmentService.searchEnrollments(null, null, studentId, null, null);
    }
}
