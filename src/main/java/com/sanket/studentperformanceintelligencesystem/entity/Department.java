package com.sanket.studentperformanceintelligencesystem.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="departments")
public class Department {
    @Id                     //primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique Identifier of the department record.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long departmentId;

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    @Schema(
            description = "Name of the department",
            example = "Information Technology"
    )
    @Column(nullable = false)
    private String departmentName;

    @NotBlank(message = "Department code cannot be empty")
    @Size(min = 2, max = 10, message = "Department code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z]+$", message = "Department code must contain only letters" )
    @Schema(
            description = "Unique code of the department",
            example = "IT"
    )
    @Column(nullable = false, unique = true)
    private String departmentCode;

    @NotBlank(message = "HOD name cannot be empty")
    @Size(min = 3, max = 100, message = "HOD name must be between 3 and 100 characters")
    @Schema(
            description = "Name of HOD of the department.",
            example = "Dr. Priya Sharma"
    )
    private String hodName;

    //Constructors
    public Department() {
    }
    public Department(Long departmentId, String departmentName, String departmentCode, String hodName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.hodName=hodName;
    }

    //Getters and Setters
    public Long getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getHodName() {
        return hodName;
    }
    public void setHodName(String hodName) {
        this.hodName = hodName;
    }

    //toString
    @Override
    public String toString() {
        return "Department{" +
                "departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", hodName='" + hodName + '\'' +
                '}';
    }

}
