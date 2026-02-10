package com.studentmanagement.config;

import com.studentmanagement.entity.Role;
import com.studentmanagement.entity.Student;
import com.studentmanagement.entity.User;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Data initializer for development/testing purposes.
 * Creates default users and sample students on application startup.
 * 
 * Default credentials:
 * - Admin: admin / admin123
 * - Teacher: teacher / teacher123
 * 
 * WARNING: In production, remove this or use environment-specific profiles.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Create default admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(Set.of(Role.ROLE_ADMIN))
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();
                userRepository.save(admin);
                log.info("Default admin user created - username: admin, password: admin123");
            }

            // Create default teacher user if not exists
            if (!userRepository.existsByUsername("teacher")) {
                User teacher = User.builder()
                        .username("teacher")
                        .password(passwordEncoder.encode("teacher123"))
                        .roles(Set.of(Role.ROLE_TEACHER))
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();
                userRepository.save(teacher);
                log.info("Default teacher user created - username: teacher, password: teacher123");
            }

            // Create sample students if none exist
            if (studentRepository.count() == 0) {
                Student student1 = Student.builder()
                        .name("John Doe")
                        .email("john.doe@example.com")
                        .grade(85.5)
                        .attendance(92)
                        .build();

                Student student2 = Student.builder()
                        .name("Jane Smith")
                        .email("jane.smith@example.com")
                        .grade(92.0)
                        .attendance(98)
                        .build();

                Student student3 = Student.builder()
                        .name("Bob Johnson")
                        .email("bob.johnson@example.com")
                        .grade(78.5)
                        .attendance(65)
                        .build();

                Student student4 = Student.builder()
                        .name("Alice Williams")
                        .email("alice.williams@example.com")
                        .grade(95.0)
                        .attendance(100)
                        .build();

                Student student5 = Student.builder()
                        .name("Charlie Brown")
                        .email("charlie.brown@example.com")
                        .grade(70.0)
                        .attendance(72)
                        .build();

                studentRepository.save(student1);
                studentRepository.save(student2);
                studentRepository.save(student3);
                studentRepository.save(student4);
                studentRepository.save(student5);

                log.info("Sample students created");
            }
        };
    }
}
