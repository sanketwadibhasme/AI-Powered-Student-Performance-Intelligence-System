package com.sanket.studentperformanceintelligencesystem.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.sanket.studentperformanceintelligencesystem.enums.AttendanceStatus;
import com.sanket.studentperformanceintelligencesystem.enums.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the attendance record",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long attendanceId;

    @NotNull(message = "Attendance date is required")
    @PastOrPresent(message = "Attendance date cannot be in the future")
    @Schema(
            description = "Date on which the attendance was recorded",
            example = "2026-07-18"
    )
    @Column(nullable = false)
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    @Schema(
            description = "Attendance status of the student",
            example = "PRESENT"
    )
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @NotNull(message = "Student is required")
    @Schema(
            description = "Student whose attendance is being recorded"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @NotNull(message = "Subject is required")
    @Schema(
            description = "Subject for which attendance is recorded"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Subject subject;

    public Attendance() {
    }
    public Attendance(LocalDate attendanceDate, AttendanceStatus status, Student student, Subject subject) {
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.student = student;
        this.subject = subject;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }
    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }
    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }
    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId=" + attendanceId +
                ", attendanceDate=" + attendanceDate +
                ", status='" + status + '\'' +
                ", student=" + student +
                ", subject=" + subject +
                '}';
    }
}