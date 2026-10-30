package com.sanket.studentperformanceintelligencesystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentPerformanceOpenAPI(){
        return new OpenAPI().info(
                new Info()
                        .title("Student Performance Intelligence System API")
                        .description("""
                                REST API documentation for the Student Performance Intelligence System.
                                
                                This application provides modules for:
                                - Student Management
                                - Department Management
                                - Faculty Management
                                - Subject Management
                                - Enrollment Management
                                - Examination Management
                                - Marks Management
                                - Attendance Management
                                - Advanced Search
                                - Analytics
                                - Reporting
                                - Dashboard
                                - Rankings
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Sanket Wadibhasme")
                                .email("sanketwadibhasme005@gmail.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")
                        )
        );
    }
}
