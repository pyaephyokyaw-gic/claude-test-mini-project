package com.studentmanagement.entity;

/**
 * Enum defining the available roles in the system.
 * ROLE_ prefix is required by Spring Security for hasRole() checks.
 * 
 * ROLE_ADMIN: Full access to all CRUD operations on students and users
 * ROLE_TEACHER: Read students + Update grades/attendance only (no delete)
 */
public enum Role {
    ROLE_ADMIN,
    ROLE_TEACHER
}
