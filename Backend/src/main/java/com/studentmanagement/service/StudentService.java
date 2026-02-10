package com.studentmanagement.service;

import com.studentmanagement.dto.StudentCreateRequest;
import com.studentmanagement.dto.StudentResponse;
import com.studentmanagement.dto.StudentUpdateRequest;
import com.studentmanagement.entity.Student;
import com.studentmanagement.exception.ResourceAlreadyExistsException;
import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling student CRUD operations.
 * 
 * Security note:
 * Authorization is handled at the controller/security config level.
 * This service focuses on business logic only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * Get all students.
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get student by ID.
     */
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return mapToResponse(student);
    }

    /**
     * Create a new student (ADMIN only via security config).
     */
    public StudentResponse createStudent(StudentCreateRequest request) {
        // Check if email already exists
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Student", "email", request.getEmail());
        }

        Student student = Student.builder()
                .name(request.getName())
                .email(request.getEmail())
                .grade(request.getGrade())
                .attendance(request.getAttendance())
                .build();

        Student savedStudent = studentRepository.save(student);
        log.info("Student created with id: {}", savedStudent.getId());
        
        return mapToResponse(savedStudent);
    }

    /**
     * Update student information (ADMIN and TEACHER via security config).
     * Teachers typically update only grade and attendance.
     */
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        // Update only provided fields
        if (request.getName() != null) {
            student.setName(request.getName());
        }
        if (request.getEmail() != null) {
            // Check if new email is already taken by another student
            if (!student.getEmail().equals(request.getEmail()) 
                    && studentRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Student", "email", request.getEmail());
            }
            student.setEmail(request.getEmail());
        }
        if (request.getGrade() != null) {
            student.setGrade(request.getGrade());
        }
        if (request.getAttendance() != null) {
            student.setAttendance(request.getAttendance());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Student updated with id: {}", updatedStudent.getId());
        
        return mapToResponse(updatedStudent);
    }

    /**
     * Delete a student (ADMIN only via security config).
     */
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", "id", id);
        }
        
        studentRepository.deleteById(id);
        log.info("Student deleted with id: {}", id);
    }

    /**
     * Search students by name.
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> searchStudentsByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get students with low attendance.
     */
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsWithLowAttendance(Integer threshold) {
        return studentRepository.findByAttendanceLessThan(threshold).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Student entity to StudentResponse DTO.
     */
    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .grade(student.getGrade())
                .attendance(student.getAttendance())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
