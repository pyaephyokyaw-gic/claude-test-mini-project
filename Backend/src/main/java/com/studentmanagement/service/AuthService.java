package com.studentmanagement.service;

import com.studentmanagement.dto.LoginRequest;
import com.studentmanagement.dto.LoginResponse;
import com.studentmanagement.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param loginRequest containing username and password
     * @return LoginResponse with JWT token and user info
     */
    public LoginResponse authenticate(LoginRequest loginRequest) {
        // Authenticate using Spring Security's AuthenticationManager
        // This will throw BadCredentialsException if authentication fails
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Extract user details for response
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("User '{}' logged in successfully with roles: {}", 
                userDetails.getUsername(), roles);

        return LoginResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationMs())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }
}
