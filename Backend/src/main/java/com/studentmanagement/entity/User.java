package com.studentmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User entity implementing Spring Security's UserDetails.
 * This allows direct integration with Spring Security authentication.
 * 
 * Security considerations:
 * - Password is stored as BCrypt hash (never plain text)
 * - Roles are stored as a Set to prevent duplicates
 * - UserDetails implementation allows Spring Security to use this entity directly
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password; // BCrypt hashed

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * Converts user roles to Spring Security GrantedAuthority collection.
     * This is used by Spring Security for authorization checks.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Helper method to add a role to the user
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Helper method to remove a role from the user
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Check if user has admin role
     */
    public boolean isAdmin() {
        return roles.contains(Role.ROLE_ADMIN);
    }

    /**
     * Check if user has teacher role
     */
    public boolean isTeacher() {
        return roles.contains(Role.ROLE_TEACHER);
    }
}
