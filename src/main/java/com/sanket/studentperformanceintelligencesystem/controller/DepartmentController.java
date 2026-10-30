package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.entity.Department;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.DepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import jakarta.validation.Valid;

@Tag(
        name = "Department Management",
        description = "APIs for managing department and related functionality."
)
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    //constructor with service
    private final DepartmentService departmentService;
    public DepartmentController( DepartmentService departmentService){
        this.departmentService=departmentService;
    }

    //POST api
    @Operation(
            summary = "Create a new department.",
            description = "Creates a new department record after validating all required fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Department Created Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409",
                    description = "Duplicate data found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping                  //X  //convert json from req.-> java object
    public Department saveDepartment(@Valid @RequestBody Department department){
        return departmentService.saveDepartment(department);
    }

    //GET All
    @Operation(
            summary = "Get all Departments",
            description = "Retrieves a list of all departments available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "departments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    //Get by id
    @Operation(
            summary = "Get department by ID",
            description = "Retrieves a department's complete details using the subject ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Department's entry found Successfully.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Department's entry not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public Department getDepartmentById(@PathVariable("id") Long departmentId) {
        return departmentService.getDepartmentById(departmentId);
    }


    @Operation(
            summary = "Update department",
            description = "Updates an existing department's information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Department updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public Department updateDepartment(@PathVariable Long id, @Valid @RequestBody Department department){
        return departmentService.updateDepartment(id,department);
    }


    @Operation(
            summary = "Delete department",
            description = "Deletes a department using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Department deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Department not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return "Department deleted successfully.";
    }
}
