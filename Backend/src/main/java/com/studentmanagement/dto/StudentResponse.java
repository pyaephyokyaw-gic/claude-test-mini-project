package com.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for student response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    
    private Long id;
    private String name;
    private String email;
    private Double grade;
    private Integer attendance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
