package com.studentmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Student entity representing a student record in the system.
 * 
 * Security note:
 * - ADMIN can perform all CRUD operations
 * - TEACHER can only read and update (grade/attendance)
 * - Delete operations are restricted to ADMIN only
 */
@Entity
@Table(name = "students",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotNull(message = "Grade is required")
    @DecimalMin(value = "0.0", message = "Grade must be at least 0.0")
    @DecimalMax(value = "100.0", message = "Grade must not exceed 100.0")
    @Column(nullable = false)
    private Double grade;

    @NotNull(message = "Attendance is required")
    @Min(value = 0, message = "Attendance must be at least 0")
    @Max(value = 100, message = "Attendance must not exceed 100")
    @Column(nullable = false)
    private Integer attendance; // Percentage (0-100)

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Automatically set createdAt before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Automatically update updatedAt before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
