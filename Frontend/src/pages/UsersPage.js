import React, { useState, useEffect, useCallback } from 'react';
import { userApi } from '../services/api';

/**
 * User Management Page (Admin Only).
 * 
 * Features:
 * - List all users
 * - Create new users
 * - Delete users
 * - Toggle user enabled/disabled status
 * 
 * Security:
 * - This entire page is protected by AdminRoute
 * - Backend also enforces ADMIN role via @PreAuthorize
 */
const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  
  // Form state
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    roles: [],
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  // Fetch users
  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await userApi.getAll();
      setUsers(response.data);
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Failed to load users');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // Clear messages after delay
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [success]);

  // Form validation
  const validateForm = () => {
    const errors = {};
    
    if (!formData.username.trim()) {
      errors.username = 'Username is required';
    } else if (formData.username.length < 3) {
      errors.username = 'Username must be at least 3 characters';
    }
    
    if (!formData.password.trim()) {
      errors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      errors.password = 'Password must be at least 6 characters';
    }
    
    if (formData.roles.length === 0) {
      errors.roles = 'At least one role must be selected';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Open modal for creating new user
  const handleCreate = () => {
    setFormData({ username: '', password: '', roles: [] });
    setFormErrors({});
    setShowModal(true);
  };

  // Handle form input change
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  // Handle role checkbox change
  const handleRoleChange = (role) => {
    setFormData(prev => ({
      ...prev,
      roles: prev.roles.includes(role)
        ? prev.roles.filter(r => r !== role)
        : [...prev.roles, role],
    }));
  };

  // Submit form
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setSubmitting(true);
    setError(null);
    
    try {
      await userApi.create(formData);
      setSuccess('User created successfully');
      setShowModal(false);
      fetchUsers();
    } catch (err) {
      console.error('Error creating user:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data?.fieldErrors) {
        setFormErrors(err.response.data.fieldErrors);
      } else {
        setError('Failed to create user');
      }
    } finally {
      setSubmitting(false);
    }
  };

  // Delete user
  const handleDelete = async (user) => {
    if (!window.confirm(`Are you sure you want to delete ${user.username}?`)) {
      return;
    }
    
    try {
      await userApi.delete(user.id);
      setSuccess('User deleted successfully');
      fetchUsers();
    } catch (err) {
      console.error('Error deleting user:', err);
      setError('Failed to delete user');
    }
  };

  // Toggle user status
  const handleToggleStatus = async (user) => {
    try {
      await userApi.toggleStatus(user.id);
      setSuccess(`User ${user.enabled ? 'disabled' : 'enabled'} successfully`);
      fetchUsers();
    } catch (err) {
      console.error('Error toggling user status:', err);
      setError('Failed to update user status');
    }
  };

  // Get role badge
  const getRoleBadge = (role) => {
    if (role === 'ROLE_ADMIN') {
      return <span className="badge badge-admin" key={role}>Admin</span>;
    }
    return <span className="badge badge-teacher" key={role}>Teacher</span>;
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">User Management</h1>
        <button onClick={handleCreate} className="btn btn-success">
          + Add User
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* Users Table */}
      <div className="card">
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Roles</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan="5" style={{ textAlign: 'center', padding: '2rem' }}>
                    No users found
                  </td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                        {user.roles.map(role => getRoleBadge(role))}
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${user.enabled ? 'badge-enabled' : 'badge-disabled'}`}>
                        {user.enabled ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button
                          onClick={() => handleToggleStatus(user)}
                          className={`btn btn-sm ${user.enabled ? 'btn-warning' : 'btn-success'}`}
                        >
                          {user.enabled ? 'Disable' : 'Enable'}
                        </button>
                        <button
                          onClick={() => handleDelete(user)}
                          className="btn btn-danger btn-sm"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create User Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">Add New User</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                &times;
              </button>
            </div>
            
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Username</label>
                  <input
                    type="text"
                    name="username"
                    className={`form-input ${formErrors.username ? 'error' : ''}`}
                    value={formData.username}
                    onChange={handleInputChange}
                    placeholder="Enter username"
                  />
                  {formErrors.username && <span className="form-error">{formErrors.username}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    name="password"
                    className={`form-input ${formErrors.password ? 'error' : ''}`}
                    value={formData.password}
                    onChange={handleInputChange}
                    placeholder="Enter password (min 6 characters)"
                  />
                  {formErrors.password && <span className="form-error">{formErrors.password}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label">Roles</label>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                      <input
                        type="checkbox"
                        checked={formData.roles.includes('ROLE_ADMIN')}
                        onChange={() => handleRoleChange('ROLE_ADMIN')}
                      />
                      <span className="badge badge-admin">Admin</span>
                      <span style={{ fontSize: '0.75rem', color: '#757575' }}>
                        - Full CRUD on Students and Users
                      </span>
                    </label>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                      <input
                        type="checkbox"
                        checked={formData.roles.includes('ROLE_TEACHER')}
                        onChange={() => handleRoleChange('ROLE_TEACHER')}
                      />
                      <span className="badge badge-teacher">Teacher</span>
                      <span style={{ fontSize: '0.75rem', color: '#757575' }}>
                        - Read students + Update grades/attendance
                      </span>
                    </label>
                  </div>
                  {formErrors.roles && <span className="form-error">{formErrors.roles}</span>}
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting ? 'Creating...' : 'Create User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UsersPage;
