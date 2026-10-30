package com.sanket.studentperformanceintelligencesystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanket.studentperformanceintelligencesystem.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required.")
    @Column(nullable = false, unique = true)
    private String username;

    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email is required.")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * Linked student record. Required when role = STUDENT so the student
     * can access their own academic data. Null for ADMIN/FACULTY users.
     */
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true)
    private Student student;

    /**
     * Transient convenience field for the admin User Management UI.
     * Maps to the linked {@link Student}; persistence state lives in the
     * {@code student} column mapping above, so no schema change is needed.
     */
    @Transient
    private Long studentId;

    /**
     * Transient faculty resolution for the faculty self-service endpoints.
     * The app_users table has no faculty_id column; a FACULTY account is
     * matched to its faculty record by the unique email column. Resolved
     * by the service layer (CustomUserDetailsService) at authentication time.
     */
    @Transient
    private Long facultyId;


    public AppUser() {
    }
    public AppUser(String username, String email, String password, Role role, Boolean enabled){
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * Serialized as the id of the linked student (or the transient value when
     * no link exists yet); deserialization populates the transient field so
     * the service layer can resolve/apply the link.
     */
    public Long getStudentId() {
        if (student != null) {
            return student.getStudentId();
        }
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}
