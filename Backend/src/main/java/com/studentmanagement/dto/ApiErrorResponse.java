package com.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API error response format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    
    private int status;
    private String error;
    private String message;
    private String path;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
