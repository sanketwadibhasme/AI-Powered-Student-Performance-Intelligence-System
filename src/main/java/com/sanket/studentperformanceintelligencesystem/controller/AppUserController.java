package com.sanket.studentperformanceintelligencesystem.controller;

import com.sanket.studentperformanceintelligencesystem.entity.AppUser;
import com.sanket.studentperformanceintelligencesystem.exception.ApiErrorResponse;
import com.sanket.studentperformanceintelligencesystem.service.AppUserService;
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
        name = "User Management",
        description = "APIs for managing users including CRUD operations and user-related functionality."
)
@RestController
@RequestMapping("/api/users")
public class AppUserController {
    private final AppUserService appUserService;
    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Operation(
            summary = "Create a new user",
            description = "Creates a new user record after validating all required fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409",
                    description = "Duplicate User data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public AppUser saveUser(@Valid @RequestBody AppUser appUser){
        return appUserService.saveUser(appUser);
    }

    @Operation(
            summary = "Get all Users",
            description = "Retrieves a list of all users available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<AppUser> getAllUsers(){
        return appUserService.getAllUsers();
    }

    @Operation(
            summary = "Get all Users (paginated)",
            description = "Retrieves users with pagination and sorting. Example: /api/users/page?page=0&size=20&sort=username,asc"
    )
    @GetMapping("/page")
    public Page<AppUser> getAllUsersPage(Pageable pageable) {
        return appUserService.getAllUsers(pageable);
    }

    @Operation(
            summary = "Get User by ID",
            description = "Retrieves a User's complete details using the user ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "User found successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public AppUser getUserById(@PathVariable Long id){
        return appUserService.getUserById(id);
    }

    @Operation(
            summary = "Get user by username",
            description = "Retrieves a user's complete details using the username."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "user found successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/username/{username}")
    public AppUser getUserByUsername(@PathVariable String username){
        return appUserService.getUserByUsername(username);
    }

    @Operation(
            summary = "Get user by email",
            description = "Retrieves a user's complete details using the user's email'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "user found successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/email/{email}")
    public AppUser getUserByEmail(@PathVariable String email){
        return appUserService.getUserByEmail(email);
    }

    @Operation(
            summary = "Update user",
            description = "Updates an existing user's information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public AppUser updateUser(@PathVariable Long id, @Valid @RequestBody AppUser appUser){
        return appUserService.updateUser(id, appUser);
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes a user using the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "User deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "user not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        appUserService.deleteUser(id);
    }
}
