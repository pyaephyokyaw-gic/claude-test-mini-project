package com.studentmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new student.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Grade is required")
    @DecimalMin(value = "0.0", message = "Grade must be at least 0.0")
    @DecimalMax(value = "100.0", message = "Grade must not exceed 100.0")
    private Double grade;

    @NotNull(message = "Attendance is required")
    @Min(value = 0, message = "Attendance must be at least 0")
    @Max(value = 100, message = "Attendance must not exceed 100")
    private Integer attendance;
}
