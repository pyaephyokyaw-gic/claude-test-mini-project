package com.studentmanagement.repository;

import com.studentmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Student entity operations.
 * Spring Data JPA handles implementation automatically.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by email
     */
    Optional<Student> findByEmail(String email);

    /**
     * Check if email already exists (for validation)
     */
    boolean existsByEmail(String email);

    /**
     * Find students by name containing (case-insensitive search)
     */
    List<Student> findByNameContainingIgnoreCase(String name);

    /**
     * Find students with grade greater than or equal to specified value
     */
    List<Student> findByGradeGreaterThanEqual(Double grade);

    /**
     * Find students with attendance below specified threshold
     */
    List<Student> findByAttendanceLessThan(Integer attendance);
}
