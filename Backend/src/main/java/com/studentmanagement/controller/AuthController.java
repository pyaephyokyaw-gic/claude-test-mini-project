package com.studentmanagement.controller;

import com.studentmanagement.dto.LoginRequest;
import com.studentmanagement.dto.LoginResponse;
import com.studentmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling authentication endpoints.
 * These endpoints are public (no authentication required).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint - authenticates user and returns JWT token.
     * 
     * @param loginRequest containing username and password
     * @return LoginResponse with JWT token, expiration, and user roles
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.debug("Login attempt for user: {}", loginRequest.getUsername());
        LoginResponse response = authService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }
}
