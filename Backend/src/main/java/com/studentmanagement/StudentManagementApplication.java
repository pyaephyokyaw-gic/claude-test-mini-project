package com.studentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the Student Management System.
 * This application demonstrates production-grade security with:
 * - JWT-based stateless authentication
 * - Role-Based Access Control (RBAC)
 * - Spring Security 6 with lambda DSL
 */
@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }
}
