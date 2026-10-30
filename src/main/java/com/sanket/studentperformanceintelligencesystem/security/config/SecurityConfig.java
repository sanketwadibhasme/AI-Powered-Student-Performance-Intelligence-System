package com.sanket.studentperformanceintelligencesystem.security.config;

import com.sanket.studentperformanceintelligencesystem.security.filter.JwtAuthenticationFilter;
import com.sanket.studentperformanceintelligencesystem.security.handler.RestAccessDeniedHandler;
import com.sanket.studentperformanceintelligencesystem.security.handler.RestAuthenticationEntryPoint;
import com.sanket.studentperformanceintelligencesystem.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        // ---- public endpoints ----
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // ---- user management: ADMIN only ----
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // ---- student self-service (own data only) ----
                        .requestMatchers(HttpMethod.GET, "/api/students/me/**").hasRole("STUDENT")
                        .requestMatchers("/api/ai/me/**").hasRole("STUDENT")

                        // ---- AI health probe: any authenticated role (status flag only,
                        //      no academic data) so the student pill reflects the real
                        //      ML service state instead of a permanent 403 "Offline" ----
                        .requestMatchers(HttpMethod.GET, "/api/ai/health").hasAnyRole("ADMIN", "FACULTY", "STUDENT")

                        // ---- AI dashboard: ADMIN & FACULTY ----
                        .requestMatchers("/api/ai/**").hasAnyRole("ADMIN", "FACULTY")

                        // ---- academic data: STUDENT can read general info, writes for ADMIN/FACULTY ----
                        .requestMatchers(HttpMethod.GET,
                                "/api/departments/**",
                                "/api/subjects/**",
                                "/api/exams/**").hasAnyRole("ADMIN", "FACULTY", "STUDENT")
                        .requestMatchers(
                                "/api/departments/**",
                                "/api/subjects/**",
                                "/api/exams/**",
                                "/api/enrollments/**").hasAnyRole("ADMIN", "FACULTY")

                        // ---- student / faculty / marks / attendance records: ADMIN & FACULTY ----
                        .requestMatchers(
                                "/api/students/**",
                                "/api/faculties/**",
                                "/api/marks/**",
                                "/api/attendance/**").hasAnyRole("ADMIN", "FACULTY")

                        // ---- dashboards / analytics: ADMIN & FACULTY only (students use /api/students/me/**) ----
                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/analytics/**").hasAnyRole("ADMIN", "FACULTY")

                        // ---- rankings/reports: visible to all authenticated roles ----
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "FACULTY", "STUDENT")

                        // ---- everything else requires authentication ----
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
