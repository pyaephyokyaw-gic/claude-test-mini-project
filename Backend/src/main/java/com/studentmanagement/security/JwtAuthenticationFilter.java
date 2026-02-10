package com.studentmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that processes every request.
 * 
 * Security flow:
 * 1. Extract JWT from Authorization header
 * 2. Validate the token (signature, expiration)
 * 3. Extract username from token
 * 4. Load user details from database
 * 5. Set authentication in SecurityContext
 * 
 * This filter runs BEFORE UsernamePasswordAuthenticationFilter
 * to intercept and validate JWT tokens on all requests.
 * 
 * OncePerRequestFilter ensures this filter runs exactly once per request,
 * even in forward/include scenarios.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * Core filter logic - executed for every request.
     * 
     * Security considerations:
     * - Only sets authentication if token is valid AND user exists
     * - Clears any existing authentication on invalid token
     * - Checks SecurityContext to prevent unnecessary database calls
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract JWT from request header
            String jwt = parseJwt(request);
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Token is valid - extract user information
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                
                // Only set authentication if not already authenticated
                // This prevents unnecessary database lookups
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load user details from database to ensure user still exists and is active
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // Create authentication token with user details and authorities
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // Credentials not needed after authentication
                                    userDetails.getAuthorities()
                            );
                    
                    // Set additional details like IP address, session ID
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Set authentication in SecurityContext - 
                    // this makes the user authenticated for this request
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("User '{}' authenticated with roles: {}", 
                            username, userDetails.getAuthorities());
                }
            }
        } catch (Exception e) {
            // Log error but don't expose details to client
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Security: Clear any partial authentication on error
            SecurityContextHolder.clearContext();
        }

        // Always continue filter chain - let Spring Security handle unauthorized requests
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     * 
     * Expected format: "Bearer <token>"
     * 
     * @param request the HTTP request
     * @return the JWT token or null if not present/invalid format
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // Check if header exists and has Bearer prefix
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Extract token (everything after "Bearer ")
            return headerAuth.substring(7);
        }

        return null;
    }

    /**
     * Determine if this filter should NOT be applied.
     * Skip filter for public endpoints to improve performance.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip JWT validation for login endpoint
        return path.startsWith("/api/auth/");
    }
}
