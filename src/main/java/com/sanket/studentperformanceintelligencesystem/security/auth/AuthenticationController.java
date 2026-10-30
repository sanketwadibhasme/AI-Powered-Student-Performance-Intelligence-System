package com.sanket.studentperformanceintelligencesystem.security.auth;

import com.sanket.studentperformanceintelligencesystem.security.dto.LoginRequest;
import com.sanket.studentperformanceintelligencesystem.security.dto.LoginResponse;
import com.sanket.studentperformanceintelligencesystem.security.jwt.JwtService;
import com.sanket.studentperformanceintelligencesystem.security.user.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(
        name = "Authentication",
        description = "APIs for login and current-user identity."
)
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(
            summary = "Login",
            description = "Authenticates a user and returns a JWT along with the role and username."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()
                        )
                );

        String username = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        String token = jwtService.generateToken(username, role);

        return ResponseEntity.ok(new LoginResponse(token, role, username));
    }

    @Operation(
            summary = "Get current user identity",
            description = "Returns the username and role resolved from the JWT of the caller."
    )
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        Long studentId = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            var student = userDetails.getAppUser().getStudent();
            studentId = (student != null) ? student.getStudentId() : null;
        }

        // Map.of rejects null values; staff accounts have no linked student,
        // so build the map explicitly to keep this endpoint working for all roles.
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("username", username);
        body.put("role", role);
        body.put("studentId", studentId);
        return ResponseEntity.ok(body);
    }
}