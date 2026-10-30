package com.sanket.studentperformanceintelligencesystem.entity;
import com.sanket.studentperformanceintelligencesystem.enums.Gender;
import com.sanket.studentperformanceintelligencesystem.enums.StudentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the student",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long studentId;

    @NotBlank(message="Requires Roll number")
    @Schema(
            description = "University roll number of the student",
            example = "CS23001"
    )
    @Column(unique = true,nullable = false)
    private String rollNumber;

    @NotBlank(message = "Requires First name")
    @Size( min = 2, max = 20, message = "must be between 2 and 20 characters")
    @Schema(
            description = "Student's first name",
            example = "Sanket"
    )
    private String firstName;

    @NotBlank(message="Requires Last Name")
    @Size( min = 2, max = 20, message = "must be between 2 and 20 characters")
    @Schema(
            description = "Student's last name",
            example = "Wadibhasme"
    )
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message="Invalid email format")
    @Schema(
            description = "Student's email address",
            example = "sanket.W@example.com"
    )
    private String email;

    @NotBlank(message= "Requires Phone number.")
    @Pattern(regexp = "^[0-9]{10}$", message = "10 digits phone number required")
    @Schema(
            description = "10-digit mobile number",
            example = "9876543210"
    )
    private String phone;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Gender is required")
    @Schema(
            description = "Gender of the student",
            example = "MALE"
    )
    private Gender gender;

    @NotNull(message = "DoB is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(
            description = "Student's date of birth",
            example = "2005-01-13"
    )
    private LocalDate dob;

    @NotNull(message = "Admission year is required")
    @Schema(
            description = "Year in which the student was admitted",
            example = "2023"
    )
    private Integer admissionYear;

    @NotNull(message = "Current semester is required")
    @Min(value = 1)
    @Max(value = 8)
    @Schema(
            description = "Current semester of the student",
            example = "7",
            minimum = "1",
            maximum = "8"
    )
    private Integer currentSemester;

    @NotNull(message = "Student status is required")
    @Enumerated(EnumType.STRING)
    @Schema(
            description = "Current academic status of the student",
            example = "ACTIVE"
    )
    private StudentStatus status;

    @NotNull(message = "Department is required")
    @Schema(
            description = "Department to which the student belongs"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    //constructor ->getter and setters ->tostring (alt +ins)

    public Student() {
    }
    public Student(String rollNumber,
                   String firstName,
                   String lastName,
                   String email,
                   String phone,
                   Gender gender,
                   LocalDate dob,
                   Integer admissionYear,
                   Integer currentSemester,
                   StudentStatus status,
                   Department department) {

        this.rollNumber = rollNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.dob = dob;
        this.admissionYear = admissionYear;
        this.currentSemester = currentSemester;
        this.status = status;
        this.department = department;
    }

    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Integer getAdmissionYear() {
        return admissionYear;
    }
    public void setAdmissionYear(Integer admissionYear) {
        this.admissionYear = admissionYear;
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }
    public void setCurrentSemester(Integer currentSemester) {
        this.currentSemester = currentSemester;
    }

    public StudentStatus getStatus() {
        return status;
    }
    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", rollNumber='" + rollNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender=" + gender +
                ", dob=" + dob +
                ", admissionYear=" + admissionYear +
                ", currentSemester=" + currentSemester +
                ", status=" + status +
                ", department=" + department +
                '}';
    }

    //----

}