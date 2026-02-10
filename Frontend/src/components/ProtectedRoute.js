import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Protected Route Component - requires authentication.
 * 
 * Usage:
 * <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
 * 
 * Security behavior:
 * - If not authenticated, redirects to login
 * - Preserves intended destination for redirect after login
 * - Shows loading state while checking auth
 */
export const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  // Show loading while checking authentication
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    // Save the attempted URL for redirect after login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

/**
 * Admin Route Component - requires ADMIN role.
 * 
 * Usage:
 * <Route path="/users" element={<AdminRoute><UserManagement /></AdminRoute>} />
 * 
 * Security behavior:
 * - First checks authentication (redirects to login if not)
 * - Then checks for ADMIN role (shows access denied if not admin)
 * - Never shows admin content to non-admin users
 */
export const AdminRoute = ({ children }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  const location = useLocation();

  // Show loading while checking authentication
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Show access denied if not admin
  if (!isAdmin) {
    return <AccessDenied />;
  }

  return children;
};

/**
 * Role-based Route Component - requires specific role(s).
 * 
 * Usage:
 * <Route 
 *   path="/reports" 
 *   element={
 *     <RoleRoute allowedRoles={['ROLE_ADMIN', 'ROLE_TEACHER']}>
 *       <Reports />
 *     </RoleRoute>
 *   } 
 * />
 * 
 * @param {string[]} allowedRoles - Array of roles that can access this route
 */
export const RoleRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, hasAnyRole, loading } = useAuth();
  const location = useLocation();

  // Show loading while checking authentication
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Show access denied if user doesn't have required role
  if (!hasAnyRole(allowedRoles)) {
    return <AccessDenied />;
  }

  return children;
};

/**
 * Access Denied Component - shown when user lacks permissions.
 * 
 * Displays a friendly message and provides navigation options.
 */
export const AccessDenied = () => {
  return (
    <div className="container">
      <div className="access-denied">
        <div className="access-denied-icon">&#x1F6AB;</div>
        <h1 className="access-denied-title">Access Denied</h1>
        <p className="access-denied-message">
          You don't have permission to access this page.
          <br />
          Please contact your administrator if you believe this is an error.
        </p>
        <a href="/dashboard" className="btn btn-primary">
          Go to Dashboard
        </a>
      </div>
    </div>
  );
};

export default ProtectedRoute;
