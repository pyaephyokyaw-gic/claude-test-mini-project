package com.studentmanagement.service;

import com.studentmanagement.dto.UserCreateRequest;
import com.studentmanagement.dto.UserResponse;
import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.User;
import com.studentmanagement.exception.ResourceAlreadyExistsException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling user management operations.
 * All methods require ADMIN role (enforced at controller level).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    /**
     * Create a new user.
     * Password is hashed using BCrypt before storage.
     */
    public UserResponse createUser(UserCreateRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
        }

        // Parse and validate roles
        Set<Role> roles = new HashSet<>();
        for (String roleStr : request.getRoles()) {
            try {
                Role role = Role.valueOf(roleStr);
                roles.add(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + roleStr + 
                        ". Valid roles are: ROLE_ADMIN, ROLE_TEACHER");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .roles(roles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created: {} with roles: {}", savedUser.getUsername(), roles);
        
        return mapToResponse(savedUser);
    }

    /**
     * Delete a user by ID.
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }

    /**
     * Toggle user enabled status (enable/disable account).
     */
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        user.setEnabled(!user.isEnabled());
        User updatedUser = userRepository.save(user);
        
        log.info("User {} status changed to: {}", 
                user.getUsername(), user.isEnabled() ? "enabled" : "disabled");
        
        return mapToResponse(updatedUser);
    }

    /**
     * Map User entity to UserResponse DTO.
     * Note: Password is never included in response.
     */
    private UserResponse mapToResponse(User user) {
        Set<String> roleStrings = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(roleStrings)
                .enabled(user.isEnabled())
                .build();
    }
}
