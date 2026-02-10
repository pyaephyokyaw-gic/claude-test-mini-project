package com.studentmanagement.controller;

import com.studentmanagement.dto.StudentCreateRequest;
import com.studentmanagement.dto.StudentResponse;
import com.studentmanagement.dto.StudentUpdateRequest;
import com.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling student management endpoints.
 * 
 * RBAC Rules (enforced by both SecurityConfig and @PreAuthorize):
 * - GET: ADMIN, TEACHER
 * - POST: ADMIN only
 * - PUT: ADMIN, TEACHER
 * - DELETE: ADMIN only
 * 
 * Note: @PreAuthorize provides method-level security as an additional layer.
 * The primary authorization is in SecurityConfig.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    /**
     * Get all students.
     * Accessible by: ADMIN, TEACHER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        log.debug("Fetching all students");
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    /**
     * Get student by ID.
     * Accessible by: ADMIN, TEACHER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        log.debug("Fetching student with id: {}", id);
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    /**
     * Create a new student.
     * Accessible by: ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody StudentCreateRequest request) {
        log.debug("Creating new student: {}", request.getName());
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a student.
     * Accessible by: ADMIN, TEACHER
     * Note: Teachers typically update grades and attendance only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request) {
        log.debug("Updating student with id: {}", id);
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    /**
     * Delete a student.
     * Accessible by: ADMIN only
     * 
     * SECURITY: Teachers are explicitly forbidden from deleting students.
     * This is enforced both here and in SecurityConfig.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        log.debug("Deleting student with id: {}", id);
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search students by name.
     * Accessible by: ADMIN, TEACHER
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<StudentResponse>> searchStudents(
            @RequestParam String name) {
        log.debug("Searching students by name: {}", name);
        return ResponseEntity.ok(studentService.searchStudentsByName(name));
    }

    /**
     * Get students with low attendance.
     * Accessible by: ADMIN, TEACHER
     */
    @GetMapping("/low-attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<StudentResponse>> getStudentsWithLowAttendance(
            @RequestParam(defaultValue = "75") Integer threshold) {
        log.debug("Fetching students with attendance below: {}%", threshold);
        return ResponseEntity.ok(studentService.getStudentsWithLowAttendance(threshold));
    }
}
