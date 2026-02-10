package com.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for login response containing JWT and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String accessToken;
    private String tokenType;
    private long expiresIn; // Expiration time in milliseconds
    private String username;
    private List<String> roles;
}
