package com.studentmanagement.security;

import com.studentmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * 
 * This service loads user data from the database during authentication.
 * Since our User entity implements UserDetails, we can return it directly.
 * 
 * Security note:
 * - Uses @Transactional(readOnly = true) for database operations
 * - Throws UsernameNotFoundException for non-existent users
 *   (Spring Security converts this to BadCredentialsException to prevent username enumeration)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username for authentication.
     * 
     * @param username the username to look up
     * @return UserDetails containing user information and authorities
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
    }
}
