package com.sanket.studentperformanceintelligencesystem.controller;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.enums.Gender;
import com.sanket.studentperformanceintelligencesystem.enums.StudentStatus;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.StudentService;

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
        name = "Student Management",
        description = "APIs for managing students including CRUD operations and student-related functionality."
)
@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;
    public StudentController( StudentService studentService){
        this.studentService = studentService;
    }

    @Operation(
            summary = "Create a new student",
            description = "Creates a new student record after validating all required fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Student created successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409",
                    description = "Duplicate student data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Student saveStudent(@Valid @RequestBody Student student){
        return studentService.saveStudent(student);
    }

    @Operation(
            summary = "Get all students",
            description = "Retrieves a list of all students available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    //adding swagger ui
    @Operation(
            summary = "Get student by ID",
            description = "Retrieves a student's complete details using the student ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Student found successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @Operation(
            summary = "Update student",
            description = "Updates an existing student's information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Student updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @Valid @RequestBody Student student){
        return studentService.updateStudent(id,student);
    }

    @Operation(
            summary = "Delete student",
            description = "Deletes a student using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Student deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "Student deleted successfully.";
    }

    @Operation(
            summary = "Get all students (paginated)",
            description = "Retrieves students with pagination and sorting. Example: /api/students/page?page=0&size=20&sort=firstName,asc"
    )
    @GetMapping("/page")
    public Page<Student> getAllStudentsPage(Pageable pageable) {
        return studentService.getAllStudents(pageable);
    }

    @Operation(
            summary = "Search students",
            description = "Searches students using one or more optional filter criteria such as name, department, semester, admission year, status, or gender."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Students retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search criteria",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public List<Student> searchStudents(
            @RequestParam(required = false) String rollNumber,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer admissionYear,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) StudentStatus status) {

        return studentService.searchStudents(rollNumber, firstName, lastName, departmentId, semester, admissionYear, gender, status);
    }
}
