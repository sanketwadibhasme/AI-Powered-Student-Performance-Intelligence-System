package com.sanket.studentperformanceintelligencesystem.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "faculties")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the faculty member",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long facultyId;

    @NotBlank(message = "Faculty name is required")
    @Size(min = 2, max = 50, message = "Faculty name must be between 2 and 50 characters")
    @Schema(
            description = "Full name of the faculty member",
            example = "Dr. Priya Sharma"
    )
    @Column(nullable = false)
    private String facultyName;

    @NotBlank(message = "Employee code is required")
    @Schema(
            description = "Unique employee code assigned to the faculty member",
            example = "EMP1001"
    )
    @Column(nullable = false, unique = true)
    private String employeeCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(
            description = "Official email address of the faculty member",
            example = "priya.sharma@college.edu"
    )
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message= "Requires Phone number.")
    @Pattern(regexp = "^[0-9]{10}$", message = "10 digits phone number required")
    @Schema(
            description = "10-digit contact number of the faculty member",
            example = "9876543210"
    )
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "Designation is required")
    @Size(min = 2, max = 50, message = "Designation must be between 2 and 50 characters")
    @Schema(
            description = "Designation of the faculty member",
            example = "Assistant Professor"
    )
    @Column(nullable = false)
    private String designation;

    @NotNull(message = "Department is required")
    @Schema(
            description = "Department to which the faculty member belongs"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    // Default Constructor
    public Faculty() {
    }

    // Parameterized Constructor
    public Faculty(Long facultyId,
                   String facultyName,
                   String employeeCode,
                   String email,
                   String phone,
                   String designation,
                   Department department) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.employeeCode = employeeCode;
        this.email = email;
        this.phone = phone;
        this.designation = designation;
        this.department = department;
    }

    // Getters and Setters

    public Long getFacultyId() {
        return facultyId;
    }
    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }
    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDesignation() {
        return designation;
    }
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Faculty{" +
                "facultyId=" + facultyId +
                ", facultyName='" + facultyName + '\'' +
                ", employeeCode='" + employeeCode + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", designation='" + designation + '\'' +
                ", departmentId=" + (department != null ? department.getDepartmentId() : null) +
                '}';
    }
}