package com.studentmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Utility class for token generation, validation, and parsing.
 * 
 * Security decisions:
 * - Uses HS256 (HMAC-SHA256) algorithm for signing
 * - Stores roles as claims for RBAC enforcement
 * - Validates signature, expiration, and structure
 * - Uses JJWT 0.12.x API (latest stable)
 * 
 * Production considerations:
 * - Secret key should be loaded from environment variables or secrets manager
 * - Token expiration should align with security requirements
 * - Consider refresh token mechanism for long-lived sessions
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Get the signing key from base64-encoded secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token from Authentication object.
     * Includes username and roles in the token claims.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        
        // Extract roles as list of strings for the token
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generate JWT token directly from username and roles.
     * Used when we need to generate token without Authentication object.
     */
    public String generateJwtToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extract username from JWT token.
     */
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extract roles from JWT token.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return (List<String>) claims.get("roles");
    }

    /**
     * Get token expiration time in milliseconds.
     */
    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Get token expiration date from token.
     */
    public Date getExpirationDateFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    /**
     * Validate JWT token.
     * Checks:
     * - Signature validity
     * - Token expiration
     * - Token structure (malformed)
     * - Supported algorithm
     * 
     * @param authToken the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException e) {
            // Invalid JWT signature - potential tampering attempt
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            // JWT token is malformed - not a valid JWT structure
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // JWT token has expired - user needs to re-authenticate
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // JWT token uses unsupported algorithm or format
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // JWT claims string is empty or null
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
