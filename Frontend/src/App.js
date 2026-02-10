import React, { useEffect, useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

// Components
import Navbar from './components/Navbar';
import { ProtectedRoute, AdminRoute } from './components/ProtectedRoute';

// Pages
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import StudentsPage from './pages/StudentsPage';
import UsersPage from './pages/UsersPage';

/**
 * Main Application Component.
 * 
 * Routing structure:
 * - /login: Public login page
 * - /dashboard: Protected - requires authentication
 * - /students: Protected - ADMIN and TEACHER
 * - /users: Protected - ADMIN only
 * - /: Redirects to dashboard if authenticated, login if not
 * 
 * Security features:
 * - Global event listeners for auth errors (401, 403)
 * - Toast notifications for security events
 * - Automatic redirect on unauthorized access
 */
const App = () => {
  const { isAuthenticated, loading } = useAuth();
  const [notification, setNotification] = useState(null);

  // Listen for auth/network events from Axios interceptor
  useEffect(() => {
    const handleForbidden = (event) => {
      setNotification({
        type: 'error',
        message: event.detail.message,
      });
    };

    const handleNetworkError = (event) => {
      setNotification({
        type: 'warning',
        message: event.detail.message,
      });
    };

    window.addEventListener('auth:forbidden', handleForbidden);
    window.addEventListener('network:error', handleNetworkError);

    return () => {
      window.removeEventListener('auth:forbidden', handleForbidden);
      window.removeEventListener('network:error', handleNetworkError);
    };
  }, []);

  // Clear notification after 5 seconds
  useEffect(() => {
    if (notification) {
      const timer = setTimeout(() => setNotification(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  // Show loading while checking auth state
  if (loading) {
    return (
      <div className="loading-container" style={{ height: '100vh' }}>
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="app">
      {/* Global notification for security events */}
      {notification && (
        <div 
          className={`alert alert-${notification.type}`}
          style={{
            position: 'fixed',
            top: '1rem',
            right: '1rem',
            zIndex: 9999,
            maxWidth: '400px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          }}
        >
          {notification.message}
          <button
            onClick={() => setNotification(null)}
            style={{
              marginLeft: '1rem',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              fontSize: '1.2rem',
            }}
          >
            &times;
          </button>
        </div>
      )}

      <Routes>
        {/* Public route - Login */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected routes - require authentication */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Navbar />
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* Students - accessible by ADMIN and TEACHER */}
        <Route
          path="/students"
          element={
            <ProtectedRoute>
              <Navbar />
              <StudentsPage />
            </ProtectedRoute>
          }
        />

        {/* Users - ADMIN only route */}
        <Route
          path="/users"
          element={
            <AdminRoute>
              <Navbar />
              <UsersPage />
            </AdminRoute>
          }
        />

        {/* Root redirect - to dashboard if authenticated, login otherwise */}
        <Route
          path="/"
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />

        {/* Catch-all - redirect to dashboard or login */}
        <Route
          path="*"
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
      </Routes>
    </div>
  );
};

export default App;
