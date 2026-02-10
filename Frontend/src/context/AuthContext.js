import React, { createContext, useContext, useState, useCallback, useEffect, useMemo } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../services/api';

/**
 * Authentication Context for React application.
 * 
 * Security features:
 * - JWT stored in localStorage (consider httpOnly cookies for production)
 * - Automatic token expiration checking
 * - Automatic logout on token expiry
 * - Role extraction from JWT (not hardcoded)
 * 
 * Usage:
 * const { user, login, logout, isAdmin, isTeacher } = useAuth();
 */

const AuthContext = createContext(null);

/**
 * Decode JWT and extract user information.
 * NEVER hardcode roles - always extract from token.
 */
const decodeToken = (token) => {
  try {
    const decoded = jwtDecode(token);
    return {
      username: decoded.sub,
      roles: decoded.roles || [],
      exp: decoded.exp,
      iat: decoded.iat,
    };
  } catch (error) {
    console.error('Failed to decode token:', error);
    return null;
  }
};

/**
 * Check if token is expired.
 */
const isTokenExpired = (token) => {
  const decoded = decodeToken(token);
  if (!decoded) return true;
  
  // exp is in seconds, Date.now() is in milliseconds
  const expirationTime = decoded.exp * 1000;
  const currentTime = Date.now();
  
  // Add 10 second buffer to prevent edge cases
  return currentTime >= expirationTime - 10000;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  /**
   * Initialize auth state from localStorage on mount.
   * Validates stored token before using it.
   */
  useEffect(() => {
    const initializeAuth = () => {
      const storedToken = localStorage.getItem('accessToken');
      
      if (storedToken && !isTokenExpired(storedToken)) {
        const decoded = decodeToken(storedToken);
        if (decoded) {
          setToken(storedToken);
          setUser({
            username: decoded.username,
            roles: decoded.roles,
          });
        } else {
          // Invalid token - clear storage
          localStorage.removeItem('accessToken');
        }
      } else if (storedToken) {
        // Token expired - clear storage
        localStorage.removeItem('accessToken');
      }
      
      setLoading(false);
    };

    initializeAuth();
  }, []);

  /**
   * Set up automatic logout on token expiration.
   */
  useEffect(() => {
    if (!token) return;

    const decoded = decodeToken(token);
    if (!decoded) return;

    const expirationTime = decoded.exp * 1000;
    const currentTime = Date.now();
    const timeUntilExpiry = expirationTime - currentTime;

    // Set timeout to logout when token expires
    if (timeUntilExpiry > 0) {
      const timeoutId = setTimeout(() => {
        console.log('Token expired - logging out');
        logout();
      }, timeUntilExpiry);

      return () => clearTimeout(timeoutId);
    }
  }, [token]); // eslint-disable-line react-hooks/exhaustive-deps

  /**
   * Login function - authenticates user and stores JWT.
   * 
   * @param {string} username 
   * @param {string} password 
   * @returns {Promise<Object>} User info on success
   * @throws {Error} Authentication error
   */
  const login = useCallback(async (username, password) => {
    const response = await api.post('/api/auth/login', {
      username,
      password,
    });

    const { accessToken, roles } = response.data;

    // Store token securely
    localStorage.setItem('accessToken', accessToken);
    
    // Update state
    setToken(accessToken);
    setUser({
      username,
      roles,
    });

    return response.data;
  }, []);

  /**
   * Logout function - clears auth state and storage.
   * Called manually or automatically on token expiration.
   */
  const logout = useCallback(() => {
    localStorage.removeItem('accessToken');
    setToken(null);
    setUser(null);
  }, []);

  /**
   * Check if current user has ADMIN role.
   * Used for conditional UI rendering.
   */
  const isAdmin = useMemo(() => {
    return user?.roles?.includes('ROLE_ADMIN') || false;
  }, [user]);

  /**
   * Check if current user has TEACHER role.
   * Used for conditional UI rendering.
   */
  const isTeacher = useMemo(() => {
    return user?.roles?.includes('ROLE_TEACHER') || false;
  }, [user]);

  /**
   * Check if user is authenticated.
   */
  const isAuthenticated = useMemo(() => {
    return !!user && !!token && !isTokenExpired(token);
  }, [user, token]);

  /**
   * Check if user has a specific role.
   * @param {string} role - Role to check (e.g., 'ROLE_ADMIN')
   */
  const hasRole = useCallback((role) => {
    return user?.roles?.includes(role) || false;
  }, [user]);

  /**
   * Check if user has any of the specified roles.
   * @param {string[]} roles - Array of roles to check
   */
  const hasAnyRole = useCallback((roles) => {
    return roles.some(role => user?.roles?.includes(role));
  }, [user]);

  // Context value memoized to prevent unnecessary re-renders
  const value = useMemo(() => ({
    user,
    token,
    loading,
    login,
    logout,
    isAdmin,
    isTeacher,
    isAuthenticated,
    hasRole,
    hasAnyRole,
  }), [user, token, loading, login, logout, isAdmin, isTeacher, isAuthenticated, hasRole, hasAnyRole]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Custom hook to access auth context.
 * Must be used within AuthProvider.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
