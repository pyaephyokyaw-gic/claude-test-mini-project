package com.studentmanagement.config;

import com.studentmanagement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6 Configuration using lambda DSL.
 * 
 * Key Security Decisions:
 * 1. STATELESS session management - no server-side sessions (JWT-based)
 * 2. CSRF disabled - not needed for stateless JWT authentication
 * 3. JWT filter placed BEFORE UsernamePasswordAuthenticationFilter
 * 4. BCrypt for password hashing (industry standard)
 * 5. CORS configured for frontend at localhost:3000
 * 
 * RBAC Rules Implemented:
 * - /api/auth/** : Public (login)
 * - /api/students/** GET : ADMIN, TEACHER
 * - /api/students/** PUT : ADMIN, TEACHER (for grades/attendance updates)
 * - /api/students/** DELETE : ADMIN only
 * - /api/students/** POST : ADMIN only
 * - /api/users/** : ADMIN only
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize annotations
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Main security filter chain configuration.
     * Uses Spring Security 6 lambda DSL syntax (NO WebSecurityConfigurerAdapter).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS configuration - allow frontend requests
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Disable CSRF - not needed for stateless JWT auth
                // CSRF protection is for session-based auth to prevent cross-site attacks
                .csrf(AbstractHttpConfigurer::disable)
                
                // Session management - STATELESS for JWT
                // Server will not create or use HTTP sessions
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Authorization rules using lambda DSL
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // Health check endpoint (optional, for monitoring)
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // STUDENT ENDPOINTS - RBAC enforcement
                        // DELETE: ADMIN only (teachers cannot delete students)
                        .requestMatchers(HttpMethod.DELETE, "/api/students/**")
                                .hasRole("ADMIN")
                        
                        // POST (Create): ADMIN only
                        .requestMatchers(HttpMethod.POST, "/api/students/**")
                                .hasRole("ADMIN")
                        
                        // GET (Read): Both ADMIN and TEACHER
                        .requestMatchers(HttpMethod.GET, "/api/students/**")
                                .hasAnyRole("ADMIN", "TEACHER")
                        
                        // PUT (Update): Both ADMIN and TEACHER
                        // Teachers can update grades and attendance
                        .requestMatchers(HttpMethod.PUT, "/api/students/**")
                                .hasAnyRole("ADMIN", "TEACHER")
                        
                        // USER MANAGEMENT - ADMIN only
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Set authentication provider
                .authenticationProvider(authenticationProvider())
                
                // Add JWT filter BEFORE UsernamePasswordAuthenticationFilter
                // This ensures JWT is validated before any form-based auth
                .addFilterBefore(jwtAuthenticationFilter, 
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration for frontend communication.
     * 
     * Security considerations:
     * - Restrict origins to known frontend URLs
     * - Explicitly list allowed methods
     * - Allow credentials for potential cookie-based features
     * - Expose Authorization header for JWT handling
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins - restrict to your frontend URL
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allowed headers - include Authorization for JWT
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // Expose headers to client
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply CORS config to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Password encoder using BCrypt.
     * BCrypt automatically handles salt generation and is resistant to rainbow table attacks.
     * Default strength is 10 (2^10 iterations).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider that uses our UserDetailsService and password encoder.
     * DaoAuthenticationProvider handles database-backed authentication.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean - required for manual authentication in login endpoint.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
