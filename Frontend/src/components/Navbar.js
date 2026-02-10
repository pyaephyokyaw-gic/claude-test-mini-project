import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Navigation Bar Component.
 * 
 * Features:
 * - Shows user info and role badge
 * - Navigation links based on user role
 * - Admin-only links hidden from non-admins
 * - Logout functionality
 */
const Navbar = () => {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Get role display name
  const getRoleBadge = () => {
    if (isAdmin) {
      return <span className="badge badge-admin">ADMIN</span>;
    }
    return <span className="badge badge-teacher">TEACHER</span>;
  };

  return (
    <nav className="navbar">
      <NavLink to="/dashboard" className="navbar-brand">
        Student Management System
      </NavLink>

      <div className="navbar-nav">
        <NavLink 
          to="/dashboard" 
          className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
        >
          Dashboard
        </NavLink>
        
        <NavLink 
          to="/students" 
          className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
        >
          Students
        </NavLink>

        {/* Admin-only: User Management link */}
        {isAdmin && (
          <NavLink 
            to="/users" 
            className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
          >
            Users
          </NavLink>
        )}
      </div>

      <div className="user-info">
        {getRoleBadge()}
        <span className="user-badge">{user?.username}</span>
        <button onClick={handleLogout} className="btn btn-secondary btn-sm">
          Logout
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
