package com.studentmanagement.repository;

import com.studentmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity operations.
 * Spring Data JPA handles implementation automatically.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (used for authentication)
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username already exists (for registration validation)
     */
    boolean existsByUsername(String username);
}
