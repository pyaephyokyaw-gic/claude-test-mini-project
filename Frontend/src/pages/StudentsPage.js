import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { studentApi } from '../services/api';

/**
 * Students Management Page.
 * 
 * Features:
 * - List all students
 * - Create student (Admin only)
 * - Update student (Admin and Teacher)
 * - Delete student (Admin only) - HIDDEN from Teachers
 * - Search functionality
 * 
 * Security:
 * - Delete button only rendered for ADMIN role
 * - Backend enforces authorization even if UI is bypassed
 */
const StudentsPage = () => {
  const { isAdmin } = useAuth();
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('create'); // 'create' or 'edit'
  const [currentStudent, setCurrentStudent] = useState(null);
  
  // Form state
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    grade: '',
    attendance: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  // Search state
  const [searchTerm, setSearchTerm] = useState('');

  // Fetch students
  const fetchStudents = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await studentApi.getAll();
      setStudents(response.data);
    } catch (err) {
      console.error('Error fetching students:', err);
      setError('Failed to load students');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStudents();
  }, [fetchStudents]);

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
    
    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    }
    
    if (!formData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = 'Invalid email format';
    }
    
    if (formData.grade === '' || formData.grade === null) {
      errors.grade = 'Grade is required';
    } else if (formData.grade < 0 || formData.grade > 100) {
      errors.grade = 'Grade must be between 0 and 100';
    }
    
    if (formData.attendance === '' || formData.attendance === null) {
      errors.attendance = 'Attendance is required';
    } else if (formData.attendance < 0 || formData.attendance > 100) {
      errors.attendance = 'Attendance must be between 0 and 100';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Open modal for creating new student
  const handleCreate = () => {
    setModalMode('create');
    setCurrentStudent(null);
    setFormData({ name: '', email: '', grade: '', attendance: '' });
    setFormErrors({});
    setShowModal(true);
  };

  // Open modal for editing student
  const handleEdit = (student) => {
    setModalMode('edit');
    setCurrentStudent(student);
    setFormData({
      name: student.name,
      email: student.email,
      grade: student.grade,
      attendance: student.attendance,
    });
    setFormErrors({});
    setShowModal(true);
  };

  // Handle form input change
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'grade' || name === 'attendance' 
        ? (value === '' ? '' : Number(value))
        : value,
    }));
  };

  // Submit form
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setSubmitting(true);
    setError(null);
    
    try {
      if (modalMode === 'create') {
        await studentApi.create(formData);
        setSuccess('Student created successfully');
      } else {
        await studentApi.update(currentStudent.id, formData);
        setSuccess('Student updated successfully');
      }
      
      setShowModal(false);
      fetchStudents();
    } catch (err) {
      console.error('Error saving student:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data?.fieldErrors) {
        setFormErrors(err.response.data.fieldErrors);
      } else {
        setError('Failed to save student');
      }
    } finally {
      setSubmitting(false);
    }
  };

  // Delete student (Admin only)
  const handleDelete = async (student) => {
    if (!window.confirm(`Are you sure you want to delete ${student.name}?`)) {
      return;
    }
    
    try {
      await studentApi.delete(student.id);
      setSuccess('Student deleted successfully');
      fetchStudents();
    } catch (err) {
      console.error('Error deleting student:', err);
      if (err.response?.status === 403) {
        setError('You do not have permission to delete students');
      } else {
        setError('Failed to delete student');
      }
    }
  };

  // Filter students by search term
  const filteredStudents = students.filter(student =>
    student.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    student.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

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
        <h1 className="page-title">Students</h1>
        
        {/* Admin only: Add Student button */}
        {isAdmin && (
          <button onClick={handleCreate} className="btn btn-success">
            + Add Student
          </button>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* Search */}
      <div style={{ marginBottom: '1.5rem' }}>
        <input
          type="text"
          className="form-input"
          placeholder="Search by name or email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ maxWidth: '300px' }}
        />
      </div>

      {/* Students Table */}
      <div className="card">
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Grade</th>
                <th>Attendance</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredStudents.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '2rem' }}>
                    {searchTerm ? 'No students match your search' : 'No students found'}
                  </td>
                </tr>
              ) : (
                filteredStudents.map((student) => (
                  <tr key={student.id}>
                    <td>{student.id}</td>
                    <td>{student.name}</td>
                    <td>{student.email}</td>
                    <td>
                      <span style={{ 
                        color: student.grade >= 70 ? '#388e3c' : student.grade >= 50 ? '#f57c00' : '#d32f2f'
                      }}>
                        {student.grade}%
                      </span>
                    </td>
                    <td>
                      <span style={{ 
                        color: student.attendance >= 75 ? '#388e3c' : '#d32f2f'
                      }}>
                        {student.attendance}%
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button
                          onClick={() => handleEdit(student)}
                          className="btn btn-primary btn-sm"
                        >
                          Edit
                        </button>
                        
                        {/* 
                          SECURITY: Delete button is ONLY rendered for ADMIN role.
                          Teachers will never see this button.
                          Backend also enforces this via @PreAuthorize("hasRole('ADMIN')")
                        */}
                        {isAdmin && (
                          <button
                            onClick={() => handleDelete(student)}
                            className="btn btn-danger btn-sm"
                          >
                            Delete
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">
                {modalMode === 'create' ? 'Add New Student' : 'Edit Student'}
              </h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>
                &times;
              </button>
            </div>
            
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">Name</label>
                  <input
                    type="text"
                    name="name"
                    className={`form-input ${formErrors.name ? 'error' : ''}`}
                    value={formData.name}
                    onChange={handleInputChange}
                    placeholder="Enter student name"
                  />
                  {formErrors.name && <span className="form-error">{formErrors.name}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input
                    type="email"
                    name="email"
                    className={`form-input ${formErrors.email ? 'error' : ''}`}
                    value={formData.email}
                    onChange={handleInputChange}
                    placeholder="Enter email address"
                  />
                  {formErrors.email && <span className="form-error">{formErrors.email}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label">Grade (%)</label>
                  <input
                    type="number"
                    name="grade"
                    className={`form-input ${formErrors.grade ? 'error' : ''}`}
                    value={formData.grade}
                    onChange={handleInputChange}
                    placeholder="0-100"
                    min="0"
                    max="100"
                    step="0.1"
                  />
                  {formErrors.grade && <span className="form-error">{formErrors.grade}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label">Attendance (%)</label>
                  <input
                    type="number"
                    name="attendance"
                    className={`form-input ${formErrors.attendance ? 'error' : ''}`}
                    value={formData.attendance}
                    onChange={handleInputChange}
                    placeholder="0-100"
                    min="0"
                    max="100"
                  />
                  {formErrors.attendance && <span className="form-error">{formErrors.attendance}</span>}
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
                  {submitting ? 'Saving...' : (modalMode === 'create' ? 'Create' : 'Save Changes')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentsPage;
