package com.studentmanagement.controller;

import com.studentmanagement.dto.UserCreateRequest;
import com.studentmanagement.dto.UserResponse;
import com.studentmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling user management endpoints.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // Class-level authorization
public class UserController {

    private final UserService userService;

    /**
     * Get all users.
     * Accessible by: ADMIN only
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.debug("Fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get user by ID.
     * Accessible by: ADMIN only
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.debug("Fetching user with id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Create a new user.
     * Accessible by: ADMIN only
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        log.debug("Creating new user: {}", request.getUsername());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete a user.
     * Accessible by: ADMIN only
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.debug("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle user enabled/disabled status.
     * Accessible by: ADMIN only
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long id) {
        log.debug("Toggling status for user with id: {}", id);
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }
}
